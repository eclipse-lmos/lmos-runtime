/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.kernel.steps


import org.eclipse.lmos.kernel.conversations.AccessValidation
import org.eclipse.lmos.kernel.conversations.Conversation
import org.eclipse.lmos.kernel.conversations.ConversationAccessValidator
import org.eclipse.lmos.kernel.conversations.ConversationHistoryRepository
import org.eclipse.lmos.kernel.getOrNull
import org.eclipse.lmos.kernel.model.ConversationMessage
import org.eclipse.lmos.kernel.model.UserMessage
import org.eclipse.lmos.kernel.services.EventLogger
import org.eclipse.lmos.kernel.steps.RequestStatus.AUTHORIZATION_REQUIRED
import org.eclipse.lmos.kernel.steps.RequestStatus.INSUFFICIENT_PERMISSIONS
import org.eclipse.lmos.kernel.steps.Status.BREAK
import org.eclipse.lmos.kernel.steps.Status.CONTINUE
import org.eclipse.lmos.kernel.user.UserInformation
import org.eclipse.lmos.kernel.user.UserProvider
import org.slf4j.LoggerFactory

const val CONVERSATION_HISTORY = "conversation_history"
const val PREVIOUS_CONVERSATION_HISTORY = "previous_conversation_history"
const val IS_INITIAL_TURN = "initial_turn"

class InitConversationHistory(
    private val conversationHistoryRepository: ConversationHistoryRepository,
    private val conversationAccessValidator: ConversationAccessValidator,
    private val userProvider: UserProvider,
    private val eventLogger: EventLogger
) : AbstractProcessingStep() {

    private val log = LoggerFactory.getLogger(javaClass)

    override suspend fun executeInternal(input: Input): Output {
        val currentTurnId = input.requestContext.turnId
        val conversationId = input.requestContext.conversationId
        val user = userProvider.provideUser().getOrNull()
        val profileId: String? = user?.profileId
        val partyId: String? = user?.partyId

        val (conversation, existsInCache) = getOrCreateConversation(conversationId, currentTurnId, profileId, partyId)

        if(!existsInCache) {
            eventLogger.logConversationInitiated(input)
            input.stepContext[IS_INITIAL_TURN] = true
        } else {
            input.stepContext[IS_INITIAL_TURN] = false
        }
        eventLogger.logTurnStarted(input)
        validateConversationAccess(conversation, user) { status ->
            log.info("Unauthorized client $profileId is trying to access sensitive conversation... returning $status")
            input.requestContext.requestStatus = status
            return Output(BREAK, input)
        }

        val mergedConversation = if (input.stepContext.contains(PREVIOUS_CONVERSATION_HISTORY)) {
            conversation.copy(history = input.context<List<ConversationMessage>>(PREVIOUS_CONVERSATION_HISTORY))
        } else {
            conversation + UserMessage(input.content)
        }

        input.stepContext[CONVERSATION_HISTORY] = mergedConversation
        return Output(CONTINUE, input)
    }

    private inline fun validateConversationAccess(
        conversation: Conversation,
        user: UserInformation?,
        onBreak: (requestStatus: RequestStatus) -> Nothing
    ) {
        when (conversationAccessValidator.canUserAccess(conversation, user)) {
            AccessValidation.AUTHORIZATION_REQUIRED -> onBreak(AUTHORIZATION_REQUIRED)
            AccessValidation.INVALID_PROFILE_ID -> onBreak(INSUFFICIENT_PERMISSIONS)
            else -> {}
        }
    }

    /**
     * Looks up the cache for [Conversation] object with given conversationId.
     * - In case of cache hit fetches the cached [Conversation] object updates the current turn id and returns a pair of this object and a boolean set to true, signifying a cache hit.
     * - In case of a cache miss creates a new [Conversation] object and returns a pair of this object and a boolean signifying a cache miss.
     *
     * @param conversationId The conversation id to find in cache
     * @param currentTurnId The current turn id for then conversation
     * @param profileId The profile id of the user. Defaults to null for guest users.
     * @param partyId Party id to identify customer in case of using fragMagenta entraId token
     * @return A pair with first element as the [Conversation] object and the second element as a boolean set to true if we find the element in the cache otherwise it is set to false
     */
    private suspend fun getOrCreateConversation(conversationId: String, currentTurnId: String, profileId: String?, partyId: String?): Pair<Conversation, Boolean> {
        val cachedConversation = conversationHistoryRepository.getConversation(conversationId)?.copy(currentTurnId = currentTurnId)
        return if (cachedConversation != null) {
            Pair(cachedConversation, true)
        } else {
            Pair(Conversation(conversationId, currentTurnId = currentTurnId, profileId = profileId, partyId = partyId), false)
        }
    }
}

class SaveConversationHistory(private val conversationHistoryRepository: ConversationHistoryRepository) : AbstractProcessingStep() {

    override suspend fun executeInternal(input: Input): Output {
        val conversation = input.stepContext[CONVERSATION_HISTORY] as Conversation?
        if (conversation != null) conversationHistoryRepository.saveConversation(conversation)
        return Output(CONTINUE, input)
    }
}

class DeleteConversationHistory(private val conversationHistoryRepository: ConversationHistoryRepository) : AbstractProcessingStep() {

    override suspend fun executeInternal(input: Input): Output {
        conversationHistoryRepository.deleteConversation(input.requestContext.conversationId)
        return Output(CONTINUE, input)
    }
}

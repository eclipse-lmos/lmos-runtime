package org.eclipse.lmos.runtime.graphql.service.inbound.subscription

import com.expediagroup.graphql.generator.annotations.GraphQLDescription
import com.expediagroup.graphql.server.operations.Subscription
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.flow
import org.eclipse.lmos.runtime.core.inbound.ConversationHandler
import org.eclipse.lmos.runtime.core.model.Conversation
import org.springframework.stereotype.Component

@Component
class ConversationSubscription(private val conversationHandler: ConversationHandler) : Subscription {

    @GraphQLDescription("Processes the user input and returns the result")
    suspend fun chat(conversation: Conversation,
                        conversationId: String,
                        tenantId: String,
                        turnId: String
    ) = flow {
            coroutineScope {

                val assistantMessageFlow = conversationHandler.handleConversation(
                    conversation, conversationId, tenantId, turnId
                )

                assistantMessageFlow.collect {
                    emit(it)
                }

            }
        }

}

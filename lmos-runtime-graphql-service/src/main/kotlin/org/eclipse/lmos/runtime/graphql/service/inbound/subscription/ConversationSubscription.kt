package org.eclipse.lmos.runtime.graphql.service.inbound.subscription

import com.expediagroup.graphql.generator.annotations.GraphQLDescription
import com.expediagroup.graphql.server.operations.Subscription
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.channelFlow
import org.eclipse.lmos.arc.api.Message
import org.eclipse.lmos.runtime.core.inbound.ConversationHandler
import org.eclipse.lmos.runtime.core.model.AssistantMessage
import org.eclipse.lmos.runtime.core.model.Conversation
import org.springframework.stereotype.Component

@Component
class ConversationSubscription(private val conversationHandler: ConversationHandler) : Subscription {
    @GraphQLDescription("Processes the user input and returns the result")
    suspend fun chat(
        conversation: Conversation,
        conversationId: String,
        tenantId: String,
        turnId: String,
    ) = channelFlow {
        coroutineScope {
            val messageChannel = Channel<AssistantMessage>()

            async {
                sendIntermediateMessage(messageChannel)
            }

            val assistantMessageFlow =
                conversationHandler.handleConversation(
                    conversation,
                    conversationId,
                    tenantId,
                    turnId,
                )

            messageChannel.send(AssistantMessage("Runtime Message before agent"))

            kotlinx.coroutines.delay(1000)

            assistantMessageFlow.collect {
                send(it)
            }

            kotlinx.coroutines.delay(1000)

            messageChannel.send(AssistantMessage("Runtime Message after agent"))
        }
    }

    private suspend fun ProducerScope<AssistantMessage>.sendIntermediateMessage(messageChannel: Channel<AssistantMessage>) {
        for (message in messageChannel) {
            trySend(message)
        }
    }

    fun AssistantMessage?.toMessage() = Message("assistant", this?.content ?: "", turnId = "1")
}

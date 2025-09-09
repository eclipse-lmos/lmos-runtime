/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package selfeval

import com.telekom.lmos.boot.selfeval.LLMSelfEvaluator
import org.eclipse.lmos.kernel.conversations.Conversation
import org.eclipse.lmos.kernel.model.UserMessage
import org.eclipse.lmos.kernel.prompt.PromptTemplateExecutor
import org.eclipse.lmos.kernel.steps.*
import org.eclipse.lmos.kernel.user.UserInformation
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class LLMSelfEvaluatorTest {
    private val promptTemplateExecutor = mockk<PromptTemplateExecutor>()
    private val llmSelfEvaluator = LLMSelfEvaluator(promptTemplateExecutor)

    @Test
    fun return_self_evaluation_result(){
        val input = Input("what is magentatv?",
            RequestContext("99", "12", "de", RequestStatus.ONGOING),
            stepContext =
            mutableMapOf(
                "natco_code" to "de",
                "user" to UserInformation(
                    accessToken = "[Access Token for M-API]",
                    profileId = "0033353234"
                ),
                CONVERSATION_HISTORY to Conversation("99", RequestStatus.ONGOING, null, "12", listOf(UserMessage("what is magentatv?"))),
                "question" to "what is magentatv?",
                "context" to "magentatv"
            )
        )
        val variables = mapOf(
            LLMSelfEvaluator.ANSWER_VAR to input.content,
            LLMSelfEvaluator.QUESTION_VAR to input.context<String>(LLMSelfEvaluator.QUESTION_VAR),
            LLMSelfEvaluator.CONTEXT_VAR to input.context<String>(LLMSelfEvaluator.CONTEXT_VAR)
        )
        coEvery { promptTemplateExecutor.execute(LLMSelfEvaluator.SELF_EVALUATION_PROMPT, variables) } returns "-1"
        val result = runBlocking { llmSelfEvaluator.evaluate(input) }
        assertEquals(-1, result)
    }
}
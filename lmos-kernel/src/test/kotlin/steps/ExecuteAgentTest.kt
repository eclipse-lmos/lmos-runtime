/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.telekom.lmos.platform.assistants.steps

import org.eclipse.lmos.kernel.ConfigurationException
import org.eclipse.lmos.kernel.agent.Agent
import org.eclipse.lmos.kernel.agent.AgentProfile
import org.eclipse.lmos.kernel.conversations.Conversation
import org.eclipse.lmos.kernel.model.AssistantMessage
import org.eclipse.lmos.kernel.model.UserMessage
import org.eclipse.lmos.kernel.steps.CONVERSATION_HISTORY
import org.eclipse.lmos.kernel.steps.ExecuteAgent
import org.eclipse.lmos.kernel.steps.Input
import org.eclipse.lmos.kernel.steps.Output
import org.eclipse.lmos.kernel.steps.RequestContext
import org.eclipse.lmos.kernel.steps.RequestStatus
import org.eclipse.lmos.kernel.steps.Status
import org.eclipse.lmos.kernel.user.UserInformation
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.slf4j.MDC
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ExecuteAgentTest {
    private val agent = mockk<Agent>()
    private val agents = listOf(agent)
    private val executeAssistant = ExecuteAgent(agents)

    @Test
    fun should_classify_intent_and_execute() {
        val input = Input(
            "Welche Geräte werden von diesem Dienst unterstützt? Widerruf",
            RequestContext("99", "12", "de", RequestStatus.ONGOING),
            mutableMapOf(
                "natco_code" to "de",
                "user" to UserInformation(
                    accessToken = "[Access Token for M-API]",
                    profileId = "0033353234"
                ),
                CONVERSATION_HISTORY to Conversation(
                    "99", RequestStatus.ONGOING, null, "12",
                    listOf(
                        UserMessage("what is magentatv?"),
                        AssistantMessage("MagentaTV ist ein Streaming-Dienst der Telekom, der exklusive Filme & Serien, aktuelle Blockbuster und besten Live Sport bietet. Mit WOW können MagentaTV Kunden der Telekom zusätzlich spezielle Optionen wie Live-Sport, Filme & Serien oder Serien buchen. Benötigen Sie weitere Informationen?")
                    )
                ),
                "intent_classification" to "faq"
            )
        )

        coEvery { agent.execute(input) } returns Output(Status.BREAK, input)
        coEvery { agent.canHandle("faq") } returns true
        runBlocking {
            assertEquals(Output(Status.BREAK, input), executeAssistant.execute(input))
        }
        coVerify(exactly = 1) {
            agent.execute(input)
        }
    }

    @Test
    fun should_throw_exception_as_not_intent_classified() {
        val input = Input(
            "Welche Geräte werden von diesem Dienst unterstützt? Widerruf",
            RequestContext("99", "12", "de", RequestStatus.ONGOING),
            mutableMapOf(
                "natco_code" to "de",
                "user" to UserInformation(
                    accessToken = "[Access Token for M-API]",
                    profileId = "0033353234"
                ),
                CONVERSATION_HISTORY to Conversation(
                    "99", RequestStatus.ONGOING, null, "12",
                    listOf()
                ),
                "intent_classification" to "faq"
            )
        )

        coEvery { agent.execute(input) } returns Output(Status.BREAK, input)
        coEvery { agent.canHandle("faq") } returns false
        runBlocking {
            assertFailsWith<ConfigurationException> { executeAssistant.execute(input) }
        }
        coVerify(exactly = 0) {
            agent.execute(input)
        }
    }

    @Test
    fun should_set_agent_as_step(): Unit = runBlocking {
        val input = Input(
            "",
            RequestContext("99", "12", "de", RequestStatus.ONGOING),
            mutableMapOf()
        )
        val testAgent = TestAgent()
        ExecuteAgent(listOf(testAgent)).execute(input)
        assertEquals("TestAgent", testAgent.stepName)
    }
}

class TestAgent : Agent() {

    var stepName: String? = null

    override fun canHandle(intent: String) = true
    override fun profile() = AgentProfile(name = "TestAgent", purpose = "", dp = "")

    override suspend fun executeInternal(input: Input): Output {
        stepName = MDC.get("step")
        return Output(Status.CONTINUE, input)
    }
}
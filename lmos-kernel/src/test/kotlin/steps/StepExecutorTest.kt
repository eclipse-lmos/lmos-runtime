/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.telekom.lmos.platform.assistants.steps

import org.eclipse.lmos.kernel.conversations.Conversation
import org.eclipse.lmos.kernel.model.AssistantMessage
import org.eclipse.lmos.kernel.model.UserMessage
import org.eclipse.lmos.kernel.steps.*
import org.eclipse.lmos.kernel.user.UserInformation
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class StepExecutorTest {

    private val stepFactory = mockk<StepFactory>()

    @Test
    fun test_step_return_break_status(){
        val input = createInput()
        val step = mockk<Step>()
        val stepFactory = object : StepFactory {
            override fun getStep(stepClass: Class<out Step>): Step = step
        }
        val stepExecutor= StepExecutor(stepFactory)
        coEvery { step.execute(input) } returns Output(Status.BREAK, input)
        coEvery { step.canHandle(any()) } returns true // This step will be executed
        runBlocking {
            assertEquals(Output(Status.BREAK, input), stepExecutor.seq().step<ClassifyIntent>().end().execute(input))
             }
    }

    @Test
    fun test_step_if_step_return_continue_status(){
        val input = createInput()
        val step = mockk<Step>()
        val stepFactory = object : StepFactory {
            override fun getStep(stepClass: Class<out Step>): Step = step
        }
        val stepExecutor= StepExecutor(stepFactory)
        coEvery { step.execute(input) } returns Output(Status.CONTINUE, input)
        coEvery { step.canHandle(any()) } returns true // This step will be executed
        runBlocking {
            assertEquals(Output(Status.CONTINUE, input), stepExecutor.seq().step<ClassifyIntent>().end().execute(input))
        }
    }

    @Test
    fun test_step_if_step_is_empty(){
        val input = createInput()
        val step = mockk<Step>()
        val stepFactory = object : StepFactory {
            override fun getStep(stepClass: Class<out Step>): Step = step
        }
        val stepExecutor= StepExecutor(stepFactory)
        runBlocking {
            assertEquals(Output(Status.CONTINUE, input), stepExecutor.seq().end().execute(input))
        }
    }

    @Test
    fun test_step_if_step_throw_exception(){
        val input = createInput()
        val step = mockk<Step>()
        val stepFactory = object : StepFactory {
            override fun getStep(stepClass: Class<out Step>): Step = step
        }
        val stepExecutor= StepExecutor(stepFactory)
        coEvery { step.canHandle(any()) } returns true // This step will be executed
        coEvery { step.execute(input) } throws Exception("Failed")
        runBlocking {
            val output = stepExecutor.seq().step<ClassifyIntent>().end().execute(input)
            assertEquals(Status.BREAK, output.status)
            assertEquals("Failed", output.errorCause?.message)
        }
    }

//    @Test
//    fun `should Skip Step when canHandle Returns False`(){
//        val input = Input("Welche Geräte werden von diesem Dienst unterstützt? Widerruf",
//            RequestContext("99", "12", "de", RequestStatus.ONGOING),
//            mutableMapOf(
//                "natco_code" to "de",
//                "user" to UserInformation(
//                    accessToken = "[Access Token for M-API]",
//                    profileId = "0033353234"
//                ),
//                CONVERSATION_HISTORY to Conversation("99", RequestStatus.ONGOING, null, "12",
//                    listOf(
//                        UserMessage("what is magentatv?"),
//                        AssistantMessage("MagentaTV ist ein Streaming-Dienst der Telekom, der exklusive Filme & Serien, aktuelle Blockbuster und besten Live Sport bietet. Mit WOW können MagentaTV Kunden der Telekom zusätzlich spezielle Optionen wie Live-Sport, Filme & Serien oder Serien buchen. Benötigen Sie weitere Informationen?")
//                    )),
//                "intent_classification" to "faq"
//            )
//        )
//        val step = mockk<Step>()
//        val stepFactory = object : StepFactory {
//            override fun getStep(stepClass: Class<out Step>): Step {
//                return step
//            }
//        }
//        val stepExecutor= StepExecutor(stepFactory)
//        coEvery { step.canHandle(input) } returns false
//        coEvery { step.execute(input) } returns Output("Step Executed", Status.CONTINUE, input)
//        runBlocking {
//            val actual = stepExecutor.seq().step<ClassifyIntent>().end().execute(input)
//            assertEquals(Output(Status.CONTINUE, input), actual)
//        }
//    }

    @Test
    fun `should skip step if canHandle returns false`() {
        // Mocking steps
        val firstStep = mockk<Step>()
        val secondStep = mockk<Step>()

        // Mocking the stepFactory to return mocked steps
        val stepFactory = mockk<StepFactory> {
            every { getStep(any()) } returns firstStep andThen secondStep
        }

        // Setting up StepExecutor
        val stepExecutor = StepExecutor(stepFactory).seq().step(firstStep).step(secondStep).end()

        val requestContext: RequestContext = mockk<RequestContext>()

        // Mocking the steps
        coEvery { firstStep.canHandle(any()) } returns false // Skip this step
        coEvery { secondStep.canHandle(any()) } returns true // This step will be executed
        coEvery { secondStep.execute(any()) } returns Output(Status.CONTINUE, Input("Step Executed", requestContext, mutableMapOf()))

        // Execute the steps
        runBlocking {
            stepExecutor.execute(testInput)
            // Verify first step was skipped
            coVerify(exactly = 0) { firstStep.execute(any()) }
            // Verify second step was executed
            coVerify { secondStep.execute(any()) }
        }
    }

    @Test
    fun `should execute first step that can handle input`() {
        val firstStep = mockk<Step>()
        val secondStep = mockk<Step>()

        val stepFactory = mockk<StepFactory> {
            every { getStep(any()) } returns firstStep andThen secondStep
        }

        val stepExecutor = StepExecutor(stepFactory).seq().step(firstStep).step(secondStep).end()
        val requestContext: RequestContext = mockk<RequestContext>()

        coEvery { firstStep.canHandle(any()) } returns true // This step will be executed
        coEvery { secondStep.canHandle(any()) } returns true // This step should not be reached
        coEvery { requestContext.conversationId } returns "conversationId"
        coEvery { requestContext.tenantId } returns "tenantId"
        coEvery { requestContext.turnId } returns "turnId"
        coEvery { firstStep.execute(any()) } returns Output(Status.CONTINUE, Input("Step Executed", requestContext, mutableMapOf()))
        coEvery { firstStep.execute(any()) } returns Output(Status.CONTINUE, Input("Step Executed", requestContext, mutableMapOf()))

        runBlocking {
            stepExecutor.execute(testInput)
            // Verify only the first step was executed
            coVerify { firstStep.execute(any()) }
            coVerify(exactly = 1) { secondStep.execute(any()) }
        }
    }

    @Test
    fun `should handle no steps available`() {
        val stepFactory = mockk<StepFactory>()
        // No steps are added
        val stepExecutor = StepExecutor(stepFactory).seq().end()

        runBlocking {
            val result = stepExecutor.execute(testInput)

            // Verify that output status is CONTINUE when no steps are available
            assertEquals(Status.CONTINUE, result.status)
        }
    }



    private fun createInput(): Input {
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
        return input
    }

}
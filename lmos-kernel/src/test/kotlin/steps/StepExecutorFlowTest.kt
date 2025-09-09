/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.telekom.lmos.platform.assistants.steps

import org.eclipse.lmos.kernel.steps.*
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

class StepExecutorFlowTest {

    @Test
    fun `test break`(): Unit = runBlocking {
        val stepFactory = AStepFactory()
        val stepExecutor = StepExecutor(stepFactory, null)
        val result = stepExecutor.seq()
            .step<AStep>()
            .step<BreakingStep>()
            .step<BStep>()
            .end().execute(testInput)

        val aStep = stepFactory.getStep(AStep::class.java) as AStep
        val bStep = stepFactory.getStep(BStep::class.java) as BStep

        Assertions.assertThat(aStep.executed).isTrue()
        Assertions.assertThat(bStep.executed).isFalse()
        Assertions.assertThat(result).isEqualTo(Output(Status.BREAK, testInput))
    }

    @Test
    fun `test AbstractProcessingSteps are still be executed after BREAK`(): Unit = runBlocking {
        val stepFactory = AStepFactory()
        val stepExecutor = StepExecutor(stepFactory, null)
        val result = stepExecutor.seq()
            .step<AStep>()
            .step<BreakingStep>()
            .step<AlwaysStep>()
            .step<BStep>()
            .end().execute(testInput)

        val aStep = stepFactory.getStep(AStep::class.java) as AStep
        val bStep = stepFactory.getStep(BStep::class.java) as BStep
        val alwaysStep = stepFactory.getStep(AlwaysStep::class.java) as AlwaysStep

        Assertions.assertThat(aStep.executed).isTrue()
        Assertions.assertThat(alwaysStep.executed).isTrue()
        Assertions.assertThat(bStep.executed).isFalse()
    }

    @Test
    fun `test sub_steps do not break flow`(): Unit = runBlocking {
        val stepFactory = AStepFactory()
        val stepExecutor = StepExecutor(stepFactory, null)
        val result = stepExecutor.seq()
            .step<AStep>()
            .step<StepWithSubSteps>()
            .step<AlwaysStep>()
            .step<BStep>()
            .end().execute(testInput)

        val aStep = stepFactory.getStep(AStep::class.java) as AStep
        val bStep = stepFactory.getStep(BStep::class.java) as BStep
        val alwaysStep = stepFactory.getStep(AlwaysStep::class.java) as AlwaysStep

        Assertions.assertThat(aStep.executed).isTrue()
        Assertions.assertThat(alwaysStep.executed).isTrue()
        Assertions.assertThat(bStep.executed).isTrue()
    }

    @Test
    fun `test broken sub_steps do break flow`(): Unit = runBlocking {
        val stepFactory = AStepFactory()
        val stepExecutor = StepExecutor(stepFactory, null)
        val result = stepExecutor.seq()
            .step<AStep>()
            .step<StepWithBrokenSubSteps>()
            .step<AlwaysStep>()
            .step<BStep>()
            .end().execute(testInput)

        val aStep = stepFactory.getStep(AStep::class.java) as AStep
        val bStep = stepFactory.getStep(BStep::class.java) as BStep
        val alwaysStep = stepFactory.getStep(AlwaysStep::class.java) as AlwaysStep

        Assertions.assertThat(aStep.executed).isTrue()
        Assertions.assertThat(alwaysStep.executed).isTrue()
        Assertions.assertThat(bStep.executed).isFalse()
    }

    @Test
    fun `custom step in the step sequence`(): Unit = runBlocking {
        val stepFactory = AStepFactory()
        val stepExecutor = StepExecutor(stepFactory, null)

        var executed = false
        val step = object : AbstractStep() {
            override suspend fun executeInternal(input: Input): Output {
                executed = true
                return Output(Status.CONTINUE, input)
            }
        }

        stepExecutor.seq()
            .step(step)
            .end().execute(testInput)

        Assertions.assertThat(executed).isTrue()
    }

    @Test
    fun `test step in the step sequence along with other`(): Unit = runBlocking {
        val stepFactory = AStepFactory()
        val stepExecutor = StepExecutor(stepFactory, null)

        var executed = false
        val step = object : AbstractStep() {
            override suspend fun executeInternal(input: Input): Output {
                executed = true
                return Output(Status.CONTINUE, input)
            }
        }

        val result = stepExecutor.seq()
            .step<AStep>()
            .step(step)
            .end().execute(testInput)

        val aStep = stepFactory.getStep(AStep::class.java) as AStep

        Assertions.assertThat(aStep.executed).isTrue()
        Assertions.assertThat(executed).isTrue()
        Assertions.assertThat(result).isEqualTo(Output(Status.CONTINUE, testInput))
    }

    @Test
    fun `test step skips if canHandle return false`(): Unit = runBlocking {
        val stepFactory = AStepFactory()
        val stepExecutor = StepExecutor(stepFactory, null)
        val result = stepExecutor.seq()
            .step<ASkipStep>()
            .step<AlwaysStep>()
            .step<BSkipStep>()
            .step<BStep>()
            .end().execute(testInput)

        val aSkipStep = stepFactory.getStep(ASkipStep::class.java) as ASkipStep
        val bStep = stepFactory.getStep(BStep::class.java) as BStep
        val alwaysStep = stepFactory.getStep(AlwaysStep::class.java) as AlwaysStep
        val bSkipStep = stepFactory.getStep(BSkipStep::class.java) as BSkipStep

        Assertions.assertThat(aSkipStep.executed).isFalse()
        Assertions.assertThat(alwaysStep.executed).isTrue()
        Assertions.assertThat(bStep.executed).isTrue()
        Assertions.assertThat(bSkipStep.executed).isFalse()
    }
}

/**
 * Test Data
 */
val testInput = Input(
    content = "Hello",
    RequestContext(
        "conversationID",
        "turnId",
        "natcoCode",
        RequestStatus.ONGOING
    ),
    mutableMapOf()
)

class AStepFactory : StepFactory {
    private val steps = mapOf(
        AStep::class.java to AStep(),
        BStep::class.java to BStep(),
        BreakingStep::class.java to BreakingStep(),
        StepWithSubSteps::class.java to StepWithSubSteps(),
        StepWithBrokenSubSteps::class.java to StepWithBrokenSubSteps(),
        AlwaysStep::class.java to AlwaysStep(),
        AbstractProcessingStep::class.java to AlwaysStep(),
        ASkipStep::class.java to ASkipStep(),
        BSkipStep::class.java to BSkipStep()
    )

    override fun getStep(stepClass: Class<out Step>) = steps[stepClass] ?: error("Undefined $stepClass!!")
}

class AStep : AbstractStep() {
    var executed = false

    override suspend fun executeInternal(input: Input): Output {
        executed = true
        return Output(Status.CONTINUE, input)
    }
}

class BStep : AbstractStep() {
    var executed = false

    override suspend fun executeInternal(input: Input): Output {
        executed = true
        return Output(Status.CONTINUE, input)
    }
}

class BreakingStep : AbstractStep() {
    override suspend fun executeInternal(input: Input) = Output(Status.BREAK, input)
}

class AlwaysStep : AbstractProcessingStep() {
    var executed = false

    override suspend fun executeInternal(input: Input): Output {
        executed = true
        return Output(Status.CONTINUE, input)
    }
}

class StepWithSubSteps : AbstractStep() {
    var executed = false

    override suspend fun executeInternal(input: Input): Output {
        executed = true
        val stepFactory = AStepFactory()
        val stepExecutor = StepExecutor(stepFactory, null)
        return stepExecutor.seq()
            .step<AStep>()
            .end().execute(testInput)
    }
}

class StepWithBrokenSubSteps : AbstractStep() {
    var executed = false

    override suspend fun executeInternal(input: Input): Output {
        executed = true
        val stepFactory = AStepFactory()
        val stepExecutor = StepExecutor(stepFactory, null)
        return stepExecutor.seq()
            .step<BreakingStep>()
            .end().execute(testInput)
    }
}

class ASkipStep : AbstractStep() {
    var executed = false

    override suspend fun executeInternal(input: Input): Output {
        executed = true
        return Output(Status.CONTINUE, input)
    }

    override suspend fun canHandle(input: Input): Boolean = false
}

class BSkipStep : AbstractStep() {
    var executed = false

    override suspend fun executeInternal(input: Input): Output {
        executed = true
        return Output(Status.CONTINUE, input)
    }

    override suspend fun canHandle(input: Input): Boolean = false
}
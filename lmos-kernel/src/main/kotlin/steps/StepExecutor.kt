/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.kernel.steps

import org.eclipse.lmos.kernel.observe.NoOpStepObserver
import org.eclipse.lmos.kernel.observe.NoOpStepObserverFactory
import org.eclipse.lmos.kernel.observe.ObservationContext
import org.eclipse.lmos.kernel.observe.ObservationInput
import org.eclipse.lmos.kernel.observe.StepObserver
import org.eclipse.lmos.kernel.observe.StepObserverFactory
import io.micrometer.core.instrument.MeterRegistry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.slf4j.MDCContext
import kotlinx.coroutines.withContext
import org.slf4j.MDC


class StepExecutor(
    private val stepFactory: StepFactory,
    private val meterRegistry: MeterRegistry? = null,
    private val stepObserverFactory: StepObserverFactory = NoOpStepObserverFactory()
) : AbstractStep() {

    private val steps = mutableListOf<Step>()
    private var stepObserver: StepObserver = NoOpStepObserver()

    /**
     * Allows for parallel execution of sequences and their steps.
     *
     * For details, refer to [ParallelStepExecutor].
     *
     * @param timeout Time in milliseconds after which execution of steps in parallel blocks will be timed out.
 *                    Default is 2 minutes.
     *
     */
    fun parallel(timeout: Long? = 120_000): ParallelBuilder = ParallelStepExecutor(stepFactory, meterRegistry, stepObserverFactory, steps, timeout).parallel()

    fun seq(): SeqStepBuilder {
        return SeqStepBuilder(StepExecutor(stepFactory, meterRegistry)) // TODO
    }

    fun seqWithObservation(origin: String): SeqStepBuilder = seqWithObservation(stepObserverFactory.create(ObservationContext(origin)))

    fun seqWithObservation(stepObserver: StepObserver): SeqStepBuilder {
        var stepExecutor = StepExecutor(stepFactory, meterRegistry)
        stepExecutor.stepObserver = stepObserver
        return SeqStepBuilder(stepExecutor)
    }

    override suspend fun executeInternal(input: Input): Output = stepObserver.observeSteps(ObservationInput(input)) { executeSteps(input, steps) }

    private suspend fun executeSteps(input: Input, steps: List<Step>): Output {
        val eligibleSteps = steps.asFlow()
            .dropWhile { !it.canHandle(input) }.toList() // Skip steps until we find an eligible one

        if (eligibleSteps.isEmpty()) return Output(Status.CONTINUE, input)
        val currentStep = eligibleSteps.first()
        val output = try {
            logStepName(currentStep) {
                stepObserver.observe(ObservationInput(input, eligibleSteps.first()::class.java)) {
                    Measure(currentStep, meterRegistry).execute(input)
                }
            }
        } catch (ex: Exception) {
            Output(Status.BREAK, input, ex)
        }

        val nextSteps = when (output.status) {
            Status.BREAK -> eligibleSteps.drop(1).filterIsInstance<AbstractProcessingStep>()
            else -> eligibleSteps.drop(1)
        }

        if (nextSteps.isEmpty()) return output
        return executeSteps(output.toInput(), nextSteps)
    }

    private suspend fun <T> logStepName(step: Step, block: suspend CoroutineScope.() -> T): T {
        val stepName = step::class.simpleName.toString()
        val currentContext = (MDC.getCopyOfContextMap() ?: emptyMap()) + ("step" to stepName)
        return withContext(MDCContext(currentContext), block)
    }

    inner class SeqStepBuilder(private val stepExecutor: StepExecutor) {

        fun step(step: Class<out Step>): SeqStepBuilder {
            stepExecutor.steps.add(stepFactory.getStep(step))
            return this
        }

        fun step(step: Step): SeqStepBuilder {
            stepExecutor.steps.add(step)
            return this
        }

        fun end(): StepExecutor = stepExecutor

    }
}

inline fun <reified T : Step> StepExecutor.SeqStepBuilder.step() = step(T::class.java)

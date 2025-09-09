/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.kernel.steps

import org.eclipse.lmos.kernel.METRIC_KEY_STEP_EXECUTOR
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import java.time.Duration
import kotlin.time.measureTime
import kotlin.time.toJavaDuration

//TODO: to be modified with metrics recording design
class Measure(private val step: Step, private val meterRegistry: MeterRegistry?=null) : AbstractStep() {

    override suspend fun executeInternal(input: Input): Output {
        val output: Output
        val time = measureTime {
            output = Log(step).execute(input)
        }
        timer(step)?.record(time.toJavaDuration())

        return output
    }

    /**
     * Creates a metrics timer with given name with preconfigured parameters.
     */
    private fun timer(step: Step): Timer? = if (meterRegistry != null) Timer.builder(METRIC_KEY_STEP_EXECUTOR)
            .tags("step", step.javaClass.simpleName)
            .distributionStatisticExpiry(Duration.ofMinutes(5))
            .distributionStatisticBufferLength(50) //limit memory usage
            .publishPercentiles(0.5, 0.95)
            .percentilePrecision(2)
            .register(meterRegistry) else null
}

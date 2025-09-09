/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.kernel.observe

import org.eclipse.lmos.kernel.steps.Input
import org.eclipse.lmos.kernel.steps.Output
import org.eclipse.lmos.kernel.steps.Step

interface StepObserver {
    suspend fun observeSteps(observationInput: ObservationInput, action: suspend () -> Output): Output
    suspend fun observe(observationInput: ObservationInput, action: suspend () -> Output): Output
}

data class ObservationInput(val input: Input?, val stepClass: Class<out Step>, val agentName: String) {
    constructor(input: Input) : this(input, Step::class.java, "")
    constructor(input: Input, agentName: String) : this(input, Step::class.java, agentName)
    constructor(input: Input, stepClass: Class<out Step>) : this(input, stepClass, "")
}

class NoOpStepObserver : StepObserver {
    override suspend fun observeSteps(observationInput: ObservationInput, action: suspend () -> Output): Output = action()

    override suspend fun observe(observationInput: ObservationInput, action: suspend () -> Output): Output = action()
}

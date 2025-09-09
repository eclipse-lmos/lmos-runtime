/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.kernel.steps


fun interface SelfEvaluator {
    suspend fun evaluate(input: Input): Int
}

class SelfEvaluate(private val selfEvaluator: SelfEvaluator) : AbstractStep() {

    companion object {
        const val SELF_EVALUATION = "self_evaluation"
    }

    override suspend fun executeInternal(input: Input): Output {
        val evaluation = selfEvaluator.evaluate(input)
        input.stepContext[SELF_EVALUATION] = evaluation
        return Output(Status.CONTINUE, input)
    }
}
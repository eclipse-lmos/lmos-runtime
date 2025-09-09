/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.kernel.steps


interface Pseudonymizer {
    fun anonymize(input: Input): String

    fun deanonymize(output: Output): String
}
class Pseudonymization(private val pseudonymizer: Pseudonymizer, private val step: Step) : AbstractStep() {

    override suspend fun executeInternal(input: Input): Output {

        val anonymizedContent = pseudonymizer.anonymize(input)

        val output = step.execute(Input(anonymizedContent, input.requestContext, input.stepContext))

        return Output(pseudonymizer.deanonymize(output), input.requestContext, Status.CONTINUE, input.stepContext)
    }

}
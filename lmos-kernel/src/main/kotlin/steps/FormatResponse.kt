/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.kernel.steps

fun interface ResponseFormatter {
    fun format(input: Input): String
}

class FormatResponse : AbstractStep() {

    override suspend fun executeInternal(input: Input): Output {
        val output = input.stepContext["ACCEPTED_OUTPUTS"]
        return Output(Status.CONTINUE, input)
    }
}

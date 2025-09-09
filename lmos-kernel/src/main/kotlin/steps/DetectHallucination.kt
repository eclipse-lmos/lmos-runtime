/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.kernel.steps

fun interface HallucinationDetector {

    fun detect(input: Input): Boolean
}

class DetectHallucination(private val hallucinationDetector: HallucinationDetector) : AbstractStep() {

    override suspend fun executeInternal(input: Input): Output {
        TODO("Not yet implemented")
    }

}
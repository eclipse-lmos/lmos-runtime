/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.kernel.router

import org.eclipse.lmos.kernel.steps.Input
import kotlin.random.Random

data class PercentageBasedRouting(
    val type: String = PercentageBasedRouting::class.simpleName.orEmpty(),
    val allocations: List<Allocation>
) : RoutingStrategy {

    init {
        validatePercentage()
    }

    private fun validatePercentage() {
        val totalPercentage = allocations.sumOf { it.percentage }
        require(totalPercentage == 100) {
            "The sum of all Split percentages must be 100%, but it is $totalPercentage%."
        }
    }

    override fun resolveModel(input: Input?): String? {
        val cumulativeSplit = allocations.runningFold(0) { acc, split -> acc + split.percentage }.drop(1)
        val randomValue = Random.nextInt(1, cumulativeSplit.last())
        return allocations.getOrNull(cumulativeSplit.indexOfFirst { it >= randomValue })?.languageModel
    }
}

data class Allocation(val languageModel: String, val percentage: Int)


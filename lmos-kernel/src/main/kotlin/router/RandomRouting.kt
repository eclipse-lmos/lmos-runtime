/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.kernel.router

import org.eclipse.lmos.kernel.steps.Input

data class RandomRouting(
    val type: String = RandomRouting::class.simpleName.orEmpty(),
    val models: List<String>
) : RoutingStrategy {
    override fun resolveModel(input: Input?): String {
        models.random().let { return it }
    }
}
/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.kernel.router

import org.eclipse.lmos.kernel.steps.Input

// Below code belongs to application layer, it should move to ia-platform along with Tenant.
interface RoutingStrategy {
    fun resolveModel(input: Input? = null): String?
}


/**
Spring Boot does not have native support for binding a list of polymorphic types like TrafficStrategy.
Once implemented, we can replace strategy: PercentageBasedRouting with strategy: RoutingStrategy.
TODO: Enable Spring's binding of polymorphic types.
 */

data class TrafficRouting(
    val step: String,
    val strategy: RoutingStrategy
)








/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.runtime.graphql.service.inbound.subscription

import com.expediagroup.graphql.generator.annotations.GraphQLDescription
import com.expediagroup.graphql.server.operations.Query
import org.springframework.stereotype.Component

/**
 * Query to get the list of agents provided by a service.
 */
@Component
class ChatQuery : Query {
    @GraphQLDescription("The single agent interface provided by the lmos-runtime")
    fun agent(): Agents = Agents(listOf("lmos-runtime"))
}

data class Agents(
    val names: List<String>,
)

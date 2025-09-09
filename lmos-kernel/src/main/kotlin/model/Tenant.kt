/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.kernel.model

import org.eclipse.lmos.kernel.router.TrafficRouting

data class Tenant(
    val tenantId: String = "DEFAULT",
    val defaultLanguage: String = "en",
    val services: List<TenantService> = listOf(TenantService("NA", "NA", "NA")),
    val languageModel: String = "openai",
    val trafficRouting: List<TrafficRouting> = emptyList(),
)

data class TenantService(
    val id: String,
    val url: String,
    val version: String
)

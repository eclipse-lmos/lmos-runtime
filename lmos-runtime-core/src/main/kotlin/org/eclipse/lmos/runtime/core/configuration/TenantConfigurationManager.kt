/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.runtime.core.configuration

interface TenantConfigurationManager {
    fun getConfiguration(tenantId: String): TenantConfig?

    fun getAllConfigurations(): List<TenantConfig>
}

data class TenantConfig(
    val id: String,
    val displayName: String?,
)

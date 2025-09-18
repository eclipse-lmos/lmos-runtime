/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.runtime.configuration

import org.eclipse.lmos.runtime.core.configuration.TenantConfig
import org.eclipse.lmos.runtime.core.configuration.TenantConfigurationManager
import org.eclipse.lmos.runtime.properties.RuntimeProperties
import org.springframework.stereotype.Component

@Component
class PropertiesBasedTenantConfigurationManager(
    private val runtimeProperties: RuntimeProperties,
) : TenantConfigurationManager {
    override fun getConfiguration(tenantId: String): TenantConfig? = runtimeProperties.tenants.find { tenant -> tenant.id == tenantId }

    override fun getAllConfigurations(): List<TenantConfig> = runtimeProperties.tenants
}

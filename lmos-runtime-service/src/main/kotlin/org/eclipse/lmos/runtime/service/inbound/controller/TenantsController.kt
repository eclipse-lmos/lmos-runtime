/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.runtime.service.inbound.controller

import org.eclipse.lmos.runtime.core.configuration.TenantConfig
import org.eclipse.lmos.runtime.core.configuration.TenantConfigurationManager
import org.eclipse.lmos.runtime.service.constants.LmosServiceConstants.Endpoints.BASE_PATH
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(BASE_PATH)
class TenantsController(
    private val tenantConfigurationManager: TenantConfigurationManager,
) {
    @GetMapping("/tenants-config")
    fun getAllTenants(): List<TenantConfig> = tenantConfigurationManager.getAllConfigurations()

    @GetMapping("/tenants-config/{tenantId}")
    fun getTenant(
        @PathVariable tenantId: String,
    ): ResponseEntity<TenantConfig> {
        val config = tenantConfigurationManager.getConfiguration(tenantId)
        return if (config != null) ResponseEntity.ok(config) else ResponseEntity.notFound().build()
    }
}

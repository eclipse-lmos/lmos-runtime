/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.runtime.service.inbound.controller

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.junit5.MockKExtension
import org.eclipse.lmos.runtime.core.configuration.TenantConfig
import org.eclipse.lmos.runtime.core.configuration.TenantConfigurationManager
import org.eclipse.lmos.runtime.service.constants.LmosServiceConstants
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.test.web.reactive.server.WebTestClient

@ExtendWith(MockKExtension::class)
@WebFluxTest(TenantsController::class)
@Import(TenantsControllerTest.MockConfig::class)
class TenantsControllerTest {
    @Autowired
    lateinit var webTestClient: WebTestClient

    @Autowired
    lateinit var tenantConfigurationManager: TenantConfigurationManager

    @BeforeEach
    fun setUp() {
        clearAllMocks()
    }

    @Test
    fun `should return all tenants`() {
        val tenants =
            listOf(
                TenantConfig("t1", "Tenant 1"),
                TenantConfig("t2", "Tenant 2"),
            )
        every { tenantConfigurationManager.getAllConfigurations() } returns tenants

        webTestClient
            .get()
            .uri(LmosServiceConstants.Endpoints.BASE_PATH + "/tenants-config")
            .exchange()
            .expectStatus()
            .isOk
            .expectBodyList(TenantConfig::class.java)
            .hasSize(2)
    }

    @Test
    fun `should return single tenant by id`() {
        val tenant = TenantConfig("t1", "Tenant 1")
        every { tenantConfigurationManager.getConfiguration("t1") } returns tenant

        webTestClient
            .get()
            .uri(LmosServiceConstants.Endpoints.BASE_PATH + "/tenants-config/t1")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody(TenantConfig::class.java)
            .isEqualTo(tenant)
    }

    @Test
    fun `should return 404 when tenant by id is not found`() {
        every { tenantConfigurationManager.getConfiguration("notfound") } returns null

        webTestClient
            .get()
            .uri(LmosServiceConstants.Endpoints.BASE_PATH + "/tenants-config/notfound")
            .exchange()
            .expectStatus()
            .isNotFound
    }

    @Test
    fun `should return empty list when no tenants exist`() {
        every { tenantConfigurationManager.getAllConfigurations() } returns emptyList()

        webTestClient
            .get()
            .uri(LmosServiceConstants.Endpoints.BASE_PATH + "/tenants-config")
            .exchange()
            .expectStatus()
            .isOk
            .expectBodyList(TenantConfig::class.java)
            .hasSize(0)
    }

    @TestConfiguration
    class MockConfig {
        @Bean
        fun tenantConfigurationManager(): TenantConfigurationManager = io.mockk.mockk()
    }
}

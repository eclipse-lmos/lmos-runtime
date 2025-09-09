/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.telekom.lmos.platform.assistants.tenant

import org.eclipse.lmos.kernel.Failure
import org.eclipse.lmos.kernel.Success
import org.eclipse.lmos.kernel.model.Tenant
import org.eclipse.lmos.kernel.tenant.CoroutineTenantProvider
import org.eclipse.lmos.kernel.tenant.MissingTenantException
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CoroutineTenantProviderTest {
    private val tenantProvider = CoroutineTenantProvider()
    val tenant = Tenant(
        "de",
        "ENGLISH",
        listOf(),
        "abc"
    )

    @Test
    fun should_provide_tenant_successfully(){
        runBlocking {
            tenantProvider.setTenant(tenant){
                assertEquals(Success(tenant), tenantProvider.provideTenant())
            }
        }
    }

    @Test
    fun should_throw_missing_tenant_exception(){
        runBlocking {
            assertTrue { tenantProvider.provideTenant() is Failure<MissingTenantException> }
        }
    }
}
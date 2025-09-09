/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.kernel.tenant

import org.eclipse.lmos.kernel.failWith
import org.eclipse.lmos.kernel.model.Tenant
import org.eclipse.lmos.kernel.result
import kotlinx.coroutines.asContextElement
import kotlinx.coroutines.ensurePresent
import kotlinx.coroutines.withContext

/**
 * Provides the current Tenant id stored in the Kotlin Coroutine.
 */
class CoroutineTenantProvider : TenantProvider {

    companion object {
        private val tenantLocal = ThreadLocal<Tenant>()
    }

    override suspend fun provideTenant() = result<Tenant, MissingTenantException> {
        try {
            tenantLocal.ensurePresent()
        } catch (ex: Exception) {
            failWith { MissingTenantException(ex) }
        }
        tenantLocal.get() ?: failWith { MissingTenantException() }
    }

    /**
     * Sets the tenant for the current Coroutine context.
     */
    override suspend fun <T> setTenant(tenant: Tenant, fn: suspend () -> T): T = withContext(tenantLocal.asContextElement(value = tenant)) {
            try {
                fn()
            } finally {
                tenantLocal.remove()
            }
        }
}


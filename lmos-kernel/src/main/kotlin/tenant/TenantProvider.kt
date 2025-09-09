/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.kernel.tenant

import org.eclipse.lmos.kernel.ConfigurationException
import org.eclipse.lmos.kernel.Result
import org.eclipse.lmos.kernel.model.Tenant

/**
 * Provides the current Tenant id.
 */
interface TenantProvider {
    suspend fun provideTenant(): Result<Tenant, MissingTenantException>

    suspend fun <T> setTenant(tenant: Tenant, fn: suspend () -> T): T
}

class MissingTenantException(cause: Exception? = null) : ConfigurationException("The tenant configuration is missing from the context!", cause)

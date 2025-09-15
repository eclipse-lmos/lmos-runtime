/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.runtime.core.cache

interface TenantAwareCache<V : Any> {
    fun save(
        tenantId: String,
        prefix: String,
        key: String,
        value: V,
    )

    fun save(
        tenantId: String,
        prefix: String,
        key: String,
        value: V,
        timeout: Long,
    )

    fun get(
        tenantId: String,
        prefix: String,
        key: String,
    ): V?

    fun delete(
        tenantId: String,
        prefix: String,
        key: String,
    )
}

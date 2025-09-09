/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.kernel.impl.model

import org.eclipse.lmos.kernel.getOrThrow
import org.eclipse.lmos.kernel.model.LanguageModelExecutor
import org.eclipse.lmos.kernel.tenant.TenantProvider
import io.micrometer.core.instrument.MeterRegistry

class MultiTenantLanguageModelExecutor(
    private val tenantProvider: TenantProvider,
    meterRegistry: MeterRegistry?
) : LanguageModelExecutor(meterRegistry) {
    override suspend fun <T> setLanguageModel(model: String, fn: suspend () -> T): T = fn()
    override suspend fun getLanguageModelName(): String {
        val tenant = tenantProvider.provideTenant().getOrThrow()
        return tenant.languageModel
    }
}
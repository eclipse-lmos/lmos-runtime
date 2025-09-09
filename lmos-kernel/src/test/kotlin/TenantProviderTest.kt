/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.telekom.lmos.platform.assistants

import org.eclipse.lmos.kernel.getOrNull
import org.eclipse.lmos.kernel.model.Tenant
import org.eclipse.lmos.kernel.tenant.CoroutineTenantProvider
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.util.concurrent.Executors


class TenantProviderTest {

    @Test
    fun `Test the CoroutineTenantProvider`(): Unit = runBlocking {
        val subject = CoroutineTenantProvider()
        val threadPoolA = Executors.newFixedThreadPool(1).asCoroutineDispatcher()
        val threadPoolB = Executors.newFixedThreadPool(1).asCoroutineDispatcher()

        runBlocking {
            subject.setTenant(
                Tenant(
                    "test01",
                    "german",
                    emptyList(),
                    "azure-openai",
                )
            ) {
                withContext(threadPoolA) {
                    assertNotNull(subject.provideTenant().getOrNull())
                    launch(threadPoolB) {
                        println(subject.provideTenant())
                        assertNotNull(subject.provideTenant().getOrNull())
                    }
                }
            }

            // test that tenant id is not available outside of scope.
            withContext(threadPoolA) {
                assertNull(subject.provideTenant().getOrNull())
            }
        }
        // test that tenant id is not available outside of scope.
        assertNull(subject.provideTenant().getOrNull())
    }
}

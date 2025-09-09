/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.telekom.lmos.platform.assistants.user


import org.eclipse.lmos.kernel.getOrNull
import org.eclipse.lmos.kernel.model.Tenant
import org.eclipse.lmos.kernel.user.CoroutineUserProvider
import org.eclipse.lmos.kernel.user.UserInformation
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.util.concurrent.Executors

class CoroutineTenantProviderTest {

    @Test
    fun `Test the CoroutineTenantProvider`(): Unit = runBlocking {
        val subject = CoroutineUserProvider()
        val threadPoolA = Executors.newFixedThreadPool(1).asCoroutineDispatcher()
        val threadPoolB = Executors.newFixedThreadPool(1).asCoroutineDispatcher()

        runBlocking {
            subject.setUser(
                UserInformation("user1", "token1")
            ) {
                withContext(threadPoolA) {
                    assertNotNull(subject.provideUser().getOrNull())
                    launch(threadPoolB) {
                        assertNotNull(subject.provideUser().getOrNull())
                    }
                }
            }

            // test that tenant id is not available outside of scope.
            withContext(threadPoolA) {
                assertNull(subject.provideUser().getOrNull())
            }
        }
        // test that tenant id is not available outside of scope.
        assertNull(subject.provideUser().getOrNull())
    }
}

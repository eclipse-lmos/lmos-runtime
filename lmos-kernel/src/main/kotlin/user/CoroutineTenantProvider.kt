/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.kernel.user

import org.eclipse.lmos.kernel.failWith
import org.eclipse.lmos.kernel.model.Tenant
import org.eclipse.lmos.kernel.result
import kotlinx.coroutines.asContextElement
import kotlinx.coroutines.ensurePresent
import kotlinx.coroutines.withContext

/**
 * Provides the current User stored in the Kotlin Coroutine.
 */
class CoroutineUserProvider : UserProvider {

    companion object {
        private val userLocal = ThreadLocal<UserInformation>()
    }

    override suspend fun provideUser() = result<UserInformation, MissingUserException> {
        try {
            userLocal.ensurePresent()
        } catch (ex: Exception) {
            failWith { MissingUserException(ex) }
        }
        userLocal.get() ?: failWith { MissingUserException() }
    }

    /**
     * Sets the user for the current Coroutine context.
     */
    override suspend fun <T> setUser(user: UserInformation, fn: suspend () -> T): T = withContext(userLocal.asContextElement(value = user)) {
            try {
                fn()
            } finally {
                userLocal.remove()
            }
        }
}


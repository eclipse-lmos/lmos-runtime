/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.kernel.user

import org.eclipse.lmos.kernel.ConfigurationException
import org.eclipse.lmos.kernel.Result
import org.eclipse.lmos.kernel.model.Tenant

/**
 * Provides the current User.
 */
interface UserProvider {
    suspend fun provideUser(): Result<UserInformation, MissingUserException>

    suspend fun <T> setUser(user: UserInformation, fn: suspend () -> T): T
}

class MissingUserException(cause: Exception? = null) : ConfigurationException("A user is missing in the context!", cause)

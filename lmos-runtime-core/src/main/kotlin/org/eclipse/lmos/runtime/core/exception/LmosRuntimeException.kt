/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.runtime.core.exception

import java.io.Serializable

data class ErrorMessage(val errorCode: String, val message: String?) : Serializable

sealed class LmosRuntimeException(msg: String? = null, cause: Exception? = null) : Exception(msg, cause)

class NoRoutingInfoFoundException(msg: String?, cause: Exception? = null) : LmosRuntimeException(msg, cause)

class InternalServerErrorException(msg: String?, cause: Exception? = null) : LmosRuntimeException(msg, cause)

class UnexpectedResponseException(msg: String?, cause: Exception? = null) : LmosRuntimeException(msg, cause)

class AgentClientException(msg: String?, cause: Exception? = null) : LmosRuntimeException(msg, cause)

class AgentNotFoundException(msg: String?, cause: Exception? = null) : LmosRuntimeException(msg, cause)

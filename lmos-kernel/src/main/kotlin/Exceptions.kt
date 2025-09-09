/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.kernel

/**
 * General exception thrown by the framework.
 */
sealed class OneAIException(msg: String? = null, cause: Exception? = null) : Exception(msg, cause)

/**
 * Indicates that a LLM has returned an invalid response.
 */
class HallucinationDetectedException(msg: String, cause: Exception? = null) : OneAIException(msg, cause)

/**
 * Indicates that the system has been misconfigured.
 */
open class ConfigurationException(msg: String, cause: Exception? = null) : OneAIException(msg, cause)

/**
 * Indicates that the client has supplied invalid input.
 */
sealed class InvalidClientInputException(msg: String, cause: Exception? = null) : OneAIException(msg, cause)

/**
 * Indicates that the client did not send a required parameter.
 */
class MissingParameterException(val parameter: String, cause: Exception? = null) : InvalidClientInputException("Required $parameter is missing!", cause)

/**
 * Indicates that the client provided an unknown tenant.
 */
class UnknownTenantException(tenant: String, cause: Exception? = null) : InvalidClientInputException("Unknown tenant $tenant provided!", cause)

/**
 * Indicates that the client input doesn't follow content management policy.
 */
class ContentPolicyViolationException(msg: String?= null, cause: Exception? = null) : InvalidClientInputException("Content policy violation $msg", cause)

/**
 * Indicates that the request is received from unknown channel.
 */
class UnknownChannelException(msg: String?= null, cause: Exception? = null) : InvalidClientInputException("Content policy violation $msg", cause)

/**
 * Indicates that the server is currently having issues progressing requests.
 */
class ServerException(msg: String? = null, cause: Exception? = null) : OneAIException(msg, cause)

/**
 * Indicates that an operation fails due to an authorization issue.
 */
sealed class AuthorizationException(msg: String? = null, cause: Exception? = null) : OneAIException(msg, cause)

/**
 * Indicates that the client does not have sufficient rights to execute a process.
 */
class PermissionDeniedException(msg: String? = null, cause: Exception? = null) : AuthorizationException(msg, cause)

/**
 * Indicates that the client needs tp provide authorization to execute a process.
 */
class AuthorizationRequiredException(msg: String? = null, cause: Exception? = null) : AuthorizationException(msg, cause)
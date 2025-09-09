/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.kernel.impl.functions

/**
 * Converts camel case to snake case.
 */
private val camelToSnakeCasePattern = "(?<=.)[A-Z]".toRegex()

fun String.camelToSnakeCase() = this.replace(camelToSnakeCasePattern, "_$0").lowercase()

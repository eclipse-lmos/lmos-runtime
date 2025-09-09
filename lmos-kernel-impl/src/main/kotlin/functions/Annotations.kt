/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.kernel.impl.functions

import org.springframework.stereotype.Service


/**
 * Annotation that tags classes as that define a set of LLMFunctions.
 */
@Retention(AnnotationRetention.RUNTIME)
@Service
annotation class LLMFunctions(val group: String)

/**
 * Annotation that can annotate Functions as LLMFunctions
 */
@Retention(AnnotationRetention.RUNTIME)
annotation class LLMFunction(val description: String, val sensitive: Boolean = false)

/**
 * Annotation that can annotate Functions as LLMFunctions
 */
@Retention(AnnotationRetention.RUNTIME)
annotation class LLMFunctionParam(val description: String, val enum: Array<String> = [])

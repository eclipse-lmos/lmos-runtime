/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.kernel.impl.functions

import org.eclipse.lmos.kernel.model.LLMFunction
import org.eclipse.lmos.kernel.model.LLMFunctionProvider

class SpringLLMFunctionProvider(
    private val functions: List<LLMFunction>
) : LLMFunctionProvider {

    override fun provideByGroup(functionGroup: String) = functions.filter { it.group == functionGroup }
}
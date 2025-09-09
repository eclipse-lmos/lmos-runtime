/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.kernel.services

import org.eclipse.lmos.kernel.steps.Input

interface TokenCountEstimator {

    fun estimate(input: Input): Int
}
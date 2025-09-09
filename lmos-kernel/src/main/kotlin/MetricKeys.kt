/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.kernel

/**
 * Contains a list of keys used for defining metrics.
 * Hint: Steps are automatically timed by the stepExecutor.
 */
const val METRIC_KEY_STEP_EXECUTOR = "oneai_step_executor"

// LANGUAGE_MODEL_EXECUTOR
const val METRIC_KEY_LANGUAGE_MODEL_EXECUTOR = "oneai.llm.executor"
const val CONTEXTUAL_KEY_LANGUAGE_MODEL_EXECUTOR = "executing-llm"

// PROMPT_COMPILER
const val METRIC_KEY_PROMPT_COMPILER = "prompt.compiler"
const val CONTEXTUAL_KEY_PROMPT_COMPILER = "compiling-prompt"


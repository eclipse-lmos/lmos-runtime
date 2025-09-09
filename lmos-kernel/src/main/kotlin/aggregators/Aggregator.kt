/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.kernel.aggregators

import org.eclipse.lmos.kernel.steps.Output
import org.eclipse.lmos.kernel.steps.StepExecutor

/**
 * Aggregator is an integral part of [StepExecutor]
 * and is used to process list of [Output] from parallel sequences of steps and combine them into a single output.
 *
 * In most cases, custom aggregators which implement this interface will need to be declared.
 * There is a default implementation [DefaultAggregator] which simply returns the output of the first sequence
 * which is executed in parallel.
 *
 * The underlying structure and usage will be as follows, for e.g.:
 *```
 * stepExecutor
 *      .parallel()
 *          .seq()
 *              .step<Step_1>()
 *              .step<Step_2>()
 *              .end()
 *          .seq()
 *              .step<Step_1>()
 *              .step<Step_3>()
 *              .end()
 *          .aggregate<DefaultAggregator>()
 *       .execute(input)
 *```
 * As seen above, The aggregator takes input list of 'n' number of outputs as there are sequences which need to be run
 * in parallel. Implementations of this interface will also need to override [aggregate] function and define their own
 * logic and rules to combine these outputs into a single output.
 *
 * It is worth noting that outputs are generated in order of the sequences. For e.g.: in the above example,
 * output from sequence with steps: Step_1 and Step_2 will always be present at 0 index of the list of outputs,
 * the following sequence with steps: Step_1 and Step_3 will always be at index 1 and so on.
 */
fun interface Aggregator {

    /**
     * Combines outputs of multiple sequences of steps executed in parallel with each other into a single output.
     *
     * @param outputs List containing outputs from multiple sequences of steps.
     *
     * @return A single instance of [Output].
     */
    suspend fun aggregate(outputs: List<Output>): Output
}

/**
 * A basic implementation of [Aggregator].
 *
 * It takes as input, list of outputs generated during parallel execution of sequences of steps
 * and returns the output of first sequence in order.
 */
class DefaultAggregator : Aggregator {

    /**
     * Returns the first [Output] in the input list of outputs.
     *
     * @param outputs List containing outputs from multiple sequences of steps.
     *
     * @return First output in the input list.
     */
    override suspend fun aggregate(outputs: List<Output>): Output = outputs.first()
}

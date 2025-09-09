/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.kernel.steps


import org.slf4j.LoggerFactory

class Log(private val step: Step) : AbstractProcessingStep() {


    private val log = LoggerFactory.getLogger(javaClass)

    override suspend fun executeInternal(input: Input): Output {
        // log input

        val conversationContext = input.requestContext

        log.debug("Input received for conversationId: ${conversationContext.conversationId}, tenantId: ${conversationContext.tenantId}, ${conversationContext.turnId}")

        val output = step.execute(input)

        log.debug("Output received for conversationId: ${conversationContext.conversationId}, tenantId: ${conversationContext.tenantId}, ${conversationContext.turnId}")

        return output
    }

}
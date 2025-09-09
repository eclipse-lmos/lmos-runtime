/*
 * SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.eclipse.lmos.kernel.impl.steps

import org.eclipse.lmos.kernel.steps.Step
import org.eclipse.lmos.kernel.steps.StepFactory
import org.springframework.beans.factory.config.ConfigurableBeanFactory

class SpringTaskFactory(private val beanFactory: ConfigurableBeanFactory) : StepFactory {
    override fun getStep(stepClass: Class<out Step>): Step = beanFactory.getBean(stepClass)
}
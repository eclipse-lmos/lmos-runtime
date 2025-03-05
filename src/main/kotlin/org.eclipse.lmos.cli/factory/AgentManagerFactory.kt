package org.eclipse.lmos.cli.factory

import org.eclipse.lmos.cli.credential.CredentialManagerType
import org.eclipse.lmos.cli.arc.ArcMacOSAgentManager
import org.eclipse.lmos.cli.agent.AgentManager
import org.eclipse.lmos.cli.arc.ArcWindowsAgentManager

class AgentManagerFactory {

    companion object {

        private val arcWindowsAgentManager = ArcWindowsAgentManager()
        private val arcMacOSAgentManager = ArcMacOSAgentManager()

        fun agentManager(): AgentManager {
            val os = getOS()
            return when(os) {
                CredentialManagerType.MAC -> arcMacOSAgentManager
                CredentialManagerType.WIN -> arcWindowsAgentManager
                CredentialManagerType.LINUX -> arcMacOSAgentManager
            }
        }
    }


}
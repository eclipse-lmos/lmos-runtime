package org.eclipse.lmos.cli.starter

import org.eclipse.lmos.cli.credential.CredentialManagerType
import org.eclipse.lmos.cli.factory.getOS

class AgentStarterFactory {
    fun getAgentStarter(): AgentStarter {
        val os = getOS()
        return when(os) {
            CredentialManagerType.MAC -> MacOSAgentStarter()
            CredentialManagerType.WIN -> WindowsAgentStarterWithENv()
            CredentialManagerType.LINUX -> MacOSAgentStarter()
            CredentialManagerType.DEFAULT -> MacOSAgentStarter()
        }
    }

}
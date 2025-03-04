package org.eclipse.lmos.cli.factory

import net.mamoe.yamlkt.Yaml
import org.eclipse.lmos.cli.constants.LmosCliConstants.CredentialManagerConstants.CREDENTIAL_CONFIG
import org.eclipse.lmos.cli.credential.CredentialManagerConfig
import org.eclipse.lmos.cli.credential.CredentialManagerType
import org.eclipse.lmos.cli.credential.WinCredentialManager
import org.eclipse.lmos.cli.credential.manager.CredentialManager
import org.eclipse.lmos.cli.credential.manager.DefaultCredentialManager
import org.eclipse.lmos.cli.credential.manager.LinuxCredentialManager
//import org.eclipse.lmos.cli.credential.manager.LinuxCred
import org.eclipse.lmos.cli.credential.manager.MacOSCredentialManager

class CredentialManagerFactory {

    fun getCredentialManager(): CredentialManager {
        var credentialManager: CredentialManager

        val credConfig = CREDENTIAL_CONFIG.readText()
        println("DEBUG: credConfig: $CREDENTIAL_CONFIG: $credConfig")
        if(credConfig.isEmpty()) {
            credentialManager = when (getOS()) {
                CredentialManagerType.MAC -> MacOSCredentialManager()
                CredentialManagerType.WIN -> WinCredentialManager()
                CredentialManagerType.LINUX -> LinuxCredentialManager()
                CredentialManagerType.DEFAULT -> DefaultCredentialManager()
            }
            if(!credentialManager.testCredentialManager()) {
                credentialManager = DefaultCredentialManager()
            }
            val credentialManagerConfig = CredentialManagerConfig(credentialManager.credentialManagerType())
            CREDENTIAL_CONFIG.writeText(Yaml.encodeToString(CredentialManagerConfig.serializer(), credentialManagerConfig))
        } else {
            val credentialManagerConfig = Yaml.decodeFromString(CredentialManagerConfig.serializer(), credConfig)
            credentialManager = when (credentialManagerConfig.type) {
                CredentialManagerType.MAC -> MacOSCredentialManager()
                CredentialManagerType.WIN -> WinCredentialManager()
                CredentialManagerType.LINUX -> LinuxCredentialManager()
                CredentialManagerType.DEFAULT -> DefaultCredentialManager()
            }
        }
        return credentialManager
    }



}

fun getOS(): CredentialManagerType {
    val osName = System.getProperty("os.name").lowercase()
    return when {
        osName.contains("win") -> CredentialManagerType.WIN
        osName.contains("nix") || osName.contains("nux") -> CredentialManagerType.LINUX
        osName.contains("mac") -> CredentialManagerType.MAC
        else -> CredentialManagerType.DEFAULT
    }
}

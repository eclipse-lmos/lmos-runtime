package org.eclipse.lmos.cli.factory

import org.eclipse.lmos.cli.credential.CredentialManagerType
import org.eclipse.lmos.cli.credential.manager.CredentialManager
import org.eclipse.lmos.cli.credential.manager.FileBasedCredentialManager
import org.slf4j.LoggerFactory


class CredentialManagerFactory {

    private val log = LoggerFactory.getLogger(CredentialManagerFactory::class.java)

    fun getCredentialManager(): CredentialManager {
        return FileBasedCredentialManager()
    }

}

fun getOS(): CredentialManagerType {
    val osName = System.getProperty("os.name").lowercase()
    return when {
        osName.contains("win") -> CredentialManagerType.WIN
        osName.contains("nix") || osName.contains("nux") -> CredentialManagerType.LINUX
        osName.contains("mac") -> CredentialManagerType.MAC
        else -> throw RuntimeException("Unsupported OS")
    }
}

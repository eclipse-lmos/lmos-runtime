package org.eclipse.lmos.cli

import org.eclipse.lmos.cli.constants.LmosCliConstants.AgentStarterConstants.AGENTS_REGISTRY
import org.eclipse.lmos.cli.constants.LmosCliConstants.AgentStarterConstants.AGENT_PROJECTS_DIRECTORY
import org.eclipse.lmos.cli.constants.LmosCliConstants.CredentialManagerConstants.CREDENTIAL_CONFIG
import org.eclipse.lmos.cli.constants.LmosCliConstants.CredentialManagerConstants.CREDENTIAL_DIRECTORY
import kotlin.io.path.createDirectories
import kotlin.io.path.notExists

//quarkus.native.image.initialize-at-run-time=org.eclipse.lmos.cli.LmosCli

class Initializer {

    fun initialize() {
        println("Initializing Lmos CLI")
        ensureDirectories()
        ensureConfigs()
    }

    private fun ensureConfigs() {
        if (!CREDENTIAL_CONFIG.exists()) {
            val createNewFile = CREDENTIAL_CONFIG.createNewFile()
            println("Cred file created: $createNewFile")
            CREDENTIAL_CONFIG.setWritable(true, true)
        }
    }

    private fun ensureDirectories() {
        if (AGENT_PROJECTS_DIRECTORY.notExists()) {
            AGENT_PROJECTS_DIRECTORY.createDirectories()
            println("Cred directory created: $AGENT_PROJECTS_DIRECTORY")
        }
        if (AGENTS_REGISTRY.notExists()) {
            AGENTS_REGISTRY.createDirectories()
            println("Cred directory created: $AGENTS_REGISTRY")
        }
        if (!CREDENTIAL_DIRECTORY.toFile().exists()) {
            val createDirectories = CREDENTIAL_DIRECTORY.createDirectories()
            println("Cred directory created: $createDirectories")
        }
    }
}
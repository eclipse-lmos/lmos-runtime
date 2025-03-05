package org.eclipse.lmos.cli

import jakarta.inject.Singleton
import org.eclipse.lmos.cli.constants.LmosCliConstants.AgentStarterConstants.AGENTS_REGISTRY
import org.eclipse.lmos.cli.constants.LmosCliConstants.AgentStarterConstants.AGENT_PROJECTS_DIRECTORY
import org.eclipse.lmos.cli.constants.LmosCliConstants.CredentialManagerConstants.CREDENTIAL_DIRECTORY
import org.eclipse.lmos.cli.constants.LmosCliConstants.LOG_DIR
import org.eclipse.lmos.cli.constants.LmosCliConstants.LOG_FILE_PATH
import org.slf4j.LoggerFactory
import java.nio.file.Files
import kotlin.io.path.createDirectories
import kotlin.io.path.createFile


@Singleton
class Initializer {

    private val log = LoggerFactory.getLogger(Initializer::class.java)

    fun initialize(): Int {
        ensureDirectories()
        setLogFilePath()
        return 0
    }

    private fun setLogFilePath() {
        System.setProperty("custom.logfile.path", LOG_FILE_PATH.toString());
        log.info("Logging initialized at {}", LOG_FILE_PATH.toString())
    }

    private fun ensureDirectories() {
        if (Files.notExists(LOG_DIR)) {
            LOG_DIR.createDirectories()
        }
        if (Files.notExists(LOG_FILE_PATH)) {
            LOG_FILE_PATH.createFile()
        }
        if (Files.notExists(AGENT_PROJECTS_DIRECTORY)) {
            AGENT_PROJECTS_DIRECTORY.createDirectories()
        }
        if (Files.notExists(AGENTS_REGISTRY)) {
            AGENTS_REGISTRY.createDirectories()
        }
        if (!CREDENTIAL_DIRECTORY.toFile().exists()) {
            CREDENTIAL_DIRECTORY.createDirectories()
        }
    }
}
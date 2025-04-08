package org.eclipse.lmos.cli.utils

import org.eclipse.microprofile.config.ConfigProvider

class EnvUtils {
    companion object {

        @JvmStatic
        fun getLmosCliSecretKey(): String {
            return ConfigProvider.getConfig().getValue("lmos.cli.secret.key", String::class.java)
                ?: throw IllegalStateException("LMOS_CLI_SECRET_KEY environment variable or lmos.cli.secret.key property is not set.")
        }
    }
}

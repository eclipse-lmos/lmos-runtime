package org.kinetiqx.prism.credentials.commands

import org.kinetiqx.prism.credentials.CredentialStore
import picocli.CommandLine
import java.util.concurrent.Callable

@CommandLine.Command(
    name = "list",
    description = ["List all credentials"],
)
class ListCommand : Callable<Int> {
    override fun call(): Int {
        val store = CredentialStore()
        val creds = store.listCredentials()
        if (creds.isEmpty()) {
            println("No credentials found")
        } else {
            println("Stored Credentials:")
            for (cred in creds) {
                println("ID: ${cred}")
            }
        }
        return 0
    }
} 

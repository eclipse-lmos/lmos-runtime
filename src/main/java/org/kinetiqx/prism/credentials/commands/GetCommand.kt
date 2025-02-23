package org.kinetiqx.prism.credentials.commands

import org.kinetiqx.prism.credentials.store.CredentialStore
import picocli.CommandLine
import java.util.concurrent.Callable

@CommandLine.Command(
    name = "get",
    description = ["Get a credential"],
)
class GetCommand : Callable<Int> {
    @CommandLine.Parameters(
        index = "0",
        description = ["ID of the credential"],
    )
    lateinit var id: String

    override fun call(): Int {
        val store = CredentialStore()
        val cred = store.getCredential(id)
        if (cred == null) {
            println("Credential not found: $id")
            return 1
        } else {
            println("ID: ${cred.id}")
            return 0
        }
    }
} 

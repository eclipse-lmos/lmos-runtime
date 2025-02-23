package org.kinetiqx.prism.credentials.commands

import org.kinetiqx.prism.credentials.Credential
import org.kinetiqx.prism.credentials.store.CredentialStore
import picocli.CommandLine
import java.util.concurrent.Callable

@CommandLine.Command(
    name = "update",
    description = ["Update a credential"],
)
class UpdateCommand : Callable<Int> {
    @CommandLine.Parameters(
        index = "0",
        description = ["ID of the credential"],
    )
    lateinit var id: String

    @CommandLine.Option(
        names = ["-u", "--username"],
        description = ["New username"],
        required = false,
    )
    var username: String? = null

    @CommandLine.Option(
        names = ["-p", "--password"],
        description = ["New password"],
        required = false,
    )
    var password: String? = null

    override fun call(): Int {
        val store = CredentialStore()
        val existingCred = store.getCredential(id)
        if (existingCred == null) {
            println("Credential not found: $id")
            return 1
        } else {
            val newPassword = password ?: existingCred.content
            val updatedCred = Credential(id, newPassword)
            store.updateCredential(updatedCred)
            println("Credential updated: $id")
            return 0
        }
    }
} 

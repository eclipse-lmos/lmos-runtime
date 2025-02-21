package org.kinetiqx.prism.credentials.commands

import org.kinetiqx.prism.credentials.CredentialStore
import picocli.CommandLine
import java.util.concurrent.Callable

@CommandLine.Command(
    name = "delete",
    description = ["Delete a credential or all credentials"],
)
class DeleteCommand : Callable<Int> {
    @CommandLine.Parameters(
        index = "0",
        description = ["ID of the credential (or 'all' to delete all credentials)"],
    )
    lateinit var id: String

    override fun call(): Int {
        val store = CredentialStore()
        if (id.equals("all", ignoreCase = true)) {
//            print("Are you sure you want to delete all credentials? (y/n): ")
            val response = "y"
//                readln()
            if (response.equals("y", ignoreCase = true)) {
                store.deleteAllCredentials()
                println("All credentials deleted.")
            } else {
                println("Operation cancelled.")
            }
        } else {
            val cred = store.getCredential(id)
            if (cred == null) {
                println("Credential not found: $id")
                return 1
            } else {
                store.deleteCredential(id)
                println("Deleted credential: $id")
            }
        }
        return 0
    }
} 

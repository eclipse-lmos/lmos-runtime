package org.kinetiqx.prism.credentials.commands

import org.kinetiqx.prism.credentials.Credential
import org.kinetiqx.prism.credentials.store.CredentialStore
import picocli.CommandLine
import java.util.concurrent.Callable

@CommandLine.Command(name = "add", description = ["Add a new credential"])
class AddCommand : Callable<Int> {
    @CommandLine.Parameters(index = "0", description = ["ID of the credential"])
    lateinit var id: String

    @CommandLine.Option(names = ["-u", "--username"], description = ["Username"], required = true)
    lateinit var username: String

    @CommandLine.Option(names = ["-p", "--password"], description = ["Password"], required = true)
    lateinit var password: String

    override fun call(): Int {
        val store = CredentialStore()
        val credential = Credential(id, password)
        store.addCredential(credential)
        println("Credential added: $id")
        return 0
    }
} 

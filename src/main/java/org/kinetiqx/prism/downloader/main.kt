import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import java.io.*
import java.net.HttpURLConnection
import java.net.URL

fun install(githubUrl: String) {

    val destinationFolder = File("/Users/bharatbhushan/Downloads/code-with-quarkus-cli/temp/")

    // Create the destination folder if it doesn't exist
    if (!destinationFolder.exists()) {
        println("Creating destination folder: ${destinationFolder.absolutePath}")
        if (!destinationFolder.mkdirs()) {
            println("Failed to create destsmination folder.")
            return
        }
    }

    val tempTarGzFile = File.createTempFile("download", ".tar.gz")
    println("Temporary tar.gz file: ${tempTarGzFile.absolutePath}")

    try {
        // Download the tar.gz file
        println("Downloading $githubUrl to ${tempTarGzFile.absolutePath}")
        downloadFile(githubUrl, tempTarGzFile)

        println("Downloaded file size: ${tempTarGzFile.length()} bytes")

        if (tempTarGzFile.length() == 0L) {
            println("Error: Downloaded file is empty. Please check the URL and try again.")
            return
        }

        // Extract the tar.gz file to the destination folder
        println("Extracting ${tempTarGzFile.absolutePath} to ${destinationFolder.absolutePath}")
        extractTarGz(tempTarGzFile, destinationFolder)
    } catch (e: Exception) {
        e.printStackTrace()
        println("Error processing file: ${e.localizedMessage}")
        return
    } finally {
        // Delete the temp tar.gz file
        if (tempTarGzFile.exists()) {
            tempTarGzFile.delete()
        }
    }

    // Optionally, make the extracted files executable
    makeFilesExecutable(destinationFolder)

    // Add the destination folder to the PATH
    addFolderToPath(destinationFolder)
}

fun downloadFile(githubUrl: String, outputFile: File) {
    val url = URL(githubUrl)
    val connection = url.openConnection() as HttpURLConnection
    connection.instanceFollowRedirects = true
    connection.connectTimeout = 10000
    connection.readTimeout = 10000
    connection.requestMethod = "GET"
    connection.doOutput = false

    val responseCode = connection.responseCode
    println("HTTP Response Code: $responseCode")

    if (responseCode != HttpURLConnection.HTTP_OK) {
        throw IOException("Failed to download file: HTTP response code $responseCode")
    }

    connection.inputStream.use { input ->
        FileOutputStream(outputFile).use { output ->
            input.copyTo(output)
        }
    }
}

fun extractTarGz(tarGzFile: File, destDir: File) {
    FileInputStream(tarGzFile).use { fis ->
        GzipCompressorInputStream(BufferedInputStream(fis)).use { gzis ->
            TarArchiveInputStream(gzis).use { tis ->
                var entry: TarArchiveEntry? = tis.nextTarEntry
                while (entry != null) {
                    val destPath = File(destDir, entry.name)
                    if (entry.isDirectory) {
                        if (!destPath.exists()) {
                            destPath.mkdirs()
                        }
                    } else {
                        // Ensure parent directories exist
                        destPath.parentFile?.mkdirs()
                        // Write file content
                        BufferedOutputStream(FileOutputStream(destPath)).use { out ->
                            tis.copyTo(out)
                        }
                    }
                    entry = tis.nextEntry
                }
            }
        }
    }
}

fun makeFilesExecutable(dir: File) {
    dir.walkTopDown().forEach { file ->
        if (file.isFile && !file.canExecute()) {
            println("Making file executable: ${file.absolutePath}")
            file.setExecutable(true)
        }
    }
}

fun addFolderToPath(folder: File) {
    val path = System.getenv("PATH") ?: ""
    val pathEntries = path.split(File.pathSeparator)

    if (folder.absolutePath in pathEntries) {
        println("The folder is already in PATH.")
        return
    }

    val shell = System.getenv("SHELL") ?: ""
    val home = System.getProperty("user.home")
    val shellConfigFile = when {
        shell.endsWith("zsh") -> File("$home/.zshrc")
        shell.endsWith("bash") -> File("$home/.bash_profile")
        else -> {
            println("Unsupported shell. Please add ${folder.absolutePath} to your PATH manually.")
            return
        }
    }

    val exportCommand = "export PATH=\"\$PATH:${folder.absolutePath}\""

    try {
        if (shellConfigFile.exists()) {
            val contents = shellConfigFile.readText()
            if (exportCommand in contents) {
                println("The PATH already contains the folder in ${shellConfigFile.absolutePath}")
            } else {
                println("Adding folder to PATH in ${shellConfigFile.absolutePath}")
                shellConfigFile.appendText("\n$exportCommand\n")
                println("Added to PATH. Please restart your terminal or run 'source ${shellConfigFile.absolutePath}'")
            }
        } else {
            println("Creating shell config file: ${shellConfigFile.absolutePath}")
            shellConfigFile.writeText("# Created by download script\n$exportCommand\n")
            println("Added to PATH. Please restart your terminal or run 'source ${shellConfigFile.absolutePath}'")
        }
    } catch (e: Exception) {
        e.printStackTrace()
        println("Failed to update PATH: ${e.localizedMessage}")
    }
}  
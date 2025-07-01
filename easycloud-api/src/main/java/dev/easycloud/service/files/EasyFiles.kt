package dev.easycloud.service.files

import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.nio.file.*
import java.nio.file.Files
import kotlin.io.path.exists

class EasyFiles {
    companion object {
        fun remove(path: Path) {
            path.takeIf { it.exists() }?.apply {
                return
            }

            try {
                Files.walk(path).use { pathStream ->
                    pathStream.sorted(Comparator.reverseOrder())
                        .map { obj: Path? -> obj!!.toFile() }
                        .forEach { obj: File? -> obj!!.delete() }
                }
            } catch (_: IOException) {
            }
        }

        fun download(url: String, output: Path) {
            val connection = (URL(url).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
            }

            if (connection.responseCode != 200) {
                throw IOException("Download failed: HTTP ${connection.responseCode}")
            }

            connection.inputStream.use { input ->
                FileOutputStream(output.toFile()).use { outputStream ->
                    val buffer = ByteArray(4096)
                    var bytesRead: Int
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        outputStream.write(buffer, 0, bytesRead)
                    }
                }
            }
        }
    }
}
package dev.easycloud.service.configuration

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dev.easycloud.service.configuration.resources.ConfigurationEntity
import org.yaml.snakeyaml.Yaml
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException
import java.nio.file.Path
import kotlin.io.path.exists

class Configurations {
    companion object {
        val yaml = Yaml()
        val gson = GsonBuilder().setPrettyPrinting().create()
        val gsonWithoutPrettyPrinting: Gson = GsonBuilder().create()

        private fun name(clazz: Class<*>): String {
            return clazz.getAnnotation(ConfigurationEntity::class.java)
                .takeIf { it != null }?.name
                ?: throw IllegalArgumentException("The class ${clazz.name} is not annotated with @FileEntity")
        }


        // writing methods
        fun writeRaw(path: Path, data: Any) {
            path.takeIf { it.exists() }?.apply {
                this.toFile().delete()
            }

            try {
                FileWriter(path.toFile().path).use { writer ->
                    this.gson.toJson(data, writer)
                }
            } catch (exception: IOException) {
                throw RuntimeException(exception)
            }

        }

        fun write(path: Path, data: Any) {
            this.writeRaw(path.resolve(name(data::class.java)), data)
        }

        fun writeIfNotExists(path: Path, data: Any) {
            path.resolve(this.name(data::class.java))
                .takeIf { !it.toFile().exists() }
                ?.apply {
                    write(path, data)
                }
        }

        // reading methods
        fun <T> readRaw(path: Path, clazz: Class<T?>?): T? {
            try {
                FileReader(path.toFile().path).use { reader -> return this.gson.fromJson(reader, clazz) }
            } catch (exception: IOException) {
                throw RuntimeException(exception)
            }
        }

        fun <T> read(path: Path, clazz: Class<T?>?): T? {
            return this.readRaw(path.resolve(name(clazz!!)), clazz)
        }
    }
}
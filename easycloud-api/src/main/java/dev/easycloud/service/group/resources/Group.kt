package dev.easycloud.service.group.resources

import dev.easycloud.service.platform.Platform
import dev.easycloud.service.property.Property
import java.nio.file.Path
import java.util.*

class Group(var enabled: Boolean, val name: String, val platform: Platform) {
    val properties = HashMap<String, Any>()

    fun insert(property: Property<*>, value: Any) {
        if (value is String || value is Int || value is Boolean ||
            value is Double || value is Float || value is Long ||
            value is Short || value is Byte || value is Char ||
            value is Class<*> || value is Enum<*> || value is Path || value is UUID
        ) {
            properties[property.key.lowercase()] = value
        } else {
            throw java.lang.IllegalArgumentException("Invalid property type: " + value.javaClass.getSimpleName())
        }
    }


    fun <T> read(property: Property<T>): T {
        val value = this.properties[property.key.lowercase()]
        var result: Any? = null

        property.className.equals("integer", ignoreCase = true).takeIf { it }?.let {
            result = (value as Double).toInt()
        }
        property.className.equals("string", ignoreCase = true).takeIf { it }?.let {
            result = value ?: ""
        }
        property.className.equals("boolean", ignoreCase = true).takeIf { it }?.let {
            result = value as? Boolean ?: value?.toString()?.toBoolean()
        }
        property.className.equals("double", ignoreCase = true).takeIf { it }?.let {
            result = value as? Double ?: value?.toString()?.toDoubleOrNull()
        }
        property.className.equals("float", ignoreCase = true).takeIf { it }?.let {
            result = value as? Float ?: value?.toString()?.toFloatOrNull()
        }
        property.className.equals("long", ignoreCase = true).takeIf { it }?.let {
            result = value as? Long ?: value?.toString()?.toLongOrNull()
        }
        property.className.equals("uuid", ignoreCase = true).takeIf { it }?.let {
            result = value?.let { uuid ->
                try {
                    UUID.fromString(uuid.toString())
                } catch (_: Exception) { null }
            }
        }
        property.className.equals("path", ignoreCase = true).takeIf { it }?.let {
            result = value?.let { path ->
                try {
                    Path.of(path.toString())
                } catch (_: Exception) { null }
            }
        }
        return result as T? ?: throw IllegalArgumentException("Property ${property.key} not found or invalid type")
    }

}
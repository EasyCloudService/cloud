package dev.easycloud.service.property

class Property<T>(val key: String, val type: Class<T>) {
    val className: String = type.simpleName.lowercase();
}
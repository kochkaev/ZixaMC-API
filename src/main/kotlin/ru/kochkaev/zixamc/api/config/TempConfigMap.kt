package ru.kochkaev.zixamc.api.config

import io.leangen.geantyref.TypeToken
import org.apache.commons.lang3.exception.ExceptionUtils
import java.io.File
import java.lang.reflect.Type

@Suppress("UNCHECKED_CAST")
open class TempConfigMap(
    val file: File
) {
    companion object {
        val model: Type = object: TypeToken<Map<TempConfigValueType<*>, *>>(){}.type
    }

    protected var content: Map<TempConfigValueType<*>, *>
        get() = ConfigManager.load<Map<TempConfigValueType<*>, Any>>(file, model) ?: mapOf<TempConfigValueType<*>, Any>()
        set(content) {
            ConfigManager.update(file, content)
        }

    open fun init() {
        if (file.length() == 0L) {
            ConfigManager.create(file) { mapOf<TempConfigValueType<*>, Any>() }
        }
    }
    open fun getAll(): Map<TempConfigValueType<*>, *> = content
    open fun <T> get(key: TempConfigValueType<T>): T? = content[key] as? T
    /** Does nothing if TempConfigValueType with that serialized name is not registered */
    open fun <T> get(serialized: String): T? {
        val found: TempConfigValueType<T> = TempConfig.registeredValueTypes[serialized] as? TempConfigValueType<T> ?: return null
        return get(found)
    }
    open fun setAll(map: Map<TempConfigValueType<*>, *>) {
        content = map
    }
    open fun contains(key: TempConfigValueType<*>) = content.containsKey(key)
    open fun contains(serialized: String) = content.keys.any { it.serializedName == serialized }
    open fun <T> set(key: TempConfigValueType<T>, value: T) {
        content = content.toMutableMap().apply { this[key] = value }
    }
    /** Does nothing if TempConfigValueType with that serialized name is not registered */
    open fun <T> set(serialized: String, value: T) {
        val found: TempConfigValueType<T> = TempConfig.registeredValueTypes[serialized] as? TempConfigValueType<T> ?: return
        set(found, value)
    }
    open fun remove(key: TempConfigValueType<*>) {
        content = content.toMutableMap().apply { this.remove(key) }
    }
    /** Does nothing if TempConfigValueType with that serialized name is not registered */
    open fun remove(serialized: String) {
        val found = TempConfig.registeredValueTypes[serialized] ?: return
        remove(found)
    }
}
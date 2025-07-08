package ru.kochkaev.zixamc.api.config
import java.io.File
import java.lang.reflect.Type

open class ConfigFile<T>(
    val file: File,
    val model: Type,
    val supplier: () -> T
) {
    protected open var content: T? = null
    open var config: T
        get() = content ?: supplier()
        set(config) {
            content = config
            update()
        }
    open fun init() {
        ConfigManager.init(file, model, supplier, { content }) { content = it }
    }
    open fun load() {
        content = ConfigManager.load(file, model)
    }
    open fun update() {
        ConfigManager.update(file, content)
    }
}
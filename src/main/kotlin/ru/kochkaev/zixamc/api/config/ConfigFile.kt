package ru.kochkaev.zixamc.api.config
import java.io.File

open class ConfigFile<T>(
    val file: File,
    val model: Class<T>,
    val supplier: () -> T = { model.getDeclaredConstructor().newInstance() }
) {
    private var content: T? = null
    var config: T
        get() = content ?: supplier()
        set(config) {
            content = config
            update()
        }
    fun init() {
        ConfigManager.init(file, model, supplier, { content }) { content = it }
    }
    fun load() {
        content = ConfigManager.load(file, model)
    }
    fun update() {
        ConfigManager.update(file, content)
    }
}
package ru.kochkaev.zixamc.api.config

import org.apache.commons.io.IOUtils
import org.apache.commons.lang3.exception.ExceptionUtils
import ru.kochkaev.zixamc.api.ZixaMC
import java.io.File
import java.io.FileOutputStream
import java.lang.reflect.Type
import java.nio.charset.StandardCharsets

/**
 * @author Xujiayao
 */
object ConfigManager {

    private val logger
        get() = ZixaMC.logger
    var config: Config
        get() = Config.config
        set(config) {
            Config.config = config
        }

    private val toReload = arrayListOf<ConfigFile<*>>()
    fun registerConfig(config: ConfigFile<*>) {
        toReload.add(config)
        config.init()
    }
    fun reload() {
        toReload.forEach { it.load() }
    }

    @Throws(Exception::class)
    fun <T> init(file: File, type: Type, supplier: ()->T, getter: ()->T?, setter: (T?)->Unit) {
        if (file.length() != 0L) {
            try {
                setter.invoke(load(file, type))
                update(file, getter.invoke())
            } catch (e: Exception) {
                logger.error(ExceptionUtils.getStackTrace(e))
            }
        } else {
            setter.invoke(create(file, supplier))
        }
    }

    fun <T> create(file: File, supplier: ()->T): T? {
        val content: T = supplier.invoke()
        try {
            FileOutputStream(file).use { outputStream ->
                val jsonString = GsonManager.gson.toJson(content)
                IOUtils.write(jsonString, outputStream, StandardCharsets.UTF_8)
                return content
            }
        } catch (e: Exception) {
            logger.error(ExceptionUtils.getStackTrace(e))
            return null
        }
    }

    fun <T> load(file: File, type: Type): T? {
        var content: T? = null
        try {
            content = GsonManager.gson.fromJson(
                IOUtils.toString(file.toURI(), StandardCharsets.UTF_8),
                type
            )
        } catch (e: Exception) {
            logger.error(ExceptionUtils.getStackTrace(e))
        }
        return content
    }

    fun update(file: File, content: Any?) {
        try {
            FileOutputStream(file).use { outputStream ->
                val jsonString = GsonManager.gson.toJson(content)
                IOUtils.write(jsonString, outputStream, StandardCharsets.UTF_8)
            }
        } catch (e: Exception) {
            logger.error(ExceptionUtils.getStackTrace(e))
        }
    }
}
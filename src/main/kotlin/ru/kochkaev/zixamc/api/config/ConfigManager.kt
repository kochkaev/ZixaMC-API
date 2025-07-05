package ru.kochkaev.zixamc.api.config

import net.fabricmc.loader.api.FabricLoader
import org.apache.commons.io.IOUtils
import org.apache.commons.lang3.exception.ExceptionUtils
import ru.kochkaev.zixamc.api.ZixaMC
import java.io.File
import java.io.FileOutputStream
import java.nio.charset.StandardCharsets

/**
 * @author Xujiayao
 */
object ConfigManager {

    private val logger
        get() = ZixaMC.logger
    lateinit var config: Config
    private val configFile = File(FabricLoader.getInstance().configDir.toFile(), "ZixaMCTGBridge.json")

    @Throws(Exception::class)
    fun init() {
        init(configFile, Config::class.java, ::Config, ConfigManager::config) { config = it ?: Config() }
    }
    @Throws(Exception::class)
    fun <T> init(file: File, clazz: Class<T>, supplier: ()->T, getter: ()->T?, setter: (T?)->Unit) {
        if (file.length() != 0L) {
            try {
                setter.invoke(load(file, clazz))
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

    fun load() {
        config = load(configFile, Config::class.java) ?: Config()
    }
    fun <T> load(file: File, clazz: Class<T>): T? {
        var content: T? = null
        try {
            content = GsonManager.gson.fromJson(
                IOUtils.toString(file.toURI(), StandardCharsets.UTF_8),
                clazz
            )
        } catch (e: Exception) {
            logger.error(ExceptionUtils.getStackTrace(e))
        }
        return content
    }

    fun update() {
        update(configFile, config)
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
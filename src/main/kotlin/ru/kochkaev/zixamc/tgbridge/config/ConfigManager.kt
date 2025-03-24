package ru.kochkaev.zixamc.tgbridge.config

import com.google.gson.GsonBuilder
import net.fabricmc.loader.api.FabricLoader
import org.apache.commons.io.IOUtils
import org.apache.commons.lang3.exception.ExceptionUtils
import ru.kochkaev.zixamc.tgbridge.ZixaMCTGBridge
import java.io.File
import java.io.FileOutputStream
import java.nio.charset.StandardCharsets
import ru.kochkaev.zixamc.tgbridge.config.GsonManager.gson

/**
 * @author Xujiayao
 */
object ConfigManager {

    private val LOGGER = ZixaMCTGBridge.logger
    var CONFIG: Config? = null
    private val CONFIG_FILE = File(FabricLoader.getInstance().configDir.toFile(), "ZixaMCTGBridge.json")

    @Throws(Exception::class)
    fun init() {
        init(CONFIG_FILE, Config::class.java, ::Config, ConfigManager::CONFIG) { CONFIG = it }
    }
    @Throws(Exception::class)
    private fun <T> init(file: File, clazz: Class<T>, supplier: ()->T, getter: ()->T?, setter: (T?)->Unit) {
        if (file.length() != 0L) {
            try {
                setter.invoke(load(file, clazz))
                update(file, getter.invoke())
            } catch (e: Exception) {
                LOGGER.error(ExceptionUtils.getStackTrace(e))
            }
        } else {
            setter.invoke(create(file, supplier))
        }
    }

    private fun <T> create(file: File, supplier: ()->T): T? {
        val content: T = supplier.invoke()
        try {
            FileOutputStream(file).use { outputStream ->
                val jsonString = gson.toJson(content)
                IOUtils.write(jsonString, outputStream, StandardCharsets.UTF_8)
                return content
            }
        } catch (e: Exception) {
            LOGGER.error(ExceptionUtils.getStackTrace(e))
            return null
        }
    }

    fun load() {
        CONFIG = load(CONFIG_FILE, Config::class.java)
    }
    private fun <T> load(file: File, clazz: Class<T>): T? {
        var content: T? = null
        try {
            content = gson.fromJson(
                IOUtils.toString(file.toURI(), StandardCharsets.UTF_8),
                clazz
            )
        } catch (e: Exception) {
            LOGGER.error(ExceptionUtils.getStackTrace(e))
        }
        return content
    }

    fun update() {
        update(CONFIG_FILE, CONFIG)
    }
    private fun update(file: File, content: Any?) {
        try {
            FileOutputStream(file).use { outputStream ->
                val jsonString = gson.toJson(content)
                IOUtils.write(jsonString, outputStream, StandardCharsets.UTF_8)
            }
        } catch (e: Exception) {
            LOGGER.error(ExceptionUtils.getStackTrace(e))
        }
    }
}
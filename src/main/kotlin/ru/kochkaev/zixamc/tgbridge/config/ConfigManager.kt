package ru.kochkaev.zixamc.tgbridge.config

import com.google.gson.GsonBuilder
import net.fabricmc.loader.api.FabricLoader
import org.apache.commons.io.IOUtils
import org.apache.commons.lang3.exception.ExceptionUtils
import ru.kochkaev.zixamc.tgbridge.ZixaMCTGBridge
import ru.kochkaev.zixamc.tgbridge.config.serialize.TextDataAdapter
import java.io.File
import java.io.FileOutputStream
import java.nio.charset.StandardCharsets

/**
 * @author Xujiayao
 */
object ConfigManager {

    private val LOGGER = ZixaMCTGBridge.logger
    var CONFIG: Config? = null
    private val CONFIG_FILE = File(FabricLoader.getInstance().configDir.toFile(), "ZixaMCTGBridge.json")
    @Throws(Exception::class)
    fun init(throwException: Boolean) {
        if (CONFIG_FILE.length() != 0L) {
            try {
//                FileUtils.copyFile(JdkConstants.CONFIG_FILE, CONFIG_BACKUP_FILE)

                load()

//                try {
//                    if (!Level.CONFIG.customMessage.formattedResponseMessage.isBlank()) {
//                        Gson().fromJson(
//                            Level.CONFIG.customMessage.formattedResponseMessage,
//                            Any::class.java
//                        )
//                    }
//                    if (!Level.CONFIG.customMessage.formattedChatMessage.isBlank()) {
//                        Gson().fromJson(
//                            Level.CONFIG.customMessage.formattedChatMessage,
//                            Any::class.java
//                        )
//                    }
//                    if (!Level.CONFIG.customMessage.formattedOtherMessage.isBlank()) {
//                        Gson().fromJson(
//                            Level.CONFIG.customMessage.formattedOtherMessage,
//                            Any::class.java
//                        )
//                    }
//                } catch (e: JsonSyntaxException) {
//                    LOGGER.error(ExceptionUtils.getStackTrace(e))
//                    LOGGER.error("Invalid JSON!")
//                }

                update()
            } catch (e: Exception) {
                if (throwException) {
                    throw e
                }

                LOGGER.error(ExceptionUtils.getStackTrace(e))
            }
        } else {
            create()

            LOGGER.error("-----------------------------------------")
            LOGGER.error("Error: The ZixaMCTGBridge config file cannot be found or is empty!")
            LOGGER.error("Stopping the server...")
            LOGGER.error("-----------------------------------------")

            System.exit(0)
        }
    }

    private fun create() {
        try {
            FileOutputStream(CONFIG_FILE).use { outputStream ->
                val jsonString = GsonBuilder()
                    .setPrettyPrinting()
                    .disableHtmlEscaping()
                    .serializeNulls()
                    .registerTypeAdapter(TextData::class.java, TextDataAdapter())
                    .create()
                    .toJson(Config())
                IOUtils.write(jsonString, outputStream, StandardCharsets.UTF_8)
            }
        } catch (e: Exception) {
            LOGGER.error(ExceptionUtils.getStackTrace(e))
        }
    }

    fun load() {
        try {
            CONFIG = GsonBuilder()
                .setPrettyPrinting()
                .disableHtmlEscaping()
                .serializeNulls()
                .registerTypeAdapter(TextData::class.java, TextDataAdapter())
                .create()
                .fromJson(
                    IOUtils.toString(CONFIG_FILE.toURI(), StandardCharsets.UTF_8),
                    Config::class.java
                )
        } catch (e: Exception) {
            LOGGER.error(ExceptionUtils.getStackTrace(e))
        }
    }

    fun update() {
        try {
            FileOutputStream(CONFIG_FILE).use { outputStream ->
                val jsonString = GsonBuilder()
                    .setPrettyPrinting()
                    .disableHtmlEscaping()
                    .serializeNulls()
                    .registerTypeAdapter(TextData::class.java, TextDataAdapter())
                    .create()
                    .toJson(CONFIG)
                IOUtils.write(jsonString, outputStream, StandardCharsets.UTF_8)
            }
        } catch (e: Exception) {
            LOGGER.error(ExceptionUtils.getStackTrace(e))
        }
    }
}
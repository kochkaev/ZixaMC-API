package ru.kochkaev.zixamc.api.config

import net.fabricmc.loader.api.FabricLoader
import java.io.File

object TempConfig {

    val temp = TempConfigMap(File(FabricLoader.getInstance().configDir.toFile(), "ZixaMC-TempConfig.json"))

    val registeredValueTypes = hashMapOf<String, TempConfigValueType<*>>()
    fun registerValueType(type: TempConfigValueType<*>) {
        registeredValueTypes[type.serializedName] = type
    }

}
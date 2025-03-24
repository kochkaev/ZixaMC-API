package ru.kochkaev.zixamc.tgbridge.telegram.serverBot.integration

import de.maxhenkel.audioplayer.AudioManager
import de.maxhenkel.audioplayer.AudioPlayer
import de.maxhenkel.audioplayer.FileNameManager
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.server.MinecraftServer
import java.nio.file.Path
import java.util.UUID

object AudioPlayerIntegration {

    val ruEnMap: Map<Char, String> = mapOf(
        'а' to "a", 'б' to "b", 'в' to "v", 'г' to "g", 'д' to "d", 'е' to "e", 'ё' to "yo",
        'ж' to "zh", 'з' to "z", 'и' to "i", 'й' to "y", 'к' to "k", 'л' to "l", 'м' to "m",
        'н' to "n", 'о' to "o", 'п' to "p", 'р' to "r", 'с' to "s", 'т' to "t", 'у' to "u",
        'ф' to "f", 'х' to "h", 'ц' to "ts", 'ч' to "ch", 'ш' to "sh", 'щ' to "shch",
        'ъ' to "", 'ы' to "y", 'ь' to "", 'э' to "e", 'ю' to "yu", 'я' to "ya"
    )

    fun resolveId(
        path: Path,
        server: MinecraftServer = FabricLoader.getInstance().gameInstance as MinecraftServer
    ): UUID {
        val uuid = UUID.randomUUID()
        AudioManager.saveSound(server, uuid, path)
        return uuid
    }

    fun resolveName(current: String): String {
        val dotIndex = current.lastIndexOf('.')
        val base = current.substring(0, dotIndex)
        val extension = current.substring(dotIndex).lowercase()
        val ruToEnBase = StringBuilder()
        base.toCharArray().forEach {
            if (ruEnMap.contains(it)) {
                val en = ruEnMap[it.lowercaseChar()] ?: ""
                ruToEnBase.append(if (it.isUpperCase()) en.replaceFirstChar { it1 -> it1.uppercaseChar() } else en)
            } else ruToEnBase.append(it)
        }
        val sanitizedBase = ruToEnBase.replace(Regex("[^a-z0-9_ \\-]", RegexOption.IGNORE_CASE), "")
        return sanitizedBase + extension
    }
}
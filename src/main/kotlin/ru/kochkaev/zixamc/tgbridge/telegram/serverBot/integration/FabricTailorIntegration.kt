package ru.kochkaev.zixamc.tgbridge.telegram.serverBot.integration

import com.google.gson.JsonObject
import com.mojang.authlib.properties.Property
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.server.network.ServerPlayerEntity
import org.samo_lego.fabrictailor.casts.TailoredPlayer
import org.samo_lego.fabrictailor.command.SkinCommand
import ru.kochkaev.zixamc.tgbridge.telegram.ServerBot
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.net.URLConnection
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.util.Scanner
import java.util.UUID
import javax.imageio.ImageIO
import javax.net.ssl.HttpsURLConnection

object FabricTailorIntegration {

    val isModLoaded: Boolean
        get() = FabricLoader.getInstance().isModLoaded("fabrictailor")

    suspend fun setSkinFromFile(file: File, slim: Boolean, player: ServerPlayerEntity): SkinUploadResult {
        if (!isModLoaded) return SkinUploadResult.OTHER_ERROR
        var skin: Property? = null
        try {
            val input = FileInputStream(file)
            val fileType = input.read()
            if (fileType == 137) {
                // Check image dimensions
                val image = ImageIO.read(file);
                if (image.width != 64 || (image.height != 64 && image.height != 32)) {
                    return SkinUploadResult.WRONG_RESOLUTION
                }
                try {
                    val reply = urlRequest(URI.create("https://api.mineskin.org/v2/generate").toURL(), false, file, null, if (slim) "slim" else "classic");
                    skin =  getSkinFromReply(reply)
                } catch (e: IOException) {
                    // Error uploading
                    return SkinUploadResult.UPLOAD_ERROR
                }
            }
        } catch (e: IOException) {
            // Not an image
            return SkinUploadResult.NOT_AN_IMAGE
        }
//        skin?.let { SkinCommand.setSkin(player) { it } }
        return SkinUploadResult.SUCCESS
    }

    enum class SkinUploadResult {
        WRONG_RESOLUTION {
            override val message: String
                get() = ServerBot.config.integration.fabricTailor.messageErrorWrongResolution
        },
        NOT_AN_IMAGE {
            override val message: String
                get() = ServerBot.config.integration.fabricTailor.messageErrorNotAnImage
        },
        UPLOAD_ERROR {
            override val message: String
                get() = ServerBot.config.integration.fabricTailor.messageErrorUpload
        },
        OTHER_ERROR {
            override val message: String
                get() = ServerBot.config.integration.fabricTailor.messageErrorUpload
        },
        SUCCESS {
            override val message: String
                get() = ServerBot.config.integration.fabricTailor.messageDone
        };
        abstract val message: String
    }

    private fun getSkinFromReply(reply: String?): Property? {
        if (reply == null || reply.contains("error") || reply.isEmpty()) {
            return null
        }

        val value: String? =
            reply.split("\"value\":\"".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1].split("\"".toRegex())
                .dropLastWhile { it.isEmpty() }.toTypedArray()[0]
        val signature: String? = reply.split("\"signature\":\"".toRegex()).dropLastWhile { it.isEmpty() }
            .toTypedArray()[1].split("\"".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]

        return Property(TailoredPlayer.PROPERTY_TEXTURES, value, signature)
    }

    /**
     * Gets reply from a skin website.
     * Used internally only.
     *
     * @param url url of the website
     * @param useGetMethod whether to use GET method instead of POST
     * @param image image to upload, otherwise null
     * @return reply from website as string
     * @throws IOException IOException is thrown when connection fails for some reason.
     */
    @Throws(IOException::class)
    private fun urlRequest(url: URL, useGetMethod: Boolean, image: File?, skinUrl: String?, variant: String?): String? {
        val connection: URLConnection? = url.openConnection()

        var reply: String? = null

        if (connection is HttpsURLConnection) {
            connection.setUseCaches(false)
            connection.setDoOutput(true)
            connection.setDoInput(true)
            connection.setRequestMethod(if (useGetMethod) "GET" else "POST")

            if (image != null) {
                // Do a POST request
                val boundary: String? = UUID.randomUUID().toString()
                connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary)
                connection.setRequestProperty("User-Agent", "User-Agent")
                val outputStream = connection.getOutputStream()
                val writer: PrintWriter = PrintWriter(OutputStreamWriter(outputStream, StandardCharsets.UTF_8), true)

                val LINE = "\r\n"
                writer.append("--").append(boundary).append(LINE)
                writer.append("Content-Disposition: form-data; name=\"file\"").append(LINE)
                writer.append("Content-Type: text/plain; charset=UTF-8").append(LINE)
                writer.append(LINE)
                writer.append(image.getName()).append(LINE)
                writer.flush()

                writer.append("--").append(boundary).append(LINE)
                writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"").append(image.getName())
                    .append("\"")
                if (variant != null) writer.append("; variant=\"").append(variant).append("\"")
                writer.append(LINE)
                writer.append("Content-Type: image/png").append(LINE)
                writer.append("Content-Transfer-Encoding: binary").append(LINE)
                writer.append(LINE)
                writer.flush()

                val fileBytes: ByteArray = Files.readAllBytes(image.toPath())
                outputStream.write(fileBytes, 0, fileBytes.size)

                outputStream.flush()
                writer.append(LINE)
                writer.flush()

                writer.append("--").append(boundary).append("--").append(LINE)
                writer.close()
            }

            if (skinUrl != null) {
                // Do a POST request
                connection.setRequestProperty("Content-Type", "application/json")
                connection.setRequestProperty("User-Agent", "User-Agent")
                val outputStream = connection.getOutputStream()
                val writer: PrintWriter = PrintWriter(OutputStreamWriter(outputStream, StandardCharsets.UTF_8))

                val json: JsonObject = JsonObject()
                json.addProperty("url", skinUrl)
                json.addProperty("variant", variant)

                writer.write(json.toString())
                writer.flush()
                writer.close()
            }

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                reply = getContent(connection)
            }
            connection.disconnect()
        } else {
            reply = getContent(connection)
        }
        return reply
    }

    @Throws(IOException::class)
    private fun getContent(connection: URLConnection?): String? {
        connection?.getInputStream()?.use { `is` ->
            InputStreamReader(`is`).use { isr ->
                Scanner(isr).use { scanner ->
                    val reply = StringBuilder()
                    while (scanner.hasNextLine()) {
                        val line: String = scanner.next()
                        if (line.trim { it <= ' ' }.isEmpty()) continue
                        reply.append(line)
                    }
                    return reply.toString()
                }
            }
        }
        return null
    }

}
package ru.kochkaev.zixamc.api.telegram.model

import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import ru.kochkaev.zixamc.api.config.serialize.TgPassportElementErrorAdapter
import ru.kochkaev.zixamc.api.config.serialize.TgTypeEnum
import java.lang.reflect.Type

/** Describes Telegram Passport data shared with the bot by the user. */
data class TgPassportData(
    /** Array with information about documents and other Telegram Passport elements that was shared with the bot */
    val data: List<TgEncryptedPassportElement>,
    /** Encrypted credentials required to decrypt the data */
    val credentials: List<TgEncryptedCredentials>,
)
/** This object represents a file uploaded to Telegram Passport. Currently all Telegram Passport files are in JPEG format when decrypted and don't exceed 10MB. */
data class TgPassportFile(
    /** Identifier for this file, which can be used to download or reuse the file */
    @SerializedName("file_id")
    val fileId: String,
    /** Unique identifier for this file, which is supposed to be the same over time and for different bots. Can't be used to download or reuse the file. */
    @SerializedName("file_unique_id")
    val fileUniqueId: String,
    /** File size in bytes. */
    @SerializedName("file_size")
    val fileSize: Int?,
    /** Unix time when the file was uploaded */
    @SerializedName("file_date")
    val fileDate: Int?,
)
/** Describes documents or other Telegram Passport elements shared with the bot by the user. */
data class TgEncryptedPassportElement(
    /** Element type. One of “personal_details”, “passport”, “driver_license”, “identity_card”, “internal_passport”, “address”, “utility_bill”, “bank_statement”, “rental_agreement”, “passport_registration”, “temporary_registration”, “phone_number”, “email”. */
    val type: TgEncryptedPassportElementType,
    /** Base64-encoded encrypted Telegram Passport element data provided by the user; available only for “personal_details”, “passport”, “driver_license”, “identity_card”, “internal_passport” and “address” types. Can be decrypted and verified using the accompanying EncryptedCredentials. */
    val data: String?,
    /** User's verified phone number; available only for “phone_number” type */
    @SerializedName("phone_number")
    val phoneNumber: String?,
    /** User's verified email address; available only for “email” type */
    val email: String?,
    /** Array of encrypted files with documents provided by the user; available only for “utility_bill”, “bank_statement”, “rental_agreement”, “passport_registration” and “temporary_registration” types. Files can be decrypted and verified using the accompanying EncryptedCredentials. */
    val files: List<TgPassportFile>?,
    /** Encrypted file with the front side of the document, provided by the user; available only for “passport”, “driver_license”, “identity_card” and “internal_passport”. The file can be decrypted and verified using the accompanying EncryptedCredentials. */
    @SerializedName("front_side")
    val frontSide: TgPassportFile?,
    /** Encrypted file with the reverse side of the document, provided by the user; available only for “driver_license” and “identity_card”. The file can be decrypted and verified using the accompanying EncryptedCredentials. */
    @SerializedName("reverse_side")
    val reverseSide: TgPassportFile?,
    /** Encrypted file with the selfie of the user holding a document, provided by the user; available if requested for “passport”, “driver_license”, “identity_card” and “internal_passport”. The file can be decrypted and verified using the accompanying EncryptedCredentials. */
    val selfie: TgPassportFile?,
    /** Array of encrypted files with translated versions of documents provided by the user; available if requested for “passport”, “driver_license”, “identity_card”, “internal_passport”, “utility_bill”, “bank_statement”, “rental_agreement”, “passport_registration” and “temporary_registration” types. Files can be decrypted and verified using the accompanying EncryptedCredentials. */
    val translation: List<TgPassportFile>?,
    /** Base64-encoded element hash for using in PassportElementErrorUnspecified */
    val hash: String,
)
enum class TgEncryptedPassportElementType {
    @SerializedName("personal_details")
    PERSONAL_DETAILS,
    @SerializedName("passport")
    PASSPORT,
    @SerializedName("driver_license")
    DRIVER_LICENSE,
    @SerializedName("identity_card")
    IDENTITY_CARD,
    @SerializedName("internal_passport")
    INTERNAL_PASSPORT,
    @SerializedName("address")
    ADDRESS,
    @SerializedName("utility_bill")
    UTILITY_BILL,
    @SerializedName("bank_statement")
    BANK_STATEMENT,
    @SerializedName("rental_agreement")
    RENTAL_AGREEMENT,
    @SerializedName("passport_registration")
    PASSPORT_REGISTRATION,
    @SerializedName("temporary_registration")
    TEMPORARY_REGISTRATION,
    @SerializedName("phone_number")
    PHONE_NUMBER,
    @SerializedName("email")
    EMAIL,
}

/** Describes data required for decrypting and authenticating EncryptedPassportElement. See the Telegram Passport Documentation for a complete description of the data decryption and authentication processes: https://core.telegram.org/passport#receiving-information */
data class TgEncryptedCredentials(
    /** Base64-encoded encrypted JSON-serialized data with unique user's payload, data hashes and secrets required for EncryptedPassportElement decryption and authentication */
    val data: String,
    /** Base64-encoded data hash for data authentication */
    val hash: String,
    /** Base64-encoded secret, encrypted with the bot's public RSA key, required for data decryption */
    val secret: String,
)

/** This object represents an error in the Telegram Passport element which was submitted that should be resolved by the user. It should be one of: PassportElementErrorDataField, PassportElementErrorFrontSide, PassportElementErrorReverseSide, PassportElementErrorSelfie, PassportElementErrorFile, PassportElementErrorFiles, PassportElementErrorTranslationFile, PassportElementErrorTranslationFiles or PassportElementErrorUnspecified */
@JsonAdapter(TgPassportElementErrorAdapter::class)
open class TgPassportElementError(
    /** Error source */
    val source: TgPassportElementErrorSource,
    /** The section of the user's Telegram Passport which has the issue */
    val type: TgEncryptedPassportElementType,
    /** Error message */
    val message: String,
)
enum class TgPassportElementErrorSource: TgTypeEnum {
    @SerializedName("data")
    DATA {
        override val model = TgPassportElementErrorDataField::class.java
    },
    @SerializedName("front_side")
    FRONT_SIDE {
        override val model = TgPassportElementErrorFrontSide::class.java
    },
    @SerializedName("reverse_side")
    REVERSE_SIDE {
        override val model = TgPassportElementErrorReverseSide::class.java
    },
    @SerializedName("selfie")
    SELFIE {
        override val model = TgPassportElementErrorSelfie::class.java
    },
    @SerializedName("file")
    FILE {
        override val model = TgPassportElementErrorFile::class.java
    },
    @SerializedName("files")
    FILES {
        override val model = TgPassportElementErrorFiles::class.java
    },
    @SerializedName("translation_file")
    TRANSLATION_FILE {
        override val model = TgPassportElementErrorTranslationFile::class.java
    },
    @SerializedName("translation_files")
    TRANSLATION_FILES {
        override val model = TgPassportElementErrorTranslationFiles::class.java
    },
    @SerializedName("unspecified")
    UNSPECIFIED {
        override val model = TgPassportElementErrorUnspecified::class.java
    },
}
/** Represents an issue in one of the data fields that was provided by the user. The error is considered resolved when the field's value changes. */
class TgPassportElementErrorDataField(
    /** The section of the user's Telegram Passport which has the error, one of “personal_details”, “passport”, “driver_license”, “identity_card”, “internal_passport”, “address” */
    type: TgEncryptedPassportElementType,
    message: String,
    /** Name of the data field which has the error */
    @SerializedName("field_name")
    val fieldName: String,
    /** Base64-encoded data hash */
    @SerializedName("data_hash")
    val dataHash: String,
): TgPassportElementError(TgPassportElementErrorSource.DATA, type, message)
/** Represents an issue with the front side of a document. The error is considered resolved when the file with the front side of the document changes. */
class TgPassportElementErrorFrontSide(
    /** The section of the user's Telegram Passport which has the issue, one of “passport”, “driver_license”, “identity_card”, “internal_passport” */
    type: TgEncryptedPassportElementType,
    message: String,
    /** Base64-encoded hash of the file with the front side of the document */
    @SerializedName("file_hash")
    val fileHash: String,
): TgPassportElementError(TgPassportElementErrorSource.FRONT_SIDE, type, message)
/** Represents an issue with the reverse side of a document. The error is considered resolved when the file with reverse side of the document changes. */
class TgPassportElementErrorReverseSide(
    /** The section of the user's Telegram Passport which has the issue, one of “driver_license”, “identity_card” */
    type: TgEncryptedPassportElementType,
    message: String,
    /** Base64-encoded hash of the file with the reverse side of the document */
    @SerializedName("file_hash")
    val fileHash: String,
): TgPassportElementError(TgPassportElementErrorSource.REVERSE_SIDE, type, message)
/** Represents an issue with the selfie with a document. The error is considered resolved when the file with the selfie changes. */
class TgPassportElementErrorSelfie(
    /** The section of the user's Telegram Passport which has the issue, one of “passport”, “driver_license”, “identity_card”, “internal_passport” */
    type: TgEncryptedPassportElementType,
    message: String,
    /** Base64-encoded hash of the file with the selfie */
    @SerializedName("file_hash")
    val fileHash: String,
): TgPassportElementError(TgPassportElementErrorSource.SELFIE, type, message)
/** Represents an issue with a document scan. The error is considered resolved when the file with the document scan changes. */
class TgPassportElementErrorFile(
    /**  */
    type: TgEncryptedPassportElementType,
    message: String,
    /** Base64-encoded file hash */
    @SerializedName("file_hash")
    val fileHash: String,
): TgPassportElementError(TgPassportElementErrorSource.FILE, type, message)
/** Represents an issue with a list of scans. The error is considered resolved when the list of files containing the scans changes. */
class TgPassportElementErrorFiles(
    /** The section of the user's Telegram Passport which has the issue, one of “utility_bill”, “bank_statement”, “rental_agreement”, “passport_registration”, “temporary_registration” */
    type: TgEncryptedPassportElementType,
    message: String,
    /** List of base64-encoded file hashes */
    @SerializedName("file_hashes")
    val fileHashes: List<String>,
): TgPassportElementError(TgPassportElementErrorSource.FILES, type, message)
/** Represents an issue with one of the files that constitute the translation of a document. The error is considered resolved when the file changes. */
class TgPassportElementErrorTranslationFile(
    /** Type of element of the user's Telegram Passport which has the issue, one of “passport”, “driver_license”, “identity_card”, “internal_passport”, “utility_bill”, “bank_statement”, “rental_agreement”, “passport_registration”, “temporary_registration” */
    type: TgEncryptedPassportElementType,
    message: String,
    /** Base64-encoded file hash */
    @SerializedName("file_hash")
    val fileHash: String,
): TgPassportElementError(TgPassportElementErrorSource.TRANSLATION_FILE, type, message)
/** Represents an issue with the translated version of a document. The error is considered resolved when a file with the document translation change. */
class TgPassportElementErrorTranslationFiles(
    /** Type of element of the user's Telegram Passport which has the issue, one of “passport”, “driver_license”, “identity_card”, “internal_passport”, “utility_bill”, “bank_statement”, “rental_agreement”, “passport_registration”, “temporary_registration” */
    type: TgEncryptedPassportElementType,
    message: String,
    /** List of base64-encoded file hashes */
    @SerializedName("file_hashes")
    val fileHashes: List<String>,
): TgPassportElementError(TgPassportElementErrorSource.TRANSLATION_FILES, type, message)
/** Represents an issue in an unspecified place. The error is considered resolved when new data is added. */
class TgPassportElementErrorUnspecified(
    /** Type of element of the user's Telegram Passport which has the issue */
    type: TgEncryptedPassportElementType,
    message: String,
    /** Base64-encoded element hash */
    @SerializedName("element_hash")
    val elementHash: String,
): TgPassportElementError(TgPassportElementErrorSource.UNSPECIFIED, type, message)
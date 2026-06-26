package com.github.aivanovski.keepasstreebuilder.converter.kotpass

import app.keemobile.kotpass.constants.GroupOverride
import app.keemobile.kotpass.cryptography.EncryptedValue
import app.keemobile.kotpass.cryptography.format.BaseCiphers
import app.keemobile.kotpass.database.Credentials
import app.keemobile.kotpass.database.KeePassDatabase
import app.keemobile.kotpass.database.encode
import app.keemobile.kotpass.database.header.DatabaseHeader
import app.keemobile.kotpass.database.header.DatabaseInnerHeader
import app.keemobile.kotpass.database.header.KdfParameters
import app.keemobile.kotpass.database.header.Signature
import app.keemobile.kotpass.database.modifiers.modifyBinaries
import app.keemobile.kotpass.database.modifiers.modifyGroup
import app.keemobile.kotpass.models.BinaryData
import app.keemobile.kotpass.models.BinaryReference
import app.keemobile.kotpass.models.DatabaseContent
import app.keemobile.kotpass.models.DatabaseElement
import app.keemobile.kotpass.models.Entry
import app.keemobile.kotpass.models.EntryFields
import app.keemobile.kotpass.models.EntryValue
import app.keemobile.kotpass.models.FormatVersion
import app.keemobile.kotpass.models.Group
import app.keemobile.kotpass.models.Meta
import app.keemobile.kotpass.models.TimeData
import com.github.aivanovski.keepasstreebuilder.Fields
import com.github.aivanovski.keepasstreebuilder.converter.Converter
import com.github.aivanovski.keepasstreebuilder.extensions.toByteString
import com.github.aivanovski.keepasstreebuilder.extensions.traverseAndCollect
import com.github.aivanovski.keepasstreebuilder.model.Database
import com.github.aivanovski.keepasstreebuilder.model.DatabaseKey
import com.github.aivanovski.keepasstreebuilder.model.DatabaseNode
import com.github.aivanovski.keepasstreebuilder.model.EntryEntity
import com.github.aivanovski.keepasstreebuilder.model.GroupEntity
import com.github.aivanovski.keepasstreebuilder.model.KeyHashingAlgorithm
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.security.SecureRandom
import java.util.Random
import java.util.UUID
import okio.ByteString
import okio.ByteString.Companion.toByteString

class KotpassDatabaseConverter : Converter<Group, Entry, DatabaseElement, KeePassDatabase> {

    override fun createEntry(entry: EntryEntity): Entry = entry.toKotpassEntry()

    override fun createGroup(
        group: GroupEntity,
        groups: List<Group>,
        entries: List<Entry>
    ): Group =
        group.toKotpassGroup(
            groups = groups,
            entries = entries
        )

    override fun createDatabase(
        key: DatabaseKey,
        root: DatabaseNode<Group>,
        keyHashingAlgorithm: KeyHashingAlgorithm
    ): Database<DatabaseElement, KeePassDatabase> {
        val random = Random()

        val db = KeePassDatabase.Ver4x(
            credentials = key.toCredentials(),
            header = DatabaseHeader.Ver4x(
                signature = Signature.Default,
                version = FormatVersion(4, 1),
                cipherId = BaseCiphers.Aes.uuid,
                compression = DatabaseHeader.Compression.GZip,
                masterSeed = random.nextByteString(32),
                encryptionIV = random.nextByteString(BaseCiphers.Aes.ivLength.toInt()),
                kdfParameters = keyHashingAlgorithm.toKdfParameters(),
                publicCustomData = mapOf()
            ),
            content = DatabaseContent(
                meta = Meta(recycleBinEnabled = false),
                group = Group(
                    uuid = UUID.randomUUID(),
                    name = DATABASE_NAME,
                    enableAutoType = GroupOverride.Enabled,
                    enableSearching = GroupOverride.Enabled
                ),
                deletedObjects = listOf()
            ),
            innerHeader = DatabaseInnerHeader.create()
        )

        val allBinaries = root
            .traverseAndCollect { node ->
                val entity = node.originalEntity
                if (entity is EntryEntity) {
                    entity.binaries
                } else {
                    emptyList()
                }
            }
            .flatten()
            .associate { binary ->
                binary.hash.toByteString() to BinaryData.Uncompressed(
                    memoryProtection = false,
                    rawContent = binary.data
                )
            }

        val result = db
            .modifyGroup(db.content.group.uuid) {
                root.entity
            }
            .modifyBinaries {
                allBinaries
            }

        return Database(
            underlying = result,
            root = root as DatabaseNode<DatabaseElement>,
            contentFactory = { result.toInputStream() }
        )
    }

    private fun KeePassDatabase.toInputStream(): InputStream {
        val out = ByteArrayOutputStream()
        encode(out)
        val bytes = out.toByteArray()
        out.close()

        return ByteArrayInputStream(bytes)
    }

    private fun GroupEntity.toKotpassGroup(
        groups: List<Group> = emptyList(),
        entries: List<Entry> = emptyList()
    ): Group =
        Group(
            uuid = uuid,
            name = fields[Fields.TITLE].orEmpty(),
            groups = groups,
            entries = entries
        )

    private fun EntryEntity.toKotpassEntry(): Entry {
        val kotpassFields = fields.map { (key, value) ->
            val kotpassValue = when (key) {
                Fields.PASSWORD -> EntryValue.Encrypted(EncryptedValue.fromString(value))
                else -> EntryValue.Plain(value)
            }

            key to kotpassValue
        }
            .toMap()

        val kotpassHistory = history.map { entry ->
            entry.toKotpassEntry()
        }

        val kotpassBinaries = binaries.map { binary ->
            val hashData = binary.hash.data

            BinaryReference(
                hash = hashData.toByteString(0, hashData.size),
                name = binary.name
            )
        }

        return Entry(
            uuid = uuid,
            fields = EntryFields(kotpassFields),
            times = TimeData(
                creationTime = created,
                lastAccessTime = null,
                lastModificationTime = modified,
                locationChanged = null,
                expiryTime = expires,
                expires = (expires != null)
            ),
            history = kotpassHistory,
            binaries = kotpassBinaries
        )
    }

    private fun Random.nextByteString(length: Int): ByteString =
        ByteArray(length)
            .apply { nextBytes(this) }
            .toByteString()

    private fun KeyHashingAlgorithm.toKdfParameters(): KdfParameters {
        val random = Random()

        return when (this) {
            is KeyHashingAlgorithm.Aes -> {
                KdfParameters.Aes(
                    rounds = transformationRounds,
                    seed = random.nextByteString(length = KDF_SEED_LENGTH)
                )
            }

            is KeyHashingAlgorithm.Argon2d -> {
                KdfParameters.Argon2(
                    variant = KdfParameters.Argon2.Variant.Argon2d,
                    salt = random.nextByteString(length = KDF_SEED_LENGTH),
                    parallelism = parallelism,
                    memory = memory,
                    iterations = iterations,
                    version = version,
                    secretKey = null,
                    associatedData = null
                )
            }

            is KeyHashingAlgorithm.Argon2id -> {
                KdfParameters.Argon2(
                    variant = KdfParameters.Argon2.Variant.Argon2id,
                    salt = random.nextByteString(length = KDF_SEED_LENGTH),
                    parallelism = parallelism,
                    memory = memory,
                    iterations = iterations,
                    version = version,
                    secretKey = null,
                    associatedData = null
                )
            }
        }
    }

    companion object {

        private const val DATABASE_NAME = "Passwords"
        private const val KDF_SEED_LENGTH = 32

        fun DatabaseKey.toCredentials(): Credentials =
            when (this) {
                is DatabaseKey.PasswordKey -> {
                    Credentials.from(
                        passphrase = EncryptedValue.fromString(password)
                    )
                }

                is DatabaseKey.BinaryKey -> {
                    Credentials.from(
                        keyData = binaryData
                    )
                }

                is DatabaseKey.CompositeKey -> {
                    Credentials.from(
                        passphrase = EncryptedValue.fromString(password),
                        keyData = binaryData
                    )
                }
            }
    }
}
package com.github.aivanovski.keepasstreebuilder

import app.keemobile.kotpass.database.KeePassDatabase
import app.keemobile.kotpass.database.decode
import app.keemobile.kotpass.models.DatabaseElement
import app.keemobile.kotpass.models.EntryValue
import com.github.aivanovski.keepasstreebuilder.DatabaseBuilderDsl.newBuilder
import com.github.aivanovski.keepasstreebuilder.TestData.COMPOSITE_KEY
import com.github.aivanovski.keepasstreebuilder.TestData.ENTRY_WITH_BINARIES
import com.github.aivanovski.keepasstreebuilder.TestData.ENTRY_WITH_HISTORY
import com.github.aivanovski.keepasstreebuilder.TestData.FILE_KEY
import com.github.aivanovski.keepasstreebuilder.TestData.PASSWORD_KEY
import com.github.aivanovski.keepasstreebuilder.TestData.ROOT_GROUP
import com.github.aivanovski.keepasstreebuilder.TestData.newDatabase
import com.github.aivanovski.keepasstreebuilder.TestData.newDatabaseWithBinaries
import com.github.aivanovski.keepasstreebuilder.TestData.newDatabaseWithHistory
import com.github.aivanovski.keepasstreebuilder.converter.kotpass.KotpassDatabaseConverter
import com.github.aivanovski.keepasstreebuilder.converter.kotpass.KotpassDatabaseConverter.Companion.toCredentials
import com.github.aivanovski.keepasstreebuilder.extensions.buildNodeTree
import com.github.aivanovski.keepasstreebuilder.extensions.toByteArray
import com.github.aivanovski.keepasstreebuilder.extensions.traverseAndCollect
import com.github.aivanovski.keepasstreebuilder.extensions.write
import com.github.aivanovski.keepasstreebuilder.extensions.writeToFile
import com.github.aivanovski.keepasstreebuilder.generator.EntityFactory.newEntryFrom
import com.github.aivanovski.keepasstreebuilder.model.Database
import com.github.aivanovski.keepasstreebuilder.model.DatabaseEntity
import com.github.aivanovski.keepasstreebuilder.model.DatabaseKey
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.beInstanceOf
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileInputStream
import java.nio.file.Path
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class DatabaseBuilderDslTest {

    @TempDir
    lateinit var tempDir: Path

    @Test
    fun `should work with different keys`() {
        listOf(
            PASSWORD_KEY,
            FILE_KEY,
            COMPOSITE_KEY
        ).forEach { key ->
            // arrange
            val db = newDatabase(key)

            // act
            val entities = db
                .toByteArray()
                .readDatabaseEntities(key)

            // assert
            entities shouldBe db.collectAllEntities()
        }
    }

    @Test
    fun `should work if db written to file`() {
        // arrange
        val dbFile = newDbFile()
        dbFile.exists() shouldBe false

        // act
        val db = newDatabase(PASSWORD_KEY)
            .apply {
                dbFile.write(this.toByteArray())
            }

        // assert
        val actualEntities = dbFile
            .readBytes()
            .readDatabaseEntities(PASSWORD_KEY)

        actualEntities shouldBe db.collectAllEntities()
    }

    @Test
    fun `entry values should have valid values`() {
        // arrange
        val dbFile = newDbFile()
        val expectedEntry = newEntryFrom(id = 1)

        // act
        newBuilder(KotpassDatabaseConverter())
            .key(PASSWORD_KEY)
            .content(ROOT_GROUP) {
                entry(expectedEntry)
            }
            .build()
            .apply {
                writeToFile(dbFile)
            }

        // assert
        val actualDb = dbFile.readDatabase(PASSWORD_KEY)

        val group = actualDb.content.group
        group.uuid shouldBe ROOT_GROUP.uuid
        group.name shouldBe ROOT_GROUP.fields[Fields.TITLE]

        val entry = actualDb.content.group.entries.first()
        entry.uuid shouldBe expectedEntry.uuid

        listOf(
            Fields.PASSWORD to EntryValue.Encrypted::class,
            Fields.TITLE to EntryValue.Plain::class,
            Fields.USERNAME to EntryValue.Plain::class,
            Fields.URL to EntryValue.Plain::class,
            Fields.NOTES to EntryValue.Plain::class
        )
            .forEach { (field, expectedType) ->
                entry.fields[field] should beInstanceOf(expectedType)
            }

        val fieldValues = entry.fields
            .map { (_, value) -> value.content }
            .toSet()

        fieldValues shouldBe expectedEntry.fields.values.toSet()

        entry.times?.creationTime shouldBe expectedEntry.created
        entry.times?.lastModificationTime shouldBe expectedEntry.modified
        entry.times?.expiryTime shouldBe expectedEntry.expires
        entry.times?.expires shouldBe (expectedEntry.expires != null)
    }

    @Test
    fun `entry should have history`() {
        val db = newDatabaseWithHistory(PASSWORD_KEY).toByteArray()
        val actualEntities = db.readDatabaseEntities(PASSWORD_KEY)

        actualEntities[0] shouldBe ROOT_GROUP
        actualEntities[1] shouldBe ENTRY_WITH_HISTORY
    }

    @Test
    fun `entry should have binaries`() {
        val db = newDatabaseWithBinaries(PASSWORD_KEY).toByteArray()
        val actualEntities = db.readDatabaseEntities(PASSWORD_KEY)

        actualEntities[0] shouldBe ROOT_GROUP
        actualEntities[1] shouldBe ENTRY_WITH_BINARIES
    }

    private fun File.readDatabase(key: DatabaseKey): KeePassDatabase {
        return KeePassDatabase.decode(
            inputStream = FileInputStream(this),
            credentials = key.toCredentials()
        )
    }

    private fun ByteArray.readDatabaseEntities(key: DatabaseKey): List<DatabaseEntity> {
        return KeePassDatabase.decode(
            inputStream = ByteArrayInputStream(this),
            credentials = key.toCredentials()
        )
            .buildNodeTree()
            .traverseAndCollect { node -> node.originalEntity }
    }

    private fun Database<DatabaseElement, KeePassDatabase>.collectAllEntities():
        List<DatabaseEntity> {
        return root.traverseAndCollect { node -> node.originalEntity }
    }

    private fun newDbFile(): File {
        return tempDir.resolve("db.kdbx").toFile()
    }
}
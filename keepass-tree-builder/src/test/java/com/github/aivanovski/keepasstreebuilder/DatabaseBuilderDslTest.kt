package com.github.aivanovski.keepasstreebuilder

import app.keemobile.kotpass.models.EntryValue
import com.github.aivanovski.keepasstreebuilder.DatabaseBuilderDsl.newBuilder
import com.github.aivanovski.keepasstreebuilder.TestData.COMPOSITE_KEY
import com.github.aivanovski.keepasstreebuilder.TestData.FILE_KEY
import com.github.aivanovski.keepasstreebuilder.TestData.PASSWORD_KEY
import com.github.aivanovski.keepasstreebuilder.TestData.ROOT
import com.github.aivanovski.keepasstreebuilder.TestData.newDatabase
import com.github.aivanovski.keepasstreebuilder.converter.kotpass.KotpassDatabaseConverter
import com.github.aivanovski.keepasstreebuilder.extensions.buildNodeTree
import com.github.aivanovski.keepasstreebuilder.extensions.readDatabase
import com.github.aivanovski.keepasstreebuilder.extensions.toByteArray
import com.github.aivanovski.keepasstreebuilder.extensions.traverse
import com.github.aivanovski.keepasstreebuilder.extensions.write
import com.github.aivanovski.keepasstreebuilder.extensions.writeToFile
import com.github.aivanovski.keepasstreebuilder.generator.EntityFactory.newEntryFrom
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.beInstanceOf
import java.io.File
import java.nio.file.Path
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class DatabaseBuilderDslTest {

    @TempDir
    lateinit var tempDir: Path

    @Test
    fun `dsl should work with password key`() {
        // arrange
        val dbFile = newDbFile()
        dbFile.exists() shouldBe false

        // act
        val db = newDatabase(PASSWORD_KEY)
            .apply {
                writeToFile(dbFile)
            }

        // assert
        val expectedEntities = db.root.traverse { node -> node.originalEntity }

        val actualEntities = dbFile.readDatabase(PASSWORD_KEY)
            .buildNodeTree()
            .traverse { node -> node.entity }

        actualEntities shouldBe expectedEntities
    }

    @Test
    fun `dsl should work with binary key`() {
        // arrange
        val keyFile = newKeyFile()
        val dbFile = newDbFile()

        keyFile.write(FILE_KEY.binaryData)

        keyFile.exists() shouldBe true
        dbFile.exists() shouldBe false

        // act
        val db = newDatabase(FILE_KEY)
            .apply {
                writeToFile(dbFile)
            }

        // assert
        val expectedEntities = db.root.traverse { node -> node.originalEntity }

        val actualEntities = dbFile.readDatabase(FILE_KEY)
            .buildNodeTree()
            .traverse { node -> node.entity }

        actualEntities shouldBe expectedEntities
    }

    @Test
    fun `dsl should work with composite key`() {
        // arrange
        val keyFile = newKeyFile()
        val dbFile = newDbFile()

        keyFile.write(COMPOSITE_KEY.binaryData)

        keyFile.exists() shouldBe true
        dbFile.exists() shouldBe false

        // act
        val db = newDatabase(COMPOSITE_KEY)
            .apply {
                writeToFile(dbFile)
            }

        // assert
        val expectedEntities = db.root.traverse { node -> node.originalEntity }

        val actualEntities = dbFile.readDatabase(COMPOSITE_KEY)
            .buildNodeTree()
            .traverse { node -> node.entity }

        actualEntities shouldBe expectedEntities
    }

    @Test
    fun `toByteArray should work`() {
        // arrange
        val dbFile = newDbFile()
        dbFile.exists() shouldBe false

        // act
        val db = newDatabase(PASSWORD_KEY)
            .apply {
                dbFile.write(this.toByteArray())
            }

        // assert
        val expectedEntities = db.root.traverse { node -> node.originalEntity }

        val actualEntities = dbFile.readDatabase(PASSWORD_KEY)
            .buildNodeTree()
            .traverse { node -> node.entity }

        actualEntities shouldBe expectedEntities
    }

    @Test
    fun `entry values should be valid`() {
        // arrange
        val dbFile = newDbFile()
        val expectedEntry = newEntryFrom(id = 1)

        // act
        newBuilder(KotpassDatabaseConverter())
            .key(PASSWORD_KEY)
            .content(ROOT) {
                entry(expectedEntry)
            }
            .build()
            .apply {
                writeToFile(dbFile)
            }

        // assert
        val actualDb = dbFile.readDatabase(PASSWORD_KEY)

        val group = actualDb.content.group
        group.uuid shouldBe ROOT.uuid
        group.name shouldBe ROOT.fields[Fields.TITLE]

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

    private fun newDbFile(): File {
        return tempDir.resolve("db.kdbx").toFile()
    }

    private fun newKeyFile(): File {
        return tempDir.resolve("key").toFile()
    }
}
package com.github.aivanovski.keepasstreebuilder

import com.github.aivanovski.keepasstreebuilder.TestData.BINARY_KEY_CONTENT
import com.github.aivanovski.keepasstreebuilder.TestData.PASSWORD
import com.github.aivanovski.keepasstreebuilder.TestData.newDatabase
import com.github.aivanovski.keepasstreebuilder.extensions.buildNodeTree
import com.github.aivanovski.keepasstreebuilder.extensions.readDatabase
import com.github.aivanovski.keepasstreebuilder.extensions.toByteArray
import com.github.aivanovski.keepasstreebuilder.extensions.traverse
import com.github.aivanovski.keepasstreebuilder.extensions.write
import com.github.aivanovski.keepasstreebuilder.extensions.writeToFile
import com.github.aivanovski.keepasstreebuilder.model.DatabaseKey
import com.github.aivanovski.keepasstreebuilder.model.DatabaseKey.BinaryKey
import com.github.aivanovski.keepasstreebuilder.model.DatabaseKey.PasswordKey
import io.kotest.matchers.shouldBe
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
        val key = PasswordKey(PASSWORD)
        dbFile.exists() shouldBe false

        // act
        val db = newDatabase(key)
            .apply {
                writeToFile(dbFile)
            }

        // assert
        val expectedEntities = db.root.traverse { node -> node.originalEntity }

        val actualEntities = dbFile.readDatabase(key)
            .buildNodeTree()
            .traverse { node -> node.entity }

        actualEntities shouldBe expectedEntities
    }

    @Test
    fun `dsl should work with binary key`() {
        // arrange
        val keyFile = newKeyFile()
        val dbFile = newDbFile()
        val key = BinaryKey(BINARY_KEY_CONTENT.toByteArray())

        keyFile.write(BINARY_KEY_CONTENT)

        keyFile.exists() shouldBe true
        dbFile.exists() shouldBe false

        // act
        val db = newDatabase(key)
            .apply {
                writeToFile(dbFile)
            }

        // assert
        val expectedEntities = db.root.traverse { node -> node.originalEntity }

        val actualEntities = dbFile.readDatabase(key)
            .buildNodeTree()
            .traverse { node -> node.entity }

        actualEntities shouldBe expectedEntities
    }

    @Test
    fun `dsl should work with composite key`() {
        // arrange
        val keyFile = newKeyFile()
        val dbFile = newDbFile()
        val key = DatabaseKey.CompositeKey(
            password = PASSWORD,
            binaryData = BINARY_KEY_CONTENT.toByteArray()
        )

        keyFile.write(BINARY_KEY_CONTENT)

        keyFile.exists() shouldBe true
        dbFile.exists() shouldBe false

        // act
        val db = newDatabase(key)
            .apply {
                writeToFile(dbFile)
            }

        // assert
        val expectedEntities = db.root.traverse { node -> node.originalEntity }

        val actualEntities = dbFile.readDatabase(key)
            .buildNodeTree()
            .traverse { node -> node.entity }

        actualEntities shouldBe expectedEntities
    }

    @Test
    fun `toByteArray should work`() {
        // arrange
        val dbFile = newDbFile()
        val key = PasswordKey(PASSWORD)
        dbFile.exists() shouldBe false

        // act
        val db = newDatabase(key)
            .apply {
                dbFile.write(this.toByteArray())
            }

        // assert
        val expectedEntities = db.root.traverse { node -> node.originalEntity }

        val actualEntities = dbFile.readDatabase(key)
            .buildNodeTree()
            .traverse { node -> node.entity }

        actualEntities shouldBe expectedEntities
    }

    private fun newDbFile(): File {
        return tempDir.resolve("db.kdbx").toFile()
    }

    private fun newKeyFile(): File {
        return tempDir.resolve("key").toFile()
    }
}
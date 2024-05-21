package com.github.aivanovski.keepasstreebuilder

import app.keemobile.kotpass.database.KeePassDatabase
import app.keemobile.kotpass.models.DatabaseElement
import com.github.aivanovski.keepasstreebuilder.converter.kotpass.KotpassDatabaseConverter
import com.github.aivanovski.keepasstreebuilder.extensions.resourceAsBytes
import com.github.aivanovski.keepasstreebuilder.generator.EntityFactory.newBinaryFrom
import com.github.aivanovski.keepasstreebuilder.generator.EntityFactory.newEntryFrom
import com.github.aivanovski.keepasstreebuilder.generator.EntityFactory.newGroupFrom
import com.github.aivanovski.keepasstreebuilder.model.Database
import com.github.aivanovski.keepasstreebuilder.model.DatabaseKey
import com.github.aivanovski.keepasstreebuilder.model.EntryEntity

object TestData {

    private const val PASSWORD = "abc123"
    private const val BINARY_KEY_CONTENT = "abcdefghij1234567890"

    val PASSWORD_KEY = DatabaseKey.PasswordKey(PASSWORD)
    val FILE_KEY = DatabaseKey.BinaryKey(BINARY_KEY_CONTENT.toByteArray())
    val COMPOSITE_KEY = DatabaseKey.CompositeKey(PASSWORD, BINARY_KEY_CONTENT.toByteArray())

    val ROOT_GROUP = newGroupFrom(id = "Root")

    val ENTRY_WITH_HISTORY = newEntryFrom(
        id = 101,
        history = listOf(
            newEntryFrom(
                id = 101,
                custom = mapOf("Field 1" to "Version 1")
            ),
            newEntryFrom(
                id = 101,
                custom = mapOf("Field 1" to "Version 2")
            )
        )
    )

    private val SVG_BINARY = newBinaryFrom(
        name = "favorite.png",
        content = resourceAsBytes("favorite.png")
    )

    private val PNG_BINARY = newBinaryFrom(
        name = "favorite.svg",
        content = resourceAsBytes("favorite.svg")
    )

    private val TEXT_BINARY = newBinaryFrom(
        name = "text.txt",
        content = "Dummy text".toByteArray()
    )

    val ENTRY_WITH_BINARIES = newEntryFrom(
        id = 201,
        binaries = listOf(SVG_BINARY, PNG_BINARY, TEXT_BINARY)
    )

    fun newDatabase(
        key: DatabaseKey
    ): Database<DatabaseElement, KeePassDatabase> {
        return DatabaseBuilderDsl.newBuilder(KotpassDatabaseConverter())
            .key(key)
            .content(ROOT_GROUP) {
                group(newGroupFrom('A')) {
                    entry(newEntryFrom(1))
                    entry(newEntryFrom(2))
                }
                group(newGroupFrom('B')) {
                    group(newGroupFrom('C'))
                    entry(newEntryFrom(3))
                }
            }
            .build()
    }

    fun newDatabaseWithHistory(
        key: DatabaseKey
    ): Database<DatabaseElement, KeePassDatabase> {
        return DatabaseBuilderDsl.newBuilder(KotpassDatabaseConverter())
            .key(key)
            .content(ROOT_GROUP) {
                entry(ENTRY_WITH_HISTORY)
            }
            .build()
    }

    fun newDatabaseWithBinaries(
        key: DatabaseKey
    ): Database<DatabaseElement, KeePassDatabase> {
        return DatabaseBuilderDsl.newBuilder(KotpassDatabaseConverter())
            .key(key)
            .content(ROOT_GROUP) {
                entry(ENTRY_WITH_BINARIES)
            }
            .build()
    }
}
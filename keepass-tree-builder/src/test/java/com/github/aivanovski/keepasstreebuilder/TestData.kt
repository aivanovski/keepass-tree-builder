package com.github.aivanovski.keepasstreebuilder

import app.keemobile.kotpass.database.KeePassDatabase
import app.keemobile.kotpass.models.DatabaseElement
import com.github.aivanovski.keepasstreebuilder.converter.kotpass.KotpassDatabaseConverter
import com.github.aivanovski.keepasstreebuilder.generator.EntityFactory.newEntryFrom
import com.github.aivanovski.keepasstreebuilder.generator.EntityFactory.newGroupFrom
import com.github.aivanovski.keepasstreebuilder.model.Database
import com.github.aivanovski.keepasstreebuilder.model.DatabaseKey

object TestData {

    private const val PASSWORD = "abc123"
    private const val BINARY_KEY_CONTENT = "abcdefghij1234567890"

    val PASSWORD_KEY = DatabaseKey.PasswordKey(PASSWORD)
    val FILE_KEY = DatabaseKey.BinaryKey(BINARY_KEY_CONTENT.toByteArray())
    val COMPOSITE_KEY = DatabaseKey.CompositeKey(PASSWORD, BINARY_KEY_CONTENT.toByteArray())

    val ROOT = newGroupFrom(id = "Root")

    private val ENTRY_WITH_HISTORY = newEntryFrom(id = 101)
    private val HISTORY = listOf(
        ENTRY_WITH_HISTORY.copy(
            fields = ENTRY_WITH_HISTORY.fields.plus(
                "Field 1" to "Version 1"
            )
        ),
        ENTRY_WITH_HISTORY.copy(
            fields = ENTRY_WITH_HISTORY.fields.plus(
                "Field 1" to "Version 2"
            )
        )
    )


    fun newDatabase(
        key: DatabaseKey
    ): Database<DatabaseElement, KeePassDatabase> {
        return DatabaseBuilderDsl.newBuilder(KotpassDatabaseConverter())
            .key(key)
            .content(ROOT) {
                group(newGroupFrom('A')) {
                    entry(newEntryFrom(1))
                    entry(newEntryFrom(2))
                }
                group(newGroupFrom('B')) {
                    group(newGroupFrom('C'))
                    entry(newEntryFrom(3))
                }
                entry(newEntryFrom(ENTRY_WITH_HISTORY, history = HISTORY))
            }
            .build()
    }
}
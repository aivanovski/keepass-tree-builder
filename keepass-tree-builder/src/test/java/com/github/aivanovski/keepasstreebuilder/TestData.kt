package com.github.aivanovski.keepasstreebuilder

import app.keemobile.kotpass.database.KeePassDatabase
import app.keemobile.kotpass.models.DatabaseElement
import com.github.aivanovski.keepasstreebuilder.converter.kotpass.KotpassDatabaseConverter
import com.github.aivanovski.keepasstreebuilder.generator.EntityGenerator.newEntryFrom
import com.github.aivanovski.keepasstreebuilder.generator.EntityGenerator.newGroupFrom
import com.github.aivanovski.keepasstreebuilder.model.Database
import com.github.aivanovski.keepasstreebuilder.model.DatabaseKey

object TestData {

    const val PASSWORD = "abc123"
    const val BINARY_KEY_CONTENT = "abcdefghij1234567890"

    fun newDatabase(
        key: DatabaseKey
    ): Database<DatabaseElement, KeePassDatabase> {
        return DatabaseBuilderDsl.newBuilder(KotpassDatabaseConverter())
            .key(key)
            .content(newGroupFrom('R')) {
                group(newGroupFrom('A')) {
                    entry(newEntryFrom(1))
                    entry(newEntryFrom(2))
                }
                group(newGroupFrom('B')) {
                    group(newGroupFrom('C'))
                    entry(newEntryFrom(3))
                }
                entry(newEntryFrom(4))
            }
            .build()
    }
}
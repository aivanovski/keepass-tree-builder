package com.github.aivanovski.keepasstreebuilder.generator

import com.github.aivanovski.keepasstreebuilder.Fields
import com.github.aivanovski.keepasstreebuilder.model.EntryEntity
import com.github.aivanovski.keepasstreebuilder.model.GroupEntity
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID

object EntityGenerator {

    private const val ENTRY_UID_SHIFT = 1L
    private const val GROUP_UID_SHIFT = 0xFFL

    private val CREATED_TIMESTAMP = LocalDate.parse("2020-01-01").toInstant()
    private val MODIFIED_TIMESTAMP = LocalDate.parse("2020-01-02").toInstant()

    fun newGroupFrom(
        id: Any,
        uuid: UUID = groupUuid(id),
        title: String = "Group $id"
    ): GroupEntity {
        return GroupEntity(
            uuid = uuid,
            fields = mapOf(
                Fields.TITLE to title
            )
        )
    }

    fun newEntryFrom(
        id: Any,
        uuid: UUID = entryUuid(id),
        title: String = "Entry $id"
    ): EntryEntity {
        return EntryEntity(
            uuid = uuid,
            created = CREATED_TIMESTAMP,
            modified = MODIFIED_TIMESTAMP,
            expires = null,
            fields = mapOf(
                Fields.TITLE to title,
                Fields.USERNAME to "UserName $id",
                Fields.PASSWORD to "Password $id",
                Fields.URL to "URL $id",
                Fields.NOTES to "Notes $id"
            )
        )
    }

    private fun groupUuid(id: Any): UUID {
        return UUID(GROUP_UID_SHIFT, id.hashCode().toLong())
    }

    private fun entryUuid(id: Any): UUID {
        return UUID(ENTRY_UID_SHIFT, id.hashCode().toLong())
    }

    private fun LocalDate.toInstant(): Instant {
        return this.atStartOfDay()
            .atZone(ZoneId.systemDefault())
            .toInstant()
    }
}
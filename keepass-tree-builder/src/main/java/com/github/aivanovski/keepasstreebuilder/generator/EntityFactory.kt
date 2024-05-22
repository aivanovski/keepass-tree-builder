package com.github.aivanovski.keepasstreebuilder.generator

import com.github.aivanovski.keepasstreebuilder.Fields
import com.github.aivanovski.keepasstreebuilder.model.Binary
import com.github.aivanovski.keepasstreebuilder.model.EntryEntity
import com.github.aivanovski.keepasstreebuilder.model.GroupEntity
import com.github.aivanovski.keepasstreebuilder.utils.ShaUtils
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID

object EntityFactory {

    private const val ENTRY_UID_SHIFT = 1L
    private const val GROUP_UID_SHIFT = 0xFFL

    private val CREATED_TIMESTAMP = LocalDate.parse("2020-01-01").toInstant()
    private val MODIFIED_TIMESTAMP = LocalDate.parse("2020-01-02").toInstant()
    private val EXPIRATION_TIMESTAMP = LocalDate.parse("2030-01-03").toInstant()

    fun newGroupFrom(
        id: Any,
        uuid: UUID = newGroupUuid(id),
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
        uuid: UUID = newEntryUuid(id),
        title: String = "Entry $id",
        history: List<EntryEntity> = emptyList(),
        custom: Map<String, String> = emptyMap(),
        binaries: List<Binary> = emptyList()
    ): EntryEntity {
        val defaultFields = mapOf(
            Fields.TITLE to title,
            Fields.USERNAME to "UserName $id",
            Fields.PASSWORD to "Password $id",
            Fields.URL to "URL $id",
            Fields.NOTES to "Notes $id"
        )

        return EntryEntity(
            uuid = uuid,
            created = CREATED_TIMESTAMP,
            modified = MODIFIED_TIMESTAMP,
            expires = EXPIRATION_TIMESTAMP,
            fields = defaultFields.plus(custom),
            history = history,
            binaries = binaries
        )
    }

    fun newBinaryFrom(
        name: String,
        content: ByteArray
    ): Binary {
        return Binary(
            name = name,
            hash = ShaUtils.sha256(content),
            data = content
        )
    }

    private fun newGroupUuid(id: Any): UUID {
        return UUID(GROUP_UID_SHIFT, id.hashCode().toLong())
    }

    private fun newEntryUuid(id: Any): UUID {
        return UUID(ENTRY_UID_SHIFT, id.hashCode().toLong())
    }

    private fun LocalDate.toInstant(): Instant {
        return this.atStartOfDay()
            .atZone(ZoneId.systemDefault())
            .toInstant()
    }
}
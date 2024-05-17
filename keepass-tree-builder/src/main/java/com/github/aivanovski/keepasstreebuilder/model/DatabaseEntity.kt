package com.github.aivanovski.keepasstreebuilder.model

import java.time.Instant
import java.util.UUID

sealed interface DatabaseEntity {
    val uuid: UUID
}

data class GroupEntity(
    override val uuid: UUID,
    val fields: Map<String, String>
) : DatabaseEntity

data class EntryEntity(
    override val uuid: UUID,
    val created: Instant,
    val modified: Instant,
    val expires: Instant?,
    val fields: Map<String, String>,
    val history: List<EntryEntity>
) : DatabaseEntity
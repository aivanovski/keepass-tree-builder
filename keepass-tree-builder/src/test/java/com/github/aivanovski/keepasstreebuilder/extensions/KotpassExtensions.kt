package com.github.aivanovski.keepasstreebuilder.extensions

import app.keemobile.kotpass.database.KeePassDatabase
import app.keemobile.kotpass.database.modifiers.binaries
import app.keemobile.kotpass.models.BinaryData
import app.keemobile.kotpass.models.Entry
import app.keemobile.kotpass.models.Group
import com.github.aivanovski.keepasstreebuilder.Fields
import com.github.aivanovski.keepasstreebuilder.model.Binary
import com.github.aivanovski.keepasstreebuilder.model.DatabaseEntity
import com.github.aivanovski.keepasstreebuilder.model.DatabaseNode
import com.github.aivanovski.keepasstreebuilder.model.EntryEntity
import com.github.aivanovski.keepasstreebuilder.model.GroupEntity
import com.github.aivanovski.keepasstreebuilder.model.Hash
import com.github.aivanovski.keepasstreebuilder.model.HashType
import com.github.aivanovski.keepasstreebuilder.model.MutableDatabaseNode
import java.time.Instant
import java.util.LinkedList
import okio.ByteString

fun KeePassDatabase.buildNodeTree(): DatabaseNode<DatabaseEntity> {
    val rootGroup = content.group

    val root: MutableDatabaseNode<DatabaseEntity> = MutableDatabaseNode(
        entity = rootGroup.toEntity(),
        originalEntity = rootGroup.toEntity()
    )

    val groups = LinkedList<Pair<MutableDatabaseNode<DatabaseEntity>, Group>>()
    groups.add(Pair(root, rootGroup))

    while (groups.isNotEmpty()) {
        val (node, group) = groups.poll()

        for (childGroup in group.groups) {
            val childNode = MutableDatabaseNode<DatabaseEntity>(
                entity = childGroup.toEntity(),
                originalEntity = childGroup.toEntity()
            )

            node.nodes.add(childNode)
            groups.push(Pair(childNode, childGroup))
        }

        for (entry in group.entries) {
            val entryNode = MutableDatabaseNode<DatabaseEntity>(
                entity = entry.toEntity(allBinaries = binaries),
                originalEntity = entry.toEntity(allBinaries = binaries)
            )

            node.nodes.add(entryNode)
        }
    }

    return root
}

private fun Group.toEntity(): GroupEntity {
    return GroupEntity(
        uuid = uuid,
        fields = mapOf(
            Fields.TITLE to name
        )
    )
}

private fun Entry.toEntity(allBinaries: Map<ByteString, BinaryData> = emptyMap()): EntryEntity {
    val fields = mutableMapOf<String, String>()

    for ((key, value) in this.fields.entries) {
        fields[key] = value.content
    }

    val historyEntities = history.map { entry -> entry.toEntity(allBinaries) }
    val binaryEntries = binaries.mapNotNull { binaryRef ->
        val key = binaryRef.hash
        val data = allBinaries[key]?.rawContent ?: return@mapNotNull null

        Binary(
            name = binaryRef.name,
            hash = Hash(type = HashType.SHA_256, data = binaryRef.hash.toByteArray()),
            data = data
        )
    }

    return EntryEntity(
        uuid = uuid,
        created = times?.creationTime ?: Instant.now(),
        modified = times?.lastModificationTime ?: Instant.now(),
        expires = times?.expiryTime,
        fields = fields,
        history = historyEntities,
        binaries = binaryEntries
    )
}
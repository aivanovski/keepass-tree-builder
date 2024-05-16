package com.github.aivanovski.keepasstreebuilder

import com.github.aivanovski.keepasstreebuilder.converter.Converter
import com.github.aivanovski.keepasstreebuilder.model.Database
import com.github.aivanovski.keepasstreebuilder.model.DatabaseKey
import com.github.aivanovski.keepasstreebuilder.model.DatabaseNode
import com.github.aivanovski.keepasstreebuilder.model.EntryEntity
import com.github.aivanovski.keepasstreebuilder.model.GroupEntity
import com.github.aivanovski.keepasstreebuilder.model.MutableDatabaseNode

object DatabaseBuilderDsl {

    fun <Group, Entry, Element, DB> newBuilder(
        converter: Converter<Group, Entry, Element, DB>
    ): DatabaseBuilder<Group, Entry, Element, DB> {
        return DatabaseBuilder(converter)
    }

    class DatabaseBuilder<Group, Entry, Element, DB> internal constructor(
        private val converter: Converter<Group, Entry, Element, DB>
    ) {

        private lateinit var root: GroupEntity
        private lateinit var content: DatabaseTreeBuilder<Group, Entry, Element>.() -> Unit
        private lateinit var key: DatabaseKey

        fun content(
            root: GroupEntity,
            content: DatabaseTreeBuilder<Group, Entry, Element>.() -> Unit
        ): DatabaseBuilder<Group, Entry, Element, DB> {
            this.root = root
            this.content = content
            return this
        }

        fun key(key: DatabaseKey): DatabaseBuilder<Group, Entry, Element, DB> {
            this.key = key
            return this
        }

        fun build(): Database<Element, DB> {
            val rootNode = DatabaseTreeBuilder(
                element = root,
                converter = converter
            )
                .apply {
                    content.invoke(this)
                }
                .build()

            return converter.createDatabase(key, rootNode as DatabaseNode<Group>)
        }
    }

    class DatabaseTreeBuilder<Group, Entry, Element>(
        private val element: GroupEntity,
        private val converter: Converter<Group, Entry, Element, *>
    ) {

        private val nodes = mutableListOf<MutableDatabaseNode<Element>>()
        private val groups = mutableListOf<Group>()
        private val entries = mutableListOf<Entry>()

        fun group(
            group: GroupEntity,
            content: (DatabaseTreeBuilder<Group, Entry, Element>.() -> Unit)? = null
        ) {
            val groupNode = DatabaseTreeBuilder(
                element = group,
                converter = converter
            )
                .apply {
                    content?.invoke(this)
                }
                .build()

            groups.add(groupNode.entity as Group)
            nodes.add(groupNode)
        }

        fun entry(entry: EntryEntity) {
            val node = MutableDatabaseNode(
                entity = converter.createEntry(entry),
                originalEntity = entry
            )
            entries.add(node.entity)
            nodes.add(node as MutableDatabaseNode<Element>)
        }

        fun build(): MutableDatabaseNode<Element> {
            return MutableDatabaseNode(
                entity = converter.createGroup(
                    group = element,
                    groups = groups,
                    entries = entries
                ) as Element,
                originalEntity = element,
                nodes = nodes
            )
        }
    }
}
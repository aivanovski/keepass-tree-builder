package com.github.aivanovski.keepasstreebuilder.converter

import com.github.aivanovski.keepasstreebuilder.model.Database
import com.github.aivanovski.keepasstreebuilder.model.DatabaseKey
import com.github.aivanovski.keepasstreebuilder.model.DatabaseNode
import com.github.aivanovski.keepasstreebuilder.model.EntryEntity
import com.github.aivanovski.keepasstreebuilder.model.GroupEntity
import com.github.aivanovski.keepasstreebuilder.model.KeyHashingAlgorithm

interface Converter<Group, Entry, Element, DB> {

    fun createEntry(entry: EntryEntity): Entry

    fun createGroup(
        group: GroupEntity,
        groups: List<Group>,
        entries: List<Entry>
    ): Group

    fun createDatabase(
        key: DatabaseKey,
        root: DatabaseNode<Group>,
        keyHashingAlgorithm: KeyHashingAlgorithm
    ): Database<Element, DB>
}
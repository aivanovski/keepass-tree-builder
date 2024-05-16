package com.github.aivanovski.keepasstreebuilder.model

sealed interface DatabaseNode<T> {
    val entity: T
    val originalEntity: DatabaseEntity
    val nodes: List<DatabaseNode<T>>
}

data class MutableDatabaseNode<T>(
    override val entity: T,
    override val originalEntity: DatabaseEntity,
    override val nodes: MutableList<MutableDatabaseNode<T>> = mutableListOf()
) : DatabaseNode<T>
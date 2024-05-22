package com.github.aivanovski.keepasstreebuilder.extensions

import com.github.aivanovski.keepasstreebuilder.model.DatabaseNode
import java.util.LinkedList

fun <T, R> DatabaseNode<T>.traverseAndCollect(transform: (node: DatabaseNode<T>) -> R): List<R> {
    val nodes = LinkedList<DatabaseNode<T>>()
    nodes.add(this)

    val result = mutableListOf<R>()
    while (nodes.isNotEmpty()) {
        repeat(nodes.size) {
            val node = nodes.removeFirst()

            result.add(transform.invoke(node))

            for (child in node.nodes) {
                nodes.add(child)
            }
        }
    }

    return result
}
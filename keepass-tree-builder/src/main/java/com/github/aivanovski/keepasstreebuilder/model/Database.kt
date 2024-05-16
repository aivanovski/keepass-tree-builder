package com.github.aivanovski.keepasstreebuilder.model

import java.io.InputStream

data class Database<Element, DB>(
    val underlying: DB,
    val root: DatabaseNode<Element>,
    val contentFactory: () -> InputStream
)
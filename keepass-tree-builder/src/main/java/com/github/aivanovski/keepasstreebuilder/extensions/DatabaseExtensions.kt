package com.github.aivanovski.keepasstreebuilder.extensions

import com.github.aivanovski.keepasstreebuilder.model.Database
import java.io.File
import java.io.FileOutputStream

fun Database<*, *>.writeToFile(file: File) {
    val input = contentFactory.invoke()

    FileOutputStream(file, false).use { out ->
        input.copyTo(out)
        out.flush()
    }

    input.close()
}

fun Database<*, *>.toByteArray(): ByteArray {
    return contentFactory.invoke().use { input ->
        input.readAllBytes()
    }
}
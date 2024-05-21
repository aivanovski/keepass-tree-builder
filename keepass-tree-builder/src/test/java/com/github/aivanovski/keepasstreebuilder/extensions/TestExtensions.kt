package com.github.aivanovski.keepasstreebuilder.extensions

import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream

fun File.write(bytes: ByteArray) {
    BufferedOutputStream(FileOutputStream(this, false)).use { out ->
        out.write(bytes, 0, bytes.size)
        out.flush()
    }
}

fun Any.resourceAsBytes(name: String): ByteArray {
    val stream = this.javaClass.classLoader.getResourceAsStream(name)
    checkNotNull(stream)

    return stream.readAllBytes()
}
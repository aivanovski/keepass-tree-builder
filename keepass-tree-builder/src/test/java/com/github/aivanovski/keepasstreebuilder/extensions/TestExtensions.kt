package com.github.aivanovski.keepasstreebuilder.extensions

import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

fun File.write(data: String) {
    write(data.toByteArray())
}

fun File.write(bytes: ByteArray) {
    BufferedOutputStream(FileOutputStream(this, false)).use { out ->
        out.write(bytes, 0, bytes.size)
        out.flush()
    }
}

fun File.read(): ByteArray {
    return FileInputStream(this).readAllBytes()
}
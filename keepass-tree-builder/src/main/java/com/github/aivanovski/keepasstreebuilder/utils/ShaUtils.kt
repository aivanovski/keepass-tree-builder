package com.github.aivanovski.keepasstreebuilder.utils

import com.github.aivanovski.keepasstreebuilder.model.Hash
import com.github.aivanovski.keepasstreebuilder.model.HashType
import java.security.MessageDigest

object ShaUtils {

    private const val SHA_256 = "SHA-256"

    fun sha256(bytes: ByteArray): Hash {
        val digest = MessageDigest.getInstance(SHA_256)
        val sha = digest.digest(bytes)
        return Hash(
            type = HashType.SHA_256,
            data = sha
        )
    }
}
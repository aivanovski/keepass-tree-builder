package com.github.aivanovski.keepasstreebuilder.model

sealed class DatabaseKey {

    data class PasswordKey(
        val password: String
    ) : DatabaseKey()

    data class BinaryKey(
        val binaryData: ByteArray
    ) : DatabaseKey() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as BinaryKey

            return binaryData.contentEquals(other.binaryData)
        }

        override fun hashCode(): Int {
            return binaryData.contentHashCode()
        }
    }

    data class CompositeKey(
        val password: String,
        val binaryData: ByteArray
    ) : DatabaseKey() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as CompositeKey

            if (password != other.password) return false
            if (!binaryData.contentEquals(other.binaryData)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = password.hashCode()
            result = 31 * result + binaryData.contentHashCode()
            return result
        }
    }
}
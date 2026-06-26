package com.github.aivanovski.keepasstreebuilder.model

sealed class KeyHashingAlgorithm {

    data class Aes(val transformationRounds: ULong) : KeyHashingAlgorithm() {

        companion object {
            fun default() =
                Aes(
                    transformationRounds = 6000.toULong()
                )
        }
    }

    data class Argon2d(
        val parallelism: UInt,
        val memory: ULong,
        val iterations: ULong,
        val version: UInt
    ) : KeyHashingAlgorithm() {

        companion object {
            fun default() =
                Argon2d(
                    parallelism = 2U,
                    memory = 32UL * 1024UL * 1024UL,
                    iterations = 8U,
                    version = 0x13.toUInt()
                )
        }
    }

    data class Argon2id(
        val parallelism: UInt,
        val memory: ULong,
        val iterations: ULong,
        val version: UInt
    ) : KeyHashingAlgorithm() {

        companion object {
            fun default() =
                Argon2id(
                    parallelism = 2U,
                    memory = 32UL * 1024UL * 1024UL,
                    iterations = 8U,
                    version = 0x13.toUInt()
                )
        }
    }
}
package com.github.aivanovski.keepasstreebuilder.extensions

import com.github.aivanovski.keepasstreebuilder.model.Hash
import okio.ByteString
import okio.ByteString.Companion.toByteString

fun Hash.toByteString(): ByteString = data.toByteString(0, data.size)
package com.kana_tutor.utils

fun String.codePointSplit(): List<String> {
    val rv = mutableListOf<String>()
    var idx = 0
    while (idx < length) {
        val len = if (this.hasSurrogatePairAt(idx)) 2 else 1
        rv.add(this.substring(idx, idx + len))
        idx += len
    }
    return rv
}

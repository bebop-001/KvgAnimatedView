package com.kana_tutor.kvgviewer

import java.lang.RuntimeException

// based on https://www.baeldung.com/kotlin-builder-pattern
class KvgChar private constructor(
        val name:Char,
        val paths: Array<KvgPath>,
        val annotations : Array<KvgAnnotation>) {
    class KvgPath(val op : String, val coord : Array<Float>)
    class KvgAnnotation(val op : String, val point : Pair<Float,Float>)

    class Builder {
        var name : Char? = null
        var paths = mutableListOf<KvgPath>()
        var annotations = mutableListOf<KvgAnnotation>()
        fun name (name : Char) = apply{this.name = name}
        fun s (op : String, coord : Array<Float>) =
            apply {paths.add(KvgPath(op, coord))}
        fun annotation (text : String, point : Pair<Float,Float>) =
            apply {this.annotations.add(KvgAnnotation(text, point))}
        fun build () : KvgChar {
            if (name == null) {
                throw RuntimeException("Attempt to build KvgChar without name")
            }
            return KvgChar (name!!, paths.toTypedArray(), annotations.toTypedArray())
        }
    }
}
package com.kana_tutor.animate

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import com.kana_tutor.kvgviewer.KvgViewer.Companion.externalStorageRoot
import com.kana_tutor.utils.ObservedPair
import com.kana_tutor.utils.codePointSplit
import java.io.File
import java.nio.charset.Charset
import java.util.zip.ZipFile


@Suppress("unused")
private const val TAG = "AnimatorInfo"
@Suppress("FoldInitializerAndIfToElvis", "LocalVariableName",
    "PropertyName")
class AnimatorInfo {
    companion object {
        val initResults = ObservedPair(Pair(false, ""))

        private var zf: ZipFile? = null
        private val zipFile: ZipFile?
            get() = zf

        // Each record has info on a number of kanji.  the id
        // is the name of the record as found in a zip file and
        // is the character-range of the kanji in tha record.
        // The path of the record contains the id of the animated
        // character as "kanji.style.avg", the offset into the
        // s
        data class PathRecord (
            val pathId: String, val recordId: String,
            val offset: Int, val length: Int, val stroke_count: Int
        ) {
            val selectors = listOf(offset, length, stroke_count)
        }
        // pathId is key.
        private val byKanji = mutableMapOf<String, MutableMap<String, PathRecord>>()
        val pathIdByKanji : Map<String, Map<String, PathRecord>> = byKanji

        fun getPathData(pathId: String): String {
            val kanji = pathId.codePointSplit().first()
            val pathRecord = pathIdByKanji[kanji]?.get(pathId)
            if (pathRecord == null)
                throw RuntimeException("$TAG: Failed to find pathRecord:" +
                        "$kanji->$pathId")
            val zipEntry = zipFile!!.getEntry(pathRecord.recordId)
            val recordLines = zipFile!!.getInputStream(zipEntry)
                .readBytes()
                .toString(Charset.forName("UTF-8"))
                .split("\n")
            val (offset, len) = pathRecord.selectors
            @SuppressLint("InlinedApi")
            val rv = recordLines.subList(offset, len + offset + 1)
                .joinToString("\n")
            return rv
        }

        fun initialize() {
            if (zipFile != null) {
                initResults.value = true to "success"
            }
            try {
                // Make sure we have one and only one zip file.
                val names= externalStorageRoot.list()
                    ?.filter { it.contains("""^kvgPaths(-\d+).zip$""".toRegex()) }
                if (names.isNullOrEmpty()) {
                    initResults.value = false to "no \"kvgPaths-NNN.avg\" found"
                }
                else if (names.size > 1) {
                    initResults.value = false to "Multiple avg path files found: $names"
                }
                else {
                    // found a valid zip file.
                    val zff = File(externalStorageRoot, names[0])
                    zf = if (Build.VERSION.SDK_INT >= 24) {
                        ZipFile(zff, Charset.forName("UTF-8"))
                    } else {
                        ZipFile(zff)
                    }

                    val tocEntryName = "paths/avg.toc.txt"
                    val tocLines = zipFile!!.getInputStream(
                        zipFile!!.getEntry(tocEntryName)
                    ).readBytes()
                        .toString(Charset.forName("UTF-8"))
                        .split("""\s*\n\s*""".toRegex())
                        .toList()
                    val fileRecordRegex = """\s+((\S)(?:-\S+)*\.avg)
                        ([0-9a-fA-F]{3})    # offset into record file in line feeds
                        ([0-9a-fA-F]{2})    # length of path info in line feeds
                        ([0-9a-fA-F]{2})    # number of paths in record
                        """.trimIndent().toRegex(RegexOption.COMMENTS)
                    byKanji.clear()

                    for (line in tocLines) {
                        if (line.startsWith("kvgVersion")) {
                            Log.d(TAG, "$line\n")
                            continue
                        }
                        val recordId = line.split("""\s*=\s*""".toRegex()).first()
                        val found = fileRecordRegex.findAll(line).toList()
                        if (found.isEmpty())
                            throw RuntimeException("$TAG: Bad record: Failed to parse $line")
                        found.map {
                            val (pathId, kanji, a, b, stroke_count) = it.groupValues.takeLast(5)
                            byKanji.getOrPut(kanji){ mutableMapOf() }[pathId] =
                                PathRecord(pathId, recordId,
                                a.toInt(16), b.toInt(16),
                                    stroke_count.toInt(16)
                            )
                        }
                    }
                    initResults.value = true to "success: Found $zipFile"
                }
            }
            catch (e: Exception) {
                initResults.value = false to "Failed tp parse paths file: ${e.message}"
            }
        }
    }
}

@file:Suppress("SimplifiableCallChain")

package com.kana_tutor.animate

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import com.kana_tutor.kvgviewer.KvgViewer.Companion.appContext
import com.kana_tutor.kvgviewer.KvgViewer.Companion.externalStorageRoot
import com.kana_tutor.kvgviewer.R
import com.kana_tutor.utils.codePointSplit
import java.io.File
import java.io.InputStream
import java.nio.charset.Charset
import java.util.zip.ZipFile

@Suppress("unused")
private const val TAG = "AnimatorInfo"
@Suppress("FoldInitializerAndIfToElvis", "LocalVariableName",
    "PropertyName")
class AnimatorInfo {
    companion object {
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
        fun indexedKanjiSort(kanji: Collection<String>): List<String> {
            val byStrokeCount = mutableMapOf<Int,MutableSet<String>>()
            kanji.filter { pathIdByKanji.containsKey(it) }
                .map { k ->
                    val pathRecords: Collection<PathRecord> =
                        pathIdByKanji[k]!!.values
                    val sc = pathRecords.map{it.stroke_count}.min()
                    byStrokeCount.getOrPut(sc){ mutableSetOf()}
                        .add(k)
                }
            val rv = mutableListOf<String>()
            byStrokeCount.keys.sorted().map{ sc ->
                rv.add(sc.toString())
                rv.addAll(byStrokeCount[sc]!!.sorted())
            }
            return rv
        }

        fun initialize() {
            // Check to see that we have a local zip file.
            // If we don't have one, make a copy of the
            // file from res/raw.  We need a local copy
            // because we need system read ZipFile library
            // direct access to the files within the zip
            // and res-raw allows only stream access.
            val zipFileName = "kvg_paths.zip"
            val resRawZipResourceId = R.raw.kvg_paths_04272025
            val zff = File(externalStorageRoot, zipFileName)
            if (!zff.exists()) {
                val zipIn: InputStream = appContext.resources
                    .openRawResource(resRawZipResourceId)
                val zipOut = zff.outputStream()
                val bytes = zipIn.readBytes()
                zipOut.write(bytes)
                zipIn.close()
                zipOut.close()
            }
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
        }
    }
}

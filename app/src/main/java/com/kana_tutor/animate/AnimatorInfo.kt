package com.kana_tutor.animate

import android.annotation.SuppressLint
import android.os.Build
import com.kana_tutor.kvgviewer.KvgViewer.Companion.externalStorageRoot
import com.kana_tutor.utils.ObservedPair
import java.io.File
import java.nio.charset.Charset
import java.util.zip.ZipFile


@Suppress("unused")
private const val TAG = "AnimatorInfo"
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
        // zipped record and the length of the record.
        data class PathRecord (
            val pathId: String, val recordId: String,
            val offset: Int, val length: Int
        ) {
            val selectors = listOf(offset, length)
        }
        // pathId is key.
        val recordsById = mutableMapOf<String,PathRecord>()

        fun getPathData(pathId: String): String {
            val pathRecord = recordsById[pathId]!!
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
            // Make sure we have one and only one zip file.
            val names= externalStorageRoot.list()
                ?.filter { it.contains("""^kvgPaths(-\d+).zip$""".toRegex()) }
            if (names.isNullOrEmpty()) {
                initResults.value = false to "no \"kvgPaths-NNN.avg\" found"
            }
            else if (names.size > 1) {
                initResults.value = false to "Multiple evg path files found: $names"
            }
            else {
                // found a valid zip file.
                val zff = File(externalStorageRoot, names[0])
                zf = if (Build.VERSION.SDK_INT >= 24) {
                    ZipFile(zff, Charset.forName("UTF-8"))
                }
                else {
                    ZipFile(zff)
                }

                val tocEntryName = "paths/avg.toc.txt"
                val tocLines = zipFile!!.getInputStream(
                    zipFile!!.getEntry(tocEntryName)
                ).readBytes()
                .toString(Charset.forName("UTF-8"))
                    .split("""\s*\n\s*""".toRegex())
                    .toList()
                val fileRecordRegex = """(\S+)([0-9a-fA-F]{3})([0-9a-fA-F]{2})""".toRegex()
                recordsById.clear()
                for(line in tocLines) {
                    val recordId = line.split("""\s*=\s*""".toRegex()).first()
                    fileRecordRegex.findAll(line)
                        .toList().map{
                            val (pathId, a, b) = it.groupValues.takeLast(3)
                            recordsById[pathId] = PathRecord(
                                pathId, recordId,
                                a.toInt(16), b.toInt(16)
                            )
                        }
                }
                initResults.value = true to "success: Found $zipFile"
            }
        }
    }
}

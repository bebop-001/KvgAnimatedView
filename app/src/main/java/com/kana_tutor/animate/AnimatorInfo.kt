package com.kana_tutor.animate

import android.os.Build
import android.util.Log
import android.widget.Toast
import com.kana_tutor.kvgviewer.KvgViewer.Companion.appContext
import com.kana_tutor.kvgviewer.KvgViewer.Companion.externalStorageRoot
import com.kana_tutor.utils.ObservedPair
import java.io.File
import java.nio.charset.Charset
import java.util.zip.ZipFile


private const val TAG = "AnimatorInfo"
class AnimatorInfo {
    companion object {
        val initResults = ObservedPair(Pair(false, ""))

        // map char range -> { char -> file name}
        private val filesInfo = mutableMapOf<CharRange,
                MutableMap<Char, MutableList<String>>>()
        private val toFname = mutableMapOf<CharRange, String>()
        val charRangeToFname: Map<CharRange, String> = toFname
        val charToFilesInfo: Map<CharRange, Map<Char, List<String>>> =
            filesInfo
        private val kanji = mutableSetOf<Char>()
        val supportedKanji: Set<Char> = kanji
        private val styles = mutableSetOf<String>()
        val supportedStyles: Set<String> = styles
        private val aniFiles = mutableSetOf<String>()
        val animatorFiles: Set<String> = aniFiles
        private var zf: ZipFile? = null
        val zipFile: ZipFile?
            get() = zf
        private val tocf: File? = null
        val tocFile: File?
            get() = tocf

        // Each record has info on a number of kanji.  the id
        // is the name of the record as found in a zip file and
        // is the character-range of the kanji in tha record.
        // The path of the record contains the id of the animated
        // character as "kanji.style.avg", the offset into the
        // zipped record and the length of the record.
        data class PathRecord (
            val id: String, val offset: Int, val length: Int
        )
        val recordsById = mutableMapOf<String, List<PathRecord>>()

        @Suppress("SpellCheckingInspection")
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
                    val id = line.split("""\s*=\s*""".toRegex()).first()
                    val fileRecords = fileRecordRegex.findAll(line)
                        .toList().map{
                            val (r, a, b) = it.groupValues.takeLast(3)
                            PathRecord(r, a.toInt(16), b.toInt(16))
                        }
                    recordsById[id] = fileRecords
                }
                initResults.value = true to "success: Found $zipFile"
                /*
                if (avgToc != null) {
                    val toc = avgToc.split(("\n"))
                    for (line in toc) {
                        val matchResults = fileInfoRegex.find(line)
                            ?.groupValues?.takeLast(4)?.toList()
                        if (matchResults != null) {
                            val (fName, a, b, fileString) = matchResults
                            val max = a.first()
                            val min = b.first()
                            val range = max..min
                            toFname[range] = fName
                            val filesMap = filesInfo.getOrPut(range) {
                                mutableMapOf()
                            }
                            val files = fileString.split("""\s+""".toRegex())
                            aniFiles.addAll(files)
                            files.map {
                                val l = fNameRegex.find(it)
                                    ?.groupValues?.takeLast(3)
                                if (l != null) {
                                    val (charFname, char, style) = l
                                    filesMap.getOrPut(char[0]) { mutableListOf() }
                                        .add(charFname)
                                    styles.add(style)
                                }
                            }
                            kanji.addAll(filesMap.keys)
                        }
                    }
                    initResults.value = Pair(true, "$TAG: loaded $avgTocName")
                }
              */
            }
        }
        fun getPathInfo(renderChar: Char): String? {
            var pathInfo: String? = null
            var charsFile = ""
            try {
                val charRange: CharRange = charToFilesInfo.keys.first {
                    renderChar in it
                }
                charsFile = charRangeToFname[charRange]!!
            }
            catch (e: Exception) {
                Log.d (TAG, "Failed to open $charsFile for $renderChar: ${e.message}")
                Toast.makeText(
                    appContext,
                    "Failed to open $charsFile for $renderChar.",
                    Toast.LENGTH_LONG).show()
                pathInfo = ""
            }
            return pathInfo
        }
    }
}

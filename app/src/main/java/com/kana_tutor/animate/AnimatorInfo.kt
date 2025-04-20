package com.kana_tutor.animate

import android.os.FileUtils
import android.util.Log
import android.widget.Toast
import com.kana_tutor.kvgviewer.KvgViewer.Companion.appContext
import com.kana_tutor.kvgviewer.KvgViewer.Companion.externalStorageRoot
import com.kana_tutor.utils.ObservedPair
import com.kana_tutor.utils.getFileText
import java.io.File
import java.nio.charset.Charset

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

        init {
            val avgTocName = "paths/avg.toc.txt"
            val avgToc = getFileText(avgTocName)
            if (avgToc == null) {
                val mess = "Failed to find $avgTocName locally or in assets."
                Toast.makeText(appContext, mess, Toast.LENGTH_LONG).show()
                Log.d(TAG, mess)
                initResults.value = Pair(false, mess)
            }
            val fileInfoRegex =
                """(\S+(.)-(.)\S+)\s*=\s*(.*)$""".toRegex()
            val fNameRegex = """^((.)-*([^.]+)*.avg)$""".toRegex()
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
        }
        fun getPathInfo(renderChar: Char): String? {
            var pathInfo: String?
            var charsFile = ""
            try {
                val charRange: CharRange = charToFilesInfo.keys.first {
                    renderChar in it
                }
                charsFile = charRangeToFname[charRange]!!
                pathInfo = com.kana_tutor.utils.getFileText(charsFile)
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
package com.kana_tutor.animate

import android.util.Log
import android.widget.Toast
import com.kana_tutor.kvgviewer.KvgViewer.Companion.appContext
import com.kana_tutor.kvgviewer.KvgViewer.Companion.externalStorageRoot
import com.kana_tutor.utils.ObservedPair
import java.io.File

private const val TAG = "AnimateInfo"
class AnimateInfo {
    companion object {
        val initResults = ObservedPair(Pair(false, ""))
        // map char range -> { char -> file name}
        val charToFilesInfo = mutableMapOf<CharRange, MutableMap<String, MutableList<String>>>()
        init {
            var avgToc = ""
            val avgTocName = "paths/avg.toc.txt"
            if (File(externalStorageRoot, avgTocName).exists()) {
                avgToc = File(externalStorageRoot, avgTocName).readText()
            }
            else {
                try {
                    val reader = appContext.assets.open(avgTocName).bufferedReader()
                    avgToc = reader.readText()
                    reader.close()
                }
                catch (e:Exception) {
                    val mess = "Failed to find $avgTocName locally or in assets."
                    Toast.makeText(appContext,mess, Toast.LENGTH_LONG).show()
                    Log.d(TAG, mess)
                    initResults.value = Pair(false, mess)
                }
            }
            val fileInfoRegex =
                """(\S+(.)-(.)\S+\s*=\s*(.*))$""".toRegex()
            val fNameRegex = """^((.).*.avg)$""".toRegex()
            if (avgToc.isNotEmpty()) {
                val toc = avgToc.split(("\n"))
                for(line in toc) {
                    var matchResults = fileInfoRegex.find(line)
                        ?.groupValues?.takeLast(3)?.toList()
                    if (matchResults != null) {
                        val (a, b, fileString) = matchResults
                        val max = a.first()
                        val min = b.first()
                        val range = max..min
                        val filesMap = charToFilesInfo.getOrPut(range){ mutableMapOf() }
                        val files = fileString.split("""\s+""".toRegex())
                        files.map {
                            val l = fNameRegex.find(it)
                                ?.groupValues?.takeLast(2)
                            if (l != null) {
                                val (fName, char) = l
                                filesMap.getOrPut(char){ mutableListOf() }
                                    .add(fName)
                            }
                        }
                    }
                }
                initResults.value = Pair(true, "$TAG: loaded $avgTocName")
            }
        }
    }
}
@file:Suppress("unused")

package com.kana_tutor.utils

import android.app.Application
import android.net.Uri
import android.util.Log
import com.kana_tutor.kvgviewer.KvgViewer
import com.kana_tutor.kvgviewer.KvgViewer.Companion.appFilesDir
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.URLDecoder
import java.nio.charset.Charset

private const val TAG = "KFileUtils"

class FileUtilsException(mess: String) : RuntimeException(mess)

// check assets and local memory for a file.
fun getFileText(fileName: String): String? {
    var bytes: ByteArray?
    try {
        if (File(KvgViewer.externalStorageRoot, fileName).exists()) {
            bytes = File(KvgViewer.externalStorageRoot, fileName).readBytes()
        } else if (File(KvgViewer.externalStorageRoot, "$fileName.gz").exists()) {
            bytes = File(KvgViewer.externalStorageRoot, "$fileName.gz").readBytes()
        } else {
            // NOTE! Android takes zipped files in assets and unzips
            // and removes zip/gzip extension.
            val reader = KvgViewer.appContext.assets.open(fileName)
            bytes = reader.readBytes()
            reader.close()
        }
    }
    catch (e: Exception) {
        val reader = KvgViewer.appContext.assets.open("$fileName.gz")
        bytes = reader.readBytes()
        reader.close()
    }
    return bytes?.toString(Charset.defaultCharset())
}
private const val BUFFER_SIZE = 0x1000
fun cp(inStream: InputStream, outStream: OutputStream) : Long {
    var totalBytes = 0L
    val bytes = ByteArray(BUFFER_SIZE)
    var bytesRead: Int
    while(inStream.read(bytes).also{bytesRead = it} > 0) {
        outStream.write(bytes, 0, bytesRead)
        totalBytes += bytesRead
    }
    inStream.close()
    return totalBytes
}
fun String.baseName(): String? {
    val s = this
    val v = """^.*/(.*)""".toRegex().find(s)?.groupValues
    return v?.last()
}
fun File.baseName(): String? =
    this.toString().baseName()
fun Uri.baseName(): String? =
    URLDecoder.decode(this.toString(),"UTF-8")
        .toString().baseName()

// used to copy from external file to internal file.
fun cp (baseDir: File, inStream:InputStream, outFileName: String): Long {
    val (dir, _) = """^(?:(.*)/)*(.*)$""".toRegex()
        .find(outFileName)!!.groupValues.takeLast(2)
    mkdirs(baseDir, dir)
    val outStream = File(baseDir, outFileName).outputStream()
    return cp(inStream, outStream)
}
// used to copy internal file to external file.
fun cp (baseDir: File, inFileName: String, outStream:OutputStream): Long {
    val inFile = File(baseDir, inFileName)
    if (!inFile.exists()) {
        Log.d(TAG,
            "cp infileName to outStream: $inFile doesn't exist.")
        return 0L
    }
    return cp(inFile.inputStream(), outStream)
}

// find files.  Output is relative to searchRoot.  Allow
// include/exclude using RegEx.
fun findFiles(
    baseDir : File,
    searchRoot: String, // directory relative to filesDir
    include: Regex? = null,
    exclude: Regex? = null
): Set<String> {
    var rv = mutableSetOf<String>()
    val root = File(baseDir, searchRoot).absoluteFile
    // sort so top-level files are first.

    fun find(dir: String) {
        val files: Array<File> = File(root, dir)
            .listFiles()
            ?: arrayOf()
        rv.addAll(files
            .filter { it.isFile }
            .map { it.toRelativeString(baseDir) })
        files.filter { it.isDirectory }
            .map { find(it.toRelativeString(baseDir)) }
    }
    find(".")
    if (include != null)
        rv = rv.filter { it.contains(include) }.toMutableSet()
    if (exclude != null)
        rv = rv.filterNot { it.contains(exclude) }.toMutableSet()
    return rv
}
// Android throws in some extra hidden directories I don't really
// need.  Use this or add your own if necessary.
val ignoreTheseAssetDirectories = setOf("images/", "sounds/", "webkit/")
fun Application.findAssetFiles(
    startDir:String = "", // default start dir is the assets root.
    ignore:Set<String> = ignoreTheseAssetDirectories // ignore some directories.
) : List<String> {
    val rv = mutableListOf<String>()
    // local function allows encapsulation of rv.
    fun find(dir:String) {
        // NOTE: list("directory") works but list("directory/")
        // returns empty array even if "directory" exists and is
        // a directory.
        val files = assets.list(dir)!!
        val d = if(dir.isEmpty()) dir else "$dir/"
        files.map {file ->
            try {
                if (!ignore.contains(d)) {
                    assets.open("$d$file").close()
                    rv.add("$d$file")
                } else ""
            }
            catch (e: IOException){
                find("$d$file")
            }
        }
    }
    find(startDir)
    return rv
}

fun mkdirs(
    root: File, dirName: String
): File {
    val dirs = dirName.split("/+".toRegex())
        .filter{it.isNotEmpty()}.toMutableList()
    var next: File = root
    while (dirs.isNotEmpty()) {
        next = File(next, dirs.removeAt(0))
        if (!next.exists() && !next.mkdir())
            throw RuntimeException(
                """$TAG: directory under $root,
                | directory $next doesn't and mkdir FAILED."""
                    .trimMargin("|")
            )
    }
    return next
}
// I'm too damn stubborn.  Wrote this because string -> File
// removes ending "/" and I wanted the same code to make directories
// or filed.  File name + path that end in '/' make directories.
fun String.mkFile(): File {
    val s = this.trim()
    var file = File(appFilesDir, s)
    if (file.exists()) return file
    val (dirPart, namePart) = "^(^.*/)([^/]*)$".toRegex()
        .find(s)!!.groupValues.takeLast(2)
    val dirParts = dirPart.split("/+".toRegex())
        .filter{it.isNotEmpty()}.map{"$it/"}
        .toMutableList()
    var path = ""
    file = appFilesDir
    while (dirParts.isNotEmpty()) {
        path += dirParts.removeAt(0)
        file = File(appFilesDir, path)
        if (!file.exists())
            file.mkdir() ||
                throw RuntimeException(
                    "$TAG: mkFile: mkdir($file) FAILED")
    }
    if(namePart.isNotEmpty()) {
        file = File(file, namePart)
        file.createNewFile()  ||
                throw RuntimeException(
                    "$TAG: mkFile: " +
                    "createNewFile($file) FAILED"
                )
    }
    if (!file.exists())
        throw java.lang.RuntimeException("$TAG: mkFile $this -> $file FAILED!")
    Log.d(TAG, "mkFile Created $file")
    return file
}
class StreamReader(inStream: InputStream) {
    private val fileReader = BufferedReader(
        InputStreamReader(inStream)
    )
    private var line: String = ""
    private var notEof = false
    private var lineNumber = 0
    fun nextLine(): Boolean {
        var l = fileReader.readLine()
        lineNumber++
        while (l != null && l.contains("^\\s*#")) {
            l = fileReader.readLine()
        }
        notEof = l != null
        line = if (notEof) l else ""
        return notEof
    }
}

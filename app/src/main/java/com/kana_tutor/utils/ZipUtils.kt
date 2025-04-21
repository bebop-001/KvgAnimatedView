package com.kana_tutor.utils

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.os.StatFs
import android.util.Log
import android.widget.Toast
import com.kana_tutor.kvgviewer.KvgViewer
import com.kana_tutor.kvgviewer.KvgViewer.Companion.externalStorageRoot
import java.io.*
import java.net.URLDecoder
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream


@Suppress("unused")
private const val TAG = "ZipFileUtils"

private const val BUFFER_SIZE = 0x1000
data class ZipInfo (
    val name:   String,
    val cSize:  Long,
    val size:   Long,
    val index:  Int
)
fun zipToc (zipFile: File): List<ZipInfo> {
    val rv = mutableListOf<ZipInfo>()
    Log.d(TAG, "zipToc start")
    var i = 0
    if (zipFile.exists()) {
        val zipStream = ZipInputStream(FileInputStream(zipFile))
        var zipEntry = zipStream.nextEntry
        while (zipEntry != null) {
            with (zipEntry) {
                rv.add(ZipInfo(name, zipEntry.compressedSize, size, i++))
                zipEntry = zipStream.nextEntry
            }
        }
        zipStream.close()
        Log.d(TAG, "zipToc end: ${rv.size}")
    }
    else {
        Log.d(TAG, "zipToc: $zipFile: No such file.")
    }
    return rv
}
fun getZipEntry (zipFile: File, info: ZipInfo): String? {
    var rv: String? = null
    if (zipFile.exists()) {
        var i = 0
        val zis = ZipInputStream(FileInputStream(zipFile))
        var zipEntry = zis.nextEntry
        while (i < info.index) {zipEntry = zis.nextEntry; i++}
        val buffer = zis.readBytes()
        rv = buffer.toString(Charsets.UTF_8)
        zis.close()
    }
    return rv
}
fun unzipFile(
    inStream: InputStream,
    baseDirName: String,
    extractDirs: Set<String>?
): Long {
    fun mkdirs(
        root: File, dirName: String = ""
    ): File {
        val dirs = dirName.split("/+".toRegex()).toMutableList()
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

    // extract dir name must end with one and only one "/"
    val exDirs = extractDirs?.map {
        it.replace("""/*$""".toRegex(), "/")
    }
    val baseDir = File(KvgViewer.appFilesDir, baseDirName)
    val buffer = ByteArray(BUFFER_SIZE)
    var unzippedBytes = 0L
    try {
        val zipStream = ZipInputStream(BufferedInputStream(inStream))
        var ze: ZipEntry? = zipStream.nextEntry
        if (ze == null) {
            Toast.makeText(KvgViewer.appContext,
                "unzipFile: Input doesn't appear to be a zip file.",
                Toast.LENGTH_LONG).show()
            return 0L
        }
        if (!baseDir.exists() || !baseDir.isDirectory) mkdirs(baseDir)
        @Suppress("RegExpUnnecessaryNonCapturingGroup")
        while (ze != null) {
            val timeStamp = ze.time
            val zippedFileName = ze.name
            val (dirName, fileName) = "^(.*/)*(?:([^/]+))*$".toRegex()
                .find(zippedFileName)!!.groupValues.takeLast(2)
            val unzip = exDirs.isNullOrEmpty() ||
                exDirs.firstOrNull { dir ->
                    "^$dir.*".toRegex().matches(dirName)
                } != null
            if (unzip) {
                mkdirs(baseDir, dirName)
                if (fileName.isNotEmpty()) {
                    val unzipName = "$baseDir/$dirName$fileName"
                    val f = File(unzipName)
                    if (f.exists() && !f.delete())
                        Log.d(TAG, "unzipFile: Failed to remove old $fileName")
                    val zipOut = FileOutputStream(f)
                    var bytesRead: Int
                    var bytesThisFile = 0
                    while (zipStream.read(buffer).also { bytesRead = it } > 0) {
                        zipOut.write(buffer, 0, bytesRead)
                        bytesThisFile += bytesRead
                        unzippedBytes += bytesRead.toLong()
                    }
                    f.setLastModified(timeStamp)
                    zipOut.close()
                }
            }
            zipStream.closeEntry()
            ze = zipStream.nextEntry
        }
        zipStream.close()
    }
    catch (e: java.lang.Exception) {
        throw java.lang.RuntimeException("$TAG: Unzip failed: " +
            "${e.message}:\n${e.stackTrace}")
    }
    Log.d(TAG, "unzipped $unzippedBytes bytes total")
    return unzippedBytes
}

fun Context.createZipFile(
    outputStream: OutputStream,
    baseDirName: String, //
    // Only zip files under these directories.
    zipDirs: Set<String>?
): Long {

    val baseDir = File(filesDir, baseDirName)
    val startFreeMem = StatFs(
        Environment.getExternalStorageDirectory().path
    ).availableBytes

    // Locate files and directories under this root directory.
    fun findFilesToZip(zipDirs: Set<String>?): List<String> {
        // for sorting set/list/array of files so top-level files
        // come first.
        val fileNameComparator = Comparator { lVal: String, rVal: String ->
            if (lVal.contains("/") xor rVal.contains("/")) {
                if (rVal.contains("/")) -1
                else 1
            }
            else {
                val lv1 = "%3d.%s".format(lVal.split("/").size, lVal)
                val rv1 = "%3d.%s".format(rVal.split("/").size, lVal)
                lv1.compareTo(rv1)
            }
        }

        val zDirs = zipDirs?.map {
            it.replace("""/*$""".toRegex(), "/")
        }
        val foundFiles = mutableSetOf<String>()
        fun find(dir: String) {
            val zipIt = zDirs.isNullOrEmpty() ||
                    zDirs.firstOrNull { zDir ->
                        "^$zDir.*".toRegex().matches(dir)
                    } != null
            if (!zipIt)
                return
            val d = File(baseDir, dir)
            if (d.list() == null) {
                Toast.makeText(this,
                    "ZipFiles: Empty directory: $dir",
                    Toast.LENGTH_LONG).show()
                return
            }
            d.list()!!.forEach { fName ->
                val f = File(d, fName)
                if (f.isFile)
                    foundFiles.add("$dir$fName")
                else if (f.isDirectory)
                    find("$dir$fName/")
            }
        }
        zDirs?.map { find(it) } ?: find("")
        return foundFiles.toList().sortedWith(fileNameComparator)
    }

    // add a single file to the zip output.
    val bytes = ByteArray(BUFFER_SIZE)
    fun addToZip(fileName: String, zipOutputStream: ZipOutputStream): Long {
        val fileToZip = File(baseDir, fileName)
        val ze = ZipEntry(fileName)
        ze.time = fileToZip.lastModified()
        zipOutputStream.putNextEntry(ze)

        var fileBytes = 0L
        val inStream = fileToZip.inputStream()
        var bytesRead: Int
        while (inStream.read(bytes).also { bytesRead = it } > 0) {
            fileBytes += bytesRead
            zipOutputStream.write(bytes, 0, bytesRead)
        }
        Log.d(TAG, "addToZip:fileBytes:$fileBytes, last bytesRead:$bytesRead")
        inStream.close()
        return fileBytes
    }

    var currentPath = ""
    var unzippedBytes = 0L
    val zipOutputStream = ZipOutputStream(outputStream)
    var fileName = ""
    var fileCounter = 0
    try {
        for (f in findFilesToZip(zipDirs)) {
            fileName = f; fileCounter++
            val path = """^(.*/).*""".toRegex()
                .find(fileName)?.groupValues?.lastOrNull()
                ?: ""
            if (path != currentPath) {
                zipOutputStream.putNextEntry(ZipEntry(path))
                currentPath = path
            }
            unzippedBytes += addToZip(fileName, zipOutputStream)
        }
        zipOutputStream.close()
    }
    catch(e:Exception) {
        val memFree = "%5.1f".format(
            StatFs(Environment.getExternalStorageDirectory().path)
                .availableBytes /(1024 * 1024).toFloat()
        )
        val mess = """Exception encountered at file $fileName
        | error: ${e.localizedMessage}
        | fileCounter = $fileCounter, unzipped bytes = $unzippedBytes
        | Free memory: $memFree
        | """.trimMargin()
        errorWarningDialog("Zip Failed",
            mess, showDone = true)
        Log.e(TAG, mess, e)
        unzippedBytes = -1L
    }
    return unzippedBytes
}
// first is requestor.  Second if not empty is new file.
val getZipRequest = ObservedPair(Pair("", ""))
fun Activity.getZipFromRemote(uri: Uri) {
    val inputStream: InputStream = contentResolver.openInputStream(uri)!!
    val fileBaseName = uriToFileName(uri).baseName()
    val outFile = File(externalStorageRoot, fileBaseName!!)
    fun doCp() {
        val outStream = FileOutputStream(outFile)
        cp(inputStream, outStream)
        val mess = "$outFile: ${"%,d".format(outFile.length())} bytes"

        Toast.makeText(this, mess, Toast.LENGTH_LONG).show()
        Log.d(TAG, mess)
        // Notify zip fetch complete.
        getZipRequest.value = Pair(
            getZipRequest.value_ro.first, outFile.toString())
    }
    if (!outFile.exists()) doCp()
    else yesNo("Overwrite $fileBaseName") {y ->
        if (y) doCp()
        else
            Toast.makeText(
            this, "aborting...", Toast.LENGTH_LONG
            ).show()
    }
}
private val flashDirs = setOf("Examples", "Kanji", "Vocab", "Shared", "Kotowaza")
fun Context.getUnzipFromRemote(uri: Uri) {
    var inputStream: InputStream = contentResolver.openInputStream(uri)!!
    val outFile = File(externalStorageRoot, "Flash.zip")
    val outStream = FileOutputStream(outFile)
    var bytesRead = cp(inputStream, outStream)
    Log.d(TAG, "Storage: cp\n\t$uri -> $outFile\n\t$bytesRead bytes")
    inputStream = FileInputStream(
        File(externalStorageRoot, "Flash.zip"))

    bytesRead = unzipFile(
        inputStream,
        "",
        extractDirs = flashDirs)
    val outBaseName = """^.*/(.*)""".toRegex()
        .find(URLDecoder.decode(uri.toString(),"UTF-8"))!!
        .groups.last()
    val mess = "Transfer to $outBaseName " +
            if (bytesRead > 0)
                "Succeeded. %,d bytes transferred".format(bytesRead)
            else "FAILED"
    Toast.makeText(this, mess, Toast.LENGTH_LONG).show()
}

fun Context.zipToRemote(uri: Uri) {
    val zipBaseDirName = ""
    val bytesRead = createZipFile(
        contentResolver.openOutputStream(uri)!!,
        zipBaseDirName,
        flashDirs
    )
    val outBaseName = """^.*/(.*)""".toRegex()
        .find(URLDecoder.decode(uri.toString(),"UTF-8"))!!
        .groups.last()
    val mess = "Transfer to $outBaseName " +
            if (bytesRead > 0)
                "Succeeded. %,d bytes transferred".format(bytesRead)
            else "FAILED"
    Toast.makeText(this, mess, Toast.LENGTH_LONG).show()
}

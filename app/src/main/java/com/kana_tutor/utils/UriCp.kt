package com.kana_tutor.get_put_zip_demo.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import java.net.URLDecoder

private const val TAG = "UriCp"

var uriCpIoError = ""

const val NULL_URI_IN = -1
const val NULL_URI_OUT = NULL_URI_IN - 1
const val OPEN_IN_FAILED = NULL_URI_OUT - 1
const val OPEN_OUT_FAILED = OPEN_IN_FAILED - 1
const val IO_FAILED = OPEN_OUT_FAILED - 1

val cpErrorMap = mutableMapOf (
    NULL_URI_IN to "uriIn was null",
    NULL_URI_OUT to "uriOut was null",
    OPEN_IN_FAILED to "Open input stream FAILED",
    OPEN_OUT_FAILED to "Open output stream FAILED",
    IO_FAILED to "I/O Failed. see uriCpIoError"
)
private const val BUFFER_SIZE = 0x1000
fun Context.uriCp (uriIn: Uri?, uriOut: Uri?): Int {
    uriCpIoError = ""
    if (uriIn == null) return NULL_URI_IN
    if (uriOut == null) return NULL_URI_OUT
    val inStream = contentResolver.openInputStream(uriIn) ?:
      return OPEN_IN_FAILED
    val outStream = contentResolver.openOutputStream(uriOut) ?:
      return OPEN_OUT_FAILED
    val buffer = ByteArray(BUFFER_SIZE)
    var bytesIn = 0
    var bytesOut = -1
    try {
        bytesOut = inStream.read(buffer)
        while (bytesOut > 0) {
            outStream.write(buffer, 0, bytesOut)
            bytesIn += bytesOut
            bytesOut = inStream.read(buffer)
        }
        inStream.close()
        outStream.close()
    }
    catch (e:java.lang.Exception) {
        uriCpIoError = "I/O Error: " +
                "bytesIn = $bytesIn, bytesOut = $bytesOut," +
                " ${e.message}:${e.stackTrace}"
        Log.d(TAG, uriCpIoError)
        return IO_FAILED
    }
    return bytesIn
}

// uri from storage framework has os_info:fileInfo.  This gives
// us thr file name part.  UrlDecode deals with conversion
// from network character encoding (if any) to utf8.
fun uriToFileName(uri: Uri) : String {
    val fName:String
    try {
        val uriString = URLDecoder.decode(uri.toString(), "UTF-8")
        fName = uriString.split(":").last()
    }
    catch (e:Exception) {
        return ""
    }
    return fName
}

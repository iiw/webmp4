package ac.summer.webmp4.data

import android.content.Context
import android.net.Uri

class FileData(context: Context, source: Uri) {
    val filename: String
    val fullPath: String
    val filenameWithoutExtension: String
    var durationMillis: Long = -1
    var resultFilePath: String = ""
    var lastProgress: Int = 0

    init {
        val fileUtils = FileUtils(context)
        filename = fileUtils.resolveFilename(source)
         filenameWithoutExtension = filename.split(".").getOrNull(0) ?: "converted"
        fullPath = fileUtils.getFullPath(source)

    }
}
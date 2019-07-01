package ac.summer.webmp4.data

import android.content.Context
import android.net.Uri

class FileData(context: Context, val source: Uri) {
    val filename: String
    val filenameWithoutExtension: String
    val fullPath: String
    lateinit var resultFilePath: String

    init {
        val fileUtils = FileUtils(context)
        filename = fileUtils.resolveFilename(source)
        filenameWithoutExtension = filename.split(".").getOrNull(0) ?: "converted"
        fullPath = fileUtils.getFullPath(source)
    }
}
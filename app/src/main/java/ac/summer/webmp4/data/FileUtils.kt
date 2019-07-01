package ac.summer.webmp4.data

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.OpenableColumns


class FileUtils(private val context: Context) {
    fun resolveFilename(source: Uri): String {
        val returnCursor = context.contentResolver.query(source, null, null, null, null)!!
        val nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        returnCursor.moveToFirst()
        val name = returnCursor.getString(nameIndex)
        returnCursor.close()
        return name
    }

    /**
     * WebM only
     */
    fun getFullPath(source: Uri): String {
        val documentId = DocumentsContract.getDocumentId(source)
        val path = documentId.split(":").getOrNull(1)
        return path ?: ""
    }

}
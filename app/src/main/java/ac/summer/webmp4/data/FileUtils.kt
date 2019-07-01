package ac.summer.webmp4.data

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns


class FileUtils(private val context: Context) {
    fun resolveFilename(source: Uri?): String? {
        if (source == null) return null
        val returnCursor = context.contentResolver.query(source, null, null, null, null)!!
        val nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        returnCursor.moveToFirst()
        val name = returnCursor.getString(nameIndex)
        returnCursor.close()
        return name
    }
}
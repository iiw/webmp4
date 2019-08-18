package ac.summer.webmp4.util

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.InputStream


class CloneUtil {
    fun cloneAndGetPath(context: Context, uri: Uri): String {
        context.cacheDir.createNewFile()
        val tempFile = File.createTempFile("video", ".webm")
        context.contentResolver.openInputStream(uri)?.copyTo(tempFile)
        return tempFile.path
    }

    private fun InputStream.copyTo(file: File) {
        use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }
    }
}


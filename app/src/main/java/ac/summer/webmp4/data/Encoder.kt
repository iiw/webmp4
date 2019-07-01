package ac.summer.webmp4.data

import ac.summer.webmp4.ui.Stage
import android.content.Context
import android.net.Uri

class Encoder(private val context: Context, private val onError: (Stage) -> Unit) {
    fun startEncoding(data: Uri?) {
        if (data == null) {
            onError(Stage.SELECT_FILE)
            return
        }
        val inputStream = context.contentResolver.openInputStream(data)
    }
}
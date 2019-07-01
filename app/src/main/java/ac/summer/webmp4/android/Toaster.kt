package ac.summer.webmp4.android

import android.content.Context
import android.widget.Toast

class Toaster(private val context: Context) {
    fun success(text: String?) {
        if (text == null) return
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
    }

    fun error(text: String?) {
        if (text == null) return
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
    }
}
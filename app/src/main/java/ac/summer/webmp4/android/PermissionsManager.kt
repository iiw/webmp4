package ac.summer.webmp4.android

import ac.summer.webmp4.MainActivity
import android.Manifest
import androidx.core.app.ActivityCompat

class PermissionsManager(private val activity: MainActivity) {
    fun checkPermissions() {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ),
            0
        )
    }
}
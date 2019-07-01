package ac.summer.webmp4.android

import ac.summer.webmp4.MainActivity
import android.Manifest
import android.os.Build
import android.support.v4.app.ActivityCompat

class PermissionsManager(private val activity: MainActivity) {
    fun checkReadExternalPermissions() {
        if (Build.VERSION.SDK_INT >= 16) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                0
            )
        }
    }

    fun checkWriteExternalPermissions() {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            0
        )
    }
}
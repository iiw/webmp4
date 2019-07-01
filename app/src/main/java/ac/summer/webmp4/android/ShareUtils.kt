package ac.summer.webmp4.android

import ac.summer.webmp4.MainActivity
import ac.summer.webmp4.R
import ac.summer.webmp4.data.FileData
import android.content.Context
import android.net.Uri
import android.os.Build
import android.support.v4.app.ShareCompat
import android.support.v4.content.FileProvider
import java.io.File


class ShareUtils(private val context: Context, private val activity: MainActivity) {
    private val shareString by lazy {
        context.resources.getString(R.string.share)
    }

    fun share(file: FileData?) {
        val resultPath = file?.resultFilePath ?: return
        val videoFile = File(resultPath)
        val videoURI = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            FileProvider.getUriForFile(context, context.packageName, videoFile)
        else
            Uri.fromFile(videoFile)
        ShareCompat.IntentBuilder.from(activity)
            .setStream(videoURI)
            .setType("video/mp4")
            .setChooserTitle(shareString)
            .startChooser()
    }
}
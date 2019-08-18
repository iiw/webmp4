package ac.summer.webmp4.data

import ac.summer.webmp4.ui.Stage
import android.os.Build
import android.os.Environment
import com.arthenica.mobileffmpeg.FFmpeg
import io.reactivex.disposables.Disposable
import java.io.File

class Encoder(private val onError: (Stage) -> Disposable) {
    fun startEncoding(data: FileData?): Boolean {
        if (data == null) {
            onError(Stage.SELECT_FILE)
            return false
        }
        executionThread = Thread.currentThread()
        val downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        data.resultFilePath = "$downloads/${data.filenameWithoutExtension}.mp4"
        val resultFile = File(data.resultFilePath)
        if (resultFile.exists()) {
            resultFile.delete()
        }
        val information = FFmpeg.getMediaInformation(data.fullPath)
        data.durationMillis = information?.duration ?: 60000
        val status = FFmpeg.execute(arrayOf("-i", data.fullPath, "-c:v", "mpeg4", data.resultFilePath))
        if (status != FFmpeg.RETURN_CODE_SUCCESS && status != FFmpeg.RETURN_CODE_CANCEL) {
            onError(Stage.CONVERT)
        }
        executionThread = null
        return status == FFmpeg.RETURN_CODE_SUCCESS
    }



    fun stop() {
        FFmpeg.cancel()
    }

    fun ongoing(): Boolean {
        return executionThread != null
    }

    companion object {
        private var executionThread: Thread? = null
    }
}
package ac.summer.webmp4.data

import ac.summer.webmp4.ui.Stage
import android.os.Environment
import android.util.Log
import com.arthenica.mobileffmpeg.FFmpeg
import io.reactivex.disposables.Disposable
import java.io.File

class Encoder(private val onError: (Stage) -> Disposable) {
    private var resultPath: String? = null
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
        resultPath = data.resultFilePath
        val status = FFmpeg.execute(arrayOf("-i", data.fullPath, "-c:v", "mpeg4", data.resultFilePath))
        if (status != FFmpeg.RETURN_CODE_SUCCESS && status != FFmpeg.RETURN_CODE_CANCEL) {
            onError(Stage.CONVERT)
        }
        resultPath = null
        executionThread = null
        return status == FFmpeg.RETURN_CODE_SUCCESS
    }


    fun stop() {
        FFmpeg.cancel()
        val resultPath = this.resultPath
        if (resultPath != null) {
            try {
                File(resultPath).delete()
            } catch (t: Throwable) {
                Log.e("Encoder.stop deleteResultFile error", "Result file is not deleted after stop", t)
            }
        }
    }

    fun ongoing(): Boolean {
        return executionThread != null
    }

    companion object {
        private var executionThread: Thread? = null
    }
}
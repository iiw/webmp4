package ac.summer.webmp4

import android.app.Application
import android.util.Log
import com.arthenica.mobileffmpeg.Config
import io.reactivex.subjects.BehaviorSubject
import kotlin.math.roundToInt

class WebMP4Application : Application() {
    override fun onCreate() {
        super.onCreate()
        Config.enableStatisticsCallback { stats ->
            val duration = MainActivity.file?.durationMillis?.toDouble()
            val time = stats.time.toDouble()
            if (duration == null || duration < 0 || time < 1) {
                return@enableStatisticsCallback
            }
            val progressPercents = (time / duration * 100.0).roundToInt()
            Log.d("PROGRESS", progressPercents.toString())
            progress.onNext(progressPercents)
        }
    }

    companion object {
        val progress = BehaviorSubject.createDefault(0)
    }
}
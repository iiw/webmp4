package ac.summer.webmp4.ui

import java.text.SimpleDateFormat
import java.util.*
import java.util.logging.Logger
import kotlin.math.roundToInt

object ProgressParser {
    private val durationRegex by lazy { Regex("Duration: ..:..:..") }
    private val lastFrameTimeRegex by lazy { Regex("time=..:..:..") }
    private val durationFormat by lazy {
        SimpleDateFormat("HH:mm:ss")
    }
    private val logger by lazy {
        Logger.getLogger("Main")
    }

    fun getProgress(rawOutput: String): Int? {
        return parse(rawOutput)
    }

    private fun parse(rawOutput: String): Int? {
        val durationSeconds = getSecondsTotal(getDuration(rawOutput)) ?: return null
        val lastFrameSeconds = getSecondsTotal(getLastFrameTime(rawOutput)) ?: return null
        return calculateProgress(durationSeconds, lastFrameSeconds)
    }

    private fun calculateProgress(duration: Int, last: Int): Int {
        return (last.toDouble() / (duration.toDouble() - 1.0) * 100.0).roundToInt()
    }

    private fun getDuration(rawOutput: String): Date? {
        val duration = durationRegex.find(rawOutput)?.value ?: return null
        val durationString = duration.slice(IntRange(duration.length - 8, duration.length - 1));
        return durationFormat.parse(durationString)
    }

    private fun getLastFrameTime(rawOutput: String): Date? {
        val times = lastFrameTimeRegex.findAll(rawOutput)
        if (times.count() == 0) {
            return null
        }
        val time = times.last().value
        val timeString = time.slice(IntRange(time.length - 8, time.length - 1))
        return durationFormat.parse(timeString)
    }

    private fun getSecondsTotal(time: Date?): Int? {
        if (time == null) return null
        val calendar = Calendar.getInstance()
        calendar.time = time
        val hour = calendar.get(Calendar.HOUR)
        val minute = calendar.get(Calendar.MINUTE)
        val seconds = calendar.get(Calendar.SECOND)
        return hour * 60 * 60 + minute * 60 + seconds

    }
}
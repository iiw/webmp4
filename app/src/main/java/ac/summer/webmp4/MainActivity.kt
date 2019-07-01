package ac.summer.webmp4

import ac.summer.webmp4.android.PermissionsManager
import ac.summer.webmp4.android.ShareUtils
import ac.summer.webmp4.android.Toaster
import ac.summer.webmp4.data.Encoder
import ac.summer.webmp4.data.FileData
import ac.summer.webmp4.ui.ProgressParser
import ac.summer.webmp4.ui.Stage
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.arthenica.mobileffmpeg.FFmpeg
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.logging.Logger
import kotlin.concurrent.thread


class MainActivity : AppCompatActivity() {
    private val logger by lazy {
        Logger.getLogger("Main")
    }
    private val encoder by lazy { Encoder(applicationContext, ::onError) }
    private val permissionsManager by lazy { PermissionsManager(this) }
    private val toaster by lazy { Toaster(applicationContext) }
    private val shareUtils by lazy { ShareUtils(applicationContext, this) }

    private val selectWebmString by lazy {
        resources.getString(R.string.select_webm)
    }
    private val convertString by lazy {
        resources.getString(R.string.convert)
    }
    private val fileNotSelectedString by lazy {
        resources.getString(R.string.file_not_selected)
    }
    private val shareString by lazy {
        resources.getString(R.string.share)
    }
    private val convertFileErrorString by lazy {
        resources.getString(R.string.convert_file_error)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setStage(stage)
    }

    fun onAction(view: View) {
        logger.info("Button click.")
        when (stage) {
            Stage.SELECT_FILE -> {
                permissionsManager.checkReadExternalPermissions()
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.type = "video/webm"
                startActivityForResult(intent, OPEN_WEBM)
            }
            Stage.SELECT_FILE_SUCCESS -> {
                permissionsManager.checkWriteExternalPermissions()
                setStage(Stage.CONVERT)
                var success: Boolean? = null
                GlobalScope.launch {
                    while (success == null) {
                        val progress = ProgressParser.getProgress(FFmpeg.getLastCommandOutput())
                        if (progress != null) {
                            mainThread().scheduleDirect {
                                progress_bar.progress = progress
                            }
                        }
                        delay(50)
                    }
                }
                thread {
                    Thread.sleep(500)
                    success = encoder.startEncoding(file)
                    if (success == true) {
                        mainThread().scheduleDirect {
                            setStage(Stage.CONVERT_SUCCESS)
                        }
                    }
                }
            }
            Stage.CONVERT_SUCCESS -> {
                shareUtils.share(file)
            }
            else -> {
            }
        }
    }

    fun resetStage(view: View) {
        if (encoder.ongoing()) {
            encoder.stop()
        }
        setStage(Stage.SELECT_FILE)
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, selected: Intent?) {
        super.onActivityResult(requestCode, resultCode, selected)
        if (requestCode == OPEN_WEBM && resultCode == Activity.RESULT_OK) {
            val data = selected?.data
            if (data == null) {
                onError(Stage.SELECT_FILE)
                return
            }
            file = FileData(applicationContext, data)
            setStage(Stage.SELECT_FILE_SUCCESS)
            logger.info("Data selected.")
        }
    }

    private fun setStage(stage: Stage) {
        MainActivity.stage = stage
        logger.info("STAGE SET: $stage")
        when (stage) {
            Stage.SELECT_FILE -> {
                file = null
                reset.visibility = View.GONE
                filename.text = fileNotSelectedString
                action_button.text = selectWebmString
                action_button.isEnabled = true
                progress_bar.visibility = View.INVISIBLE
                progress_bar.progress = 0
            }
            Stage.SELECT_FILE_SUCCESS -> {
                action_button.text = convertString
                action_button.isEnabled = true
                progress_bar.visibility = View.INVISIBLE
                progress_bar.progress = 0
                filename.text = getFileName()
                reset.visibility = View.VISIBLE
            }
            Stage.CONVERT -> {
                action_button.isEnabled = false
                progress_bar.visibility = View.VISIBLE
            }
            Stage.CONVERT_SUCCESS -> {
                action_button.isEnabled = true
                progress_bar.visibility = View.INVISIBLE
                action_button.text = shareString
                progress_bar.progress = 0
            }
            else -> {
            }
        }
    }

    private fun onError(stage: Stage) = mainThread().scheduleDirect {
        val errorMsg = when (stage) {
            Stage.SELECT_FILE -> {
                setStage(Stage.SELECT_FILE)
                fileNotSelectedString
            }
            Stage.CONVERT -> {
                setStage(Stage.SELECT_FILE_SUCCESS)
                convertFileErrorString
            }
            else -> null
        }
        if (errorMsg != null) {
            toaster.error(errorMsg)
            logger.info("File not selected.")
        }
    }

    private fun getFileName(): String {
        return file?.filename ?: fileNotSelectedString
    }

    companion object {
        private const val OPEN_WEBM = 2
        private var stage = Stage.SELECT_FILE
        private var file: FileData? = null
    }
}

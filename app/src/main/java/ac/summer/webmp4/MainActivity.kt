package ac.summer.webmp4

import ac.summer.webmp4.data.Encoder
import ac.summer.webmp4.data.FileUtils
import ac.summer.webmp4.ui.Stage
import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import java.util.logging.Logger


class MainActivity : AppCompatActivity() {
    private val OPEN_WEBM = 2
    private val logger by lazy {
        Logger.getLogger("Main")
    }
    private val encoder by lazy { Encoder(applicationContext, ::onError) }
    private val fileUtils by lazy { FileUtils(applicationContext) }
    private val selectWebmString by lazy {
        resources.getString(R.string.select_webm)
    }
    private val convertString by lazy {
        resources.getString(R.string.convert)
    }
    private val fileNotSelectedString by lazy {
        resources.getString(R.string.file_not_selected)
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
                if (Build.VERSION.SDK_INT >= 16) {
                    ActivityCompat.requestPermissions(
                        this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                        0
                    )
                }
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.type = "video/webm"
                startActivityForResult(intent, OPEN_WEBM)
            }
            Stage.SELECT_FILE_SUCCESS -> {
                encoder.startEncoding(source)
            }
            else -> {
            }
        }
    }

    fun resetStage(view: View) {
        setStage(Stage.SELECT_FILE)
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, selected: Intent?) {
        super.onActivityResult(requestCode, resultCode, selected)
        if (requestCode == OPEN_WEBM && resultCode == Activity.RESULT_OK) {
            source = selected?.data ?: return onError(Stage.SELECT_FILE)
            setStage(Stage.SELECT_FILE_SUCCESS)
            logger.info("Data selected.")
        }
    }

    private fun setStage(stage: Stage) {
        MainActivity.stage = stage
        when (stage) {
            Stage.SELECT_FILE -> {
                source = null
                reset.visibility = View.GONE
                filename.text = fileNotSelectedString
                action_button.text = selectWebmString
                progress_bar.visibility = View.GONE
            }
            Stage.SELECT_FILE_SUCCESS -> {
                action_button.text = convertString
                filename.text = getFileName()
                reset.visibility = View.VISIBLE
            }
            Stage.CONVERT -> {
                progress_bar.progress = 0
                progress_bar.visibility = View.VISIBLE
            }
            else -> {
            }
        }
    }

    private fun onError(stage: Stage) {
        val errorMsg = when (stage) {
            Stage.SELECT_FILE -> fileNotSelectedString
            else -> null
        }
        if (errorMsg != null) {
            Toast.makeText(applicationContext, errorMsg, Toast.LENGTH_SHORT)
            logger.info("File not selected.")
        }
    }

    private fun getFileName(): String {
        return fileUtils.resolveFilename(source) ?: fileNotSelectedString
    }

    companion object {
        private var stage = Stage.SELECT_FILE
        private var source: Uri? = null
    }
}

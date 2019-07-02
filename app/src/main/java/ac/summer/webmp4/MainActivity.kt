package ac.summer.webmp4

import ac.summer.webmp4.android.PermissionsManager
import ac.summer.webmp4.android.ShareUtils
import ac.summer.webmp4.android.Toaster
import ac.summer.webmp4.data.Encoder
import ac.summer.webmp4.data.FileData
import ac.summer.webmp4.ui.Stage
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.activity_main.*
import java.util.logging.Logger
import kotlin.concurrent.thread


class MainActivity : AppCompatActivity() {
    private val logger by lazy {
        Logger.getLogger("Main")
    }
    private val encoder by lazy { Encoder(::onError) }
    private val permissionsManager by lazy { PermissionsManager(this) }
    private val toaster by lazy { Toaster(applicationContext) }
    private val shareUtils by lazy { ShareUtils(applicationContext, this) }
    private var disposable = CompositeDisposable()

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
        disposable.addAll(
            observableOperationStatus
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(mainThread())
                .subscribe { success ->
                    if (success) {
                        setStage(Stage.CONVERT_SUCCESS)
                    }
                },
            WebMP4Application.progress
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(mainThread())
                .subscribe {
                    progress_bar.progress = it
                }
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable.dispose()
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
                thread {
                    observableOperationStatus.onNext(encoder.startEncoding(file))
                }
            }
            Stage.CONVERT -> {

            }
            Stage.CONVERT_SUCCESS -> {
                shareUtils.share(file)
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

        filename.text = getFileName()
        action_button.isEnabled = true
        progress_bar.progress = 0
        progress_bar.visibility = View.INVISIBLE
        reset.visibility = View.VISIBLE
        when (stage) {
            Stage.SELECT_FILE -> {
                file = null
                reset.visibility = View.GONE
                filename.text = fileNotSelectedString
                action_button.text = selectWebmString
            }
            Stage.SELECT_FILE_SUCCESS -> {
                action_button.text = convertString
            }
            Stage.CONVERT -> {
                action_button.isEnabled = false
                progress_bar.visibility = View.VISIBLE
                progress_bar.progress = file?.lastProgress ?: 0
            }
            Stage.CONVERT_SUCCESS -> {
                action_button.text = shareString
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
        var file: FileData? = null
        private var observableOperationStatus = PublishSubject.create<Boolean>()
    }
}

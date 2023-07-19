package com.example.audiorecord

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.audiorecord.databinding.ActivityMainBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date

class MainActivity : AppCompatActivity(), Time.OnTimeTickListener {
    val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission is granted. Continue the action or workflow in your
            // app.
        } else {
            // Explain to the user that the feature is unavailable because the
            // feature requires a permission that the user has denied. At the
            // same time, respect the user's decision. Don't link to system
            // settings in an effort to convince the user to change their
            // decision.
        }
    }

    companion object {
        private const val REQUEST_WRITE_STORAGE = 1
        private const val SAMPLE_RATE = 44100
        private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_STEREO
        private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
    }


    lateinit var record: MediaRecorder

    lateinit var mBinding: ActivityMainBinding
    private var dirpath = ""
    private var filename = ""

    private var isRecording = false
    private var isPause = false

    private lateinit var timer: Time
    private lateinit var btsBehavior: BottomSheetBehavior<LinearLayout>
    private lateinit var outputFile: File

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        btsBehavior = BottomSheetBehavior.from(mBinding.btsSave.btsSaveRoot)
        btsBehavior.peekHeight = 0
        btsBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        timer = Time(this)
        mBinding.play.setOnClickListener {
            when {
                ContextCompat.checkSelfPermission(
                    this, Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED -> {

                    when {
                        isPause -> resumeRec()
                        isRecording -> pauseRecord()
                        else -> StartRecorder()
                    }

                    mBinding.play.setImageResource(if (isRecording) R.drawable.baseline_pause_24 else R.drawable.baseline_play_circle_24)
                }

                shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO) -> {
                    // In an educational UI, explain to the user why your app requires this
                    // permission for a specific feature to behave as expected, and what
                    // features are disabled if it's declined. In this UI, include a
                    // "cancel" or "no thanks" button that lets the user continue
                    // using your app without granting the permission.

                }

                else -> {
                    requestPermissionLauncher.launch(
                        Manifest.permission.RECORD_AUDIO
                    )
                }
            }
        }

        mBinding.cancel.setOnClickListener {

            btsBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            mBinding.bgBtsDialog.visibility = View.VISIBLE
            mBinding.btsSave.apply {
                fileNameInput.setText(filename)
                btnCancel.setOnClickListener {
                    dismiss()
                }
                btnSave.setOnClickListener {
                    stopRec()
                    hiddenKeyboard(it)
                    save()
//                    File("$dirpath$filename.mp3")
                }
            }
        }

    }

    private fun save() {
        val newName = mBinding.btsSave.fileNameInput.text.toString()
        if (newName != filename) {
            pauseRecord()
            stopRec()
        }
    }

    private fun hiddenKeyboard(view: View) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun dismiss() {
        btsBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        mBinding.bgBtsDialog.visibility = View.GONE
    }

    private fun resumeRec() {
        isRecording = true
        record.resume()
        isPause = false
        timer.start()
    }

    private fun pauseRecord() {
        isRecording = false
        timer.pause()
        record.pause()
        isPause = true
    }

    private fun StartRecorder() {
        dirpath =  getExternalFilesDir(Environment.DIRECTORY_MUSIC)?.absolutePath
            ?: filesDir.absolutePath
        val simpleDate = SimpleDateFormat("yyyy.MM.DD_hh.mm.ss")
        val date = simpleDate.format(Date())
//        filename = System.currentTimeMillis().toString() + ".mp3"
//        outputFile = File(dirpath, filename)
//        filename = "audio_rec_$date"
        filename = "my_record.mp3"
        outputFile = File(dirpath, filename)
        record = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                setOutputFile(outputFile)
            }

            try {
                prepare()
            } catch (e: IOException) {

            }
            isPause = false
            isRecording = true
            start()
            timer.start()

        }

    }

    private fun stopRec() {
        timer.stop()
    }

    override fun onTimeTick(duration: String) {
        mBinding.tvTime.setText(duration)

        mBinding.waveFormView.addAmplitude(record.maxAmplitude.toFloat())
    }

    private fun stopRecording() {
        try {
            // Dừng và giải phóng tài nguyên MediaRecorder
            record.stop()
            record.reset()
            record.release()

            isPause = false
            isRecording = true
            Toast.makeText(
                this,
                "Recording stopped. File saved: ${outputFile.absolutePath}",
                Toast.LENGTH_SHORT
            ).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to stop recording.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onStop() {
        super.onStop()
        stopRecording()
    }
}
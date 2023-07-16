package com.example.audiorecord

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.audiorecord.databinding.ActivityMainBinding
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

    lateinit var record: MediaRecorder

    lateinit var mBinding: ActivityMainBinding
    private var dirpath = ""
    private var filename = ""

    private var isRecording = false
    private var isPause = false

    private lateinit var timer: Time

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        timer = Time(this)
        mBinding.play.setOnClickListener {
            when {
                ContextCompat.checkSelfPermission(
                    this, Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED -> {

              when{
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
        dirpath = "${externalCacheDir?.absolutePath}"
        val simpleDate = SimpleDateFormat("yyyy.MM.DD_hh.mm.ss")
        val date = simpleDate.format(Date())
        filename = "audio_rec_$date"
        record = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile("$dirpath$filename.mp3")

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
}
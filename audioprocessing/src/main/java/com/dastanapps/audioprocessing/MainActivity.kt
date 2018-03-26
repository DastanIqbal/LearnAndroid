package com.dastanapps.audioprocessing

import android.graphics.Color
import android.media.*
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.util.DisplayMetrics
import android.view.MotionEvent
import com.dastanapps.audioprocessing.Effects.rawToWave
import com.jakewharton.rxbinding2.widget.RxSeekBar
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class MainActivity : AppCompatActivity() {
    private val fileName = "44k16bitMono"
    private var bufferElements2Rec = 1024 // want to play 2048 (2K) since 2 bytes we use only 1024
    private var bytesPerElement = 2 // 2 bytes in 16bit format
    private val SAMPLERATE = 44100
    private val CHANNEL_MASK = AudioFormat.CHANNEL_IN_MONO
    private val ENCODING = AudioFormat.ENCODING_PCM_16BIT
    private val BUFFER_SIZE = 2 * AudioRecord.getMinBufferSize(SAMPLERATE, CHANNEL_MASK, ENCODING)

    private var mAudioRecorder: AudioRecord? = null
    private var isRecording: Boolean = false
    private var recordingThread: Thread? = null
    private var seekMoving = false
    private val mHandler = Handler()


    private val dp8: Int = (DisplayMetrics().density * 8).toInt()
    private val dp16: Int = (DisplayMetrics().density * 16).toInt()
    private var mMediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        imv_play.isEnabled = false
        imv_record.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    imv_record.setBackgroundColor(Color.WHITE)
                    startRecording()
                }
                MotionEvent.ACTION_UP -> {
                    stopRecording()
                    imv_record.setBackgroundColor(Color.BLACK)
                }
            }
            true
        }

        RxSeekBar.userChanges(seekBar)
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    seekMoving = true
                }
        imv_play.setOnClickListener {
            setUpMediaPlayer()
        }
    }

    fun startRecording() {
        mAudioRecorder = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLERATE,
                CHANNEL_MASK,
                ENCODING,
                BUFFER_SIZE)
        mAudioRecorder?.startRecording()
        isRecording = true
        recordingThread = Thread(Runnable { writeAudioDataToFile() }, "AudioRecorder Thread")
        recordingThread?.start()
    }

    private fun stopRecording() {
        // stops the recording activity
        if (null != mAudioRecorder) {
            isRecording = false
            mAudioRecorder?.stop()
            mAudioRecorder?.release()
            mAudioRecorder = null
            recordingThread = null
        }
    }

    private fun setUpMediaPlayer() {
        mMediaPlayer = MediaPlayer()
        mMediaPlayer?.setAudioStreamType(AudioManager.STREAM_MUSIC)
        mMediaPlayer?.setDataSource("/sdcard/$fileName.wav")
        mMediaPlayer?.prepareAsync()
        mMediaPlayer?.setOnPreparedListener {
            imv_play.isEnabled = true
            seekBar.max = mMediaPlayer?.duration!!
            runOnUiThread(object : Runnable {
                override fun run() {
                    if (mMediaPlayer != null && !seekMoving && mMediaPlayer!!.isPlaying) {
                        val mCurrentPosition = mMediaPlayer?.currentPosition
                        seekBar.progress = mCurrentPosition!!
                    }
                    mHandler.postDelayed(this, 50)
                }
            })

            mMediaPlayer?.start()
        }

        mMediaPlayer?.setOnCompletionListener {
            mMediaPlayer?.reset()
            mMediaPlayer?.release()
            mMediaPlayer = null
            // imv_play.isEnabled = false
        }

        mMediaPlayer?.setOnErrorListener { mp, what, extra ->
            mMediaPlayer?.reset()
            mMediaPlayer?.release()
            mMediaPlayer = null
            imv_play.isEnabled = false
            true
        }
    }

    private fun writeAudioDataToFile() {
        // Write the output audio in byte
        val filePath = "/sdcard/$fileName.pcm"
        val sData = ShortArray(bufferElements2Rec)
        try {
            val os = FileOutputStream(filePath)
            while (isRecording) {

                // gets the voice output from microphone to byte format
                mAudioRecorder?.read(sData, 0, bufferElements2Rec)

                // writes the data to file from buffer stores the voice buffer
                val bData = TypeConversionNative.shortToByte(sData)
                os.write(bData, 0, bufferElements2Rec * bytesPerElement)
            }
            os.close()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            val f1 = File(filePath)
            val f2 = File("/sdcard/$fileName.wav")
            try {
                rawToWave(f1, f2, SAMPLERATE)
                setUpMediaPlayer()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}

package com.dastanapps.audioprocessing

import android.graphics.Color
import android.media.*
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.util.DisplayMetrics
import android.view.MotionEvent
import com.jakewharton.rxbinding2.widget.RxSeekBar
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_main.*
import java.io.*
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.experimental.and


class MainActivity : AppCompatActivity() {
    private val fileName = "44k16bitMono"
    internal var bufferElements2Rec = 1024 // want to play 2048 (2K) since 2 bytes we use only 1024
    internal var bytesPerElement = 2 // 2 bytes in 16bit format
    private val RECORDER_SAMPLERATE = 44100
    private var waveSampling = 37000


    private var recorder: AudioRecord? = null
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
        recorder = AudioRecord(MediaRecorder.AudioSource.CAMCORDER, RECORDER_SAMPLERATE,
                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT,
                bytesPerElement * bufferElements2Rec)
        recorder?.startRecording()
        isRecording = true
        recordingThread = Thread(Runnable { writeAudioDataToFile() }, "AudioRecorder Thread")
        recordingThread?.start()
    }

    private fun stopRecording() {
        // stops the recording activity
        if (null != recorder) {
            isRecording = false
            recorder?.stop()
            recorder?.release()
            recorder = null
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
        var os: FileOutputStream? = null
        try {
            os = FileOutputStream(filePath)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }

        while (isRecording) {
            // gets the voice output from microphone to byte format
            recorder?.read(sData, 0, bufferElements2Rec)
            println("Short wirting to file" + sData.toString())
            try {
                // writes the data to file from buffer stores the voice buffer
                val bData = short2byte(sData)
                os!!.write(bData, 0, bufferElements2Rec * bytesPerElement)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        try {
            os!!.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        try {
            os?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            val f1 = File(filePath)
            val f2 = File("/sdcard/$fileName.wav")
            try {
                rawToWave(f1, f2)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    //Conversion of short to byte
    private fun short2byte(sData: ShortArray): ByteArray {
        val shortArrsize = sData.size
        val bytes = ByteArray(shortArrsize * 2)
        for (i in 0 until shortArrsize) {
            bytes[i * 2] = sData[i].toByte() and (0x00FF).toByte()
            bytes[i * 2 + 1] = (sData[i]).toInt().shr(8).toByte()
            sData[i] = 0
        }
        return bytes
    }

    @Throws(IOException::class)
    private fun rawToWave(rawFile: File, waveFile: File) {

        val rawData = ByteArray(rawFile.length().toInt())
        var input: DataInputStream? = null
        try {
            input = DataInputStream(FileInputStream(rawFile))
            input.read(rawData)
        } finally {
            if (input != null) {
                input.close()
            }
        }

        var output: DataOutputStream? = null
        try {
            output = DataOutputStream(FileOutputStream(waveFile))
            // WAVE header
            // see http://ccrma.stanford.edu/courses/422/projects/WaveFormat/
            writeString(output, "RIFF") // chunk id
            writeInt(output, 36 + rawData.size) // chunk size
            writeString(output, "WAVE") // format
            writeString(output, "fmt ") // subchunk 1 id
            writeInt(output, 16) // subchunk 1 size
            writeShort(output, 1.toShort()) // audio format (1 = PCM)
            writeShort(output, 1.toShort()) // number of channels
            writeInt(output, waveSampling) // sample rate
            writeInt(output, RECORDER_SAMPLERATE * 2) // byte rate
            writeShort(output, 2.toShort()) // block align
            writeShort(output, 16.toShort()) // bits per sample
            writeString(output, "data") // subchunk 2 id
            writeInt(output, rawData.size) // subchunk 2 size
            // Audio data (conversion big endian -> little endian)
            val shorts = ShortArray(rawData.size / 2)
            ByteBuffer.wrap(rawData).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shorts)
            val bytes = ByteBuffer.allocate(shorts.size * 2)
            for (s in shorts) {
                bytes.putShort(s)
            }
            output.write(fullyReadFileToBytes(rawFile))
        } finally {
            if (output != null) {
                output.close()
            }
        }
        // Adding echo
        //Clone original Bytes
        val bytesTemp = fullyReadFileToBytes(rawFile)
        val temp = bytesTemp.clone()
        val randomAccessFile = RandomAccessFile(waveFile, "rw")
        //seek to skip 44 bytes
        randomAccessFile.seek(44)
        //Echo
        val N = RECORDER_SAMPLERATE / 8
        for (n in N + 1 until bytesTemp.size) {
            bytesTemp[n] = (temp[n] + .3 * temp[n - N]).toByte()
        }
        randomAccessFile.write(bytesTemp)
        randomAccessFile.close()
        rawFile.delete()

        setUpMediaPlayer()
    }

    @Throws(IOException::class)
    private fun writeInt(output: DataOutputStream, value: Int) {
        output.write(value.shr(0))
        output.write(value.shr(8))
        output.write(value.shr(16))
        output.write(value.shr(24))
    }

    @Throws(IOException::class)
    private fun writeShort(output: DataOutputStream, value: Short) {
        output.write(value.toInt().shr(0))
        output.write(value.toInt().shr(8))
    }

    @Throws(IOException::class)
    private fun writeString(output: DataOutputStream, value: String) {
        for (i in 0 until value.length) {
            output.write(value[i].toInt())
        }
    }

    @Throws(IOException::class)
    internal fun fullyReadFileToBytes(f: File): ByteArray {
        val size = f.length().toInt()
        val bytes = ByteArray(size)
        val tmpBuff = ByteArray(size)
        val fis = FileInputStream(f)
        try {

            var read = fis.read(bytes, 0, size)
            if (read < size) {
                var remain = size - read
                while (remain > 0) {
                    read = fis.read(tmpBuff, 0, remain)
                    System.arraycopy(tmpBuff, 0, bytes, size - remain, read)
                    remain -= read
                }
            }
        } catch (e: IOException) {
            throw e
        } finally {
            fis.close()
        }

        return bytes
    }
}

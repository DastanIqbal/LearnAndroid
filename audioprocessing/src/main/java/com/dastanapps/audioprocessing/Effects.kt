package com.dastanapps.audioprocessing

import java.io.*
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Created by dastaniqbal on 25/03/2018.

 * 25/03/2018 11:58
 */
object Effects {
    private var waveSampling = 37000
    @Throws(IOException::class)
    fun rawToWave(rawFile: File, waveFile: File, sampleRate: Int) {
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
            AudioUtils.writeString(output, "RIFF") // chunk id
            AudioUtils.writeInt(output, 36 + rawData.size) // chunk size
            AudioUtils.writeString(output, "WAVE") // format
            AudioUtils.writeString(output, "fmt ") // subchunk 1 id
            AudioUtils.writeInt(output, 16) // subchunk 1 size
            AudioUtils.writeShort(output, 1.toShort()) // audio format (1 = PCM)
            AudioUtils.writeShort(output, 1.toShort()) // number of channels
            AudioUtils.writeInt(output, waveSampling) // sample rate
            AudioUtils.writeInt(output, sampleRate * 2) // byte rate
            AudioUtils.writeShort(output, 2.toShort()) // block align
            AudioUtils.writeShort(output, 16.toShort()) // bits per sample
            AudioUtils.writeString(output, "data") // subchunk 2 id
            AudioUtils.writeInt(output, rawData.size) // subchunk 2 size
            // Audio data (conversion big endian -> little endian)
            val shorts = ShortArray(rawData.size / 2)
            ByteBuffer.wrap(rawData).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shorts)
            val bytes = ByteBuffer.allocate(shorts.size * 2)
            for (s in shorts) {
                bytes.putShort(s)
            }
            output.write(AudioUtils.fullyReadFileToBytes(rawFile))
        } finally {
            if (output != null) {
                output.close()
            }
        }
        // Adding echo
        //Clone original Bytes
        val bytesTemp = AudioUtils.fullyReadFileToBytes(rawFile)
        val temp = bytesTemp.clone()
        val randomAccessFile = RandomAccessFile(waveFile, "rw")
        //seek to skip 44 bytes
        randomAccessFile.seek(44)
        //Echo
        val N = sampleRate / 8
        for (n in N + 1 until bytesTemp.size) {
            bytesTemp[n] = (temp[n] + .3 * temp[n - N]).toByte()
        }
        randomAccessFile.write(bytesTemp)
        randomAccessFile.close()
        rawFile.delete()
    }
}
package com.dastanapps.audioprocessing

import java.io.DataOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import kotlin.experimental.and

/**
 * Created by dastaniqbal on 25/03/2018.

 * 25/03/2018 11:53
 */
object AudioUtils {
    //Conversion of short to byte
    fun short2byte(sData: ShortArray): ByteArray {
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
    fun writeInt(output: DataOutputStream, value: Int) {
        output.write(value.shr(0))
        output.write(value.shr(8))
        output.write(value.shr(16))
        output.write(value.shr(24))
    }

    @Throws(IOException::class)
    fun writeShort(output: DataOutputStream, value: Short) {
        output.write(value.toInt().shr(0))
        output.write(value.toInt().shr(8))
    }

    @Throws(IOException::class)
    fun writeString(output: DataOutputStream, value: String) {
        for (i in 0 until value.length) {
            output.write(value[i].toInt())
        }
    }

    @Throws(IOException::class)
    fun fullyReadFileToBytes(f: File): ByteArray {
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
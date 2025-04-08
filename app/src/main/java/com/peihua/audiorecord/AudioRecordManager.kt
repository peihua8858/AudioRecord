package com.peihua.audiorecord

import android.Manifest
import android.media.AudioRecord
import androidx.annotation.RequiresPermission
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder

class AudioRecordManager {
    private val audioRecorder = AudioRecorder()
    private val audioConverter = AudioConverter(audioRecorder)
    private val audioPlayer = AudioPlayer(audioRecorder, audioConverter)


    var audioRecord: AudioRecord? = null
    private var addLog: (String) -> Unit = {}
    private var updateStatus: (String) -> Unit = {}
    fun setAddLog(addLog: (String) -> Unit) {
        this.addLog = addLog
        audioRecorder.setLogger(addLog)
    }

    fun setUpdateStatus(updateStatus: (String) -> Unit) {
        this.updateStatus = updateStatus
        audioRecorder.setUpdateStatus(updateStatus)
    }

    public fun setFilePath(filePath: String) {
        audioConverter.filePath = filePath;
    }

    fun setPcmFilePath(pcmFilePath: String) {
        audioRecorder.pcmFilePath = pcmFilePath;
    }

    public fun setRecording(recording: Boolean) {
        audioRecorder.setRecording(recording);
    }

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    public fun startRecordPcm() {
        audioRecorder.startRecordingPcm();
//        audioRecorder.startRecordAudio();
    }

    fun startPlayPcm() {
        audioPlayer.startPlayPcm()
    }

    fun startPlayPcm(file: File) {
        audioPlayer.startPlayPcm(file)
    }


    fun startPlayMp3() {
        audioPlayer.startPlayMp3()
    }

    fun startPlayMp3(mp3File: String) {
        audioPlayer.startPlayMp3(File(mp3File))
    }


    fun convertPcmToMp3() {
        audioConverter.convertPcmToMp3()
    }

    fun convertPcmToWav() {
        audioConverter.convertPcmToWav()
    }

    fun convertPcmToOpus() {
        audioConverter.convertOpusToMp3("")
    }

    companion object {

        private val instance = AudioRecordManager()

        @JvmStatic
        fun getInstance(): AudioRecordManager {
            return instance
        }
    }

    fun rawToWave(rawFile: File, waveFile: File) {
        val rawData = ByteArray(rawFile.length().toInt())
        var input: DataInputStream? = null
        try {
            input = DataInputStream(FileInputStream(rawFile))
            input.readFully(rawData)
        } finally {
            input?.close()
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
            writeInt(output, 44100) // sample rate
            writeInt(output, (audioRecord?.sampleRate ?: 8000) * 2) // byte rate
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
            output?.close()
        }
    }

    private fun fullyReadFileToBytes(f: File): ByteArray {
        val size = f.length().toInt()
        val bytes = ByteArray(size)
        val tmpBuff = ByteArray(size)
        val fis = FileInputStream(f)
        try {
            val read = fis.read(bytes, 0, size)
            if (read < size) {
                var remain = size - read
                while (remain > 0) {
                    val r = fis.read(tmpBuff, 0, remain)
                    System.arraycopy(tmpBuff, 0, bytes, size - remain, r)
                    remain -= r
                }
            }
        } catch (e: IOException) {
            throw e
        } finally {
            fis.close()
        }
        return bytes
    }

    private fun writeInt(output: DataOutputStream, value: Int) {
        output.writeByte(value ushr 0 and 0xFF)
        output.writeByte(value ushr 8 and 0xFF)
        output.writeByte(value ushr 16 and 0xFF)
        output.writeByte(value ushr 24 and 0xFF)
    }

    private fun writeShort(output: DataOutputStream, value: Short) {
        output.writeByte(value.toInt() ushr 0 and 0xFF)
        output.writeByte(value.toInt() ushr 8 and 0xFF)
    }

    private fun writeString(output: DataOutputStream, value: String) {
        for (i in 0 until value.length) {
            output.writeChar(value[i].code)
        }
    }
}
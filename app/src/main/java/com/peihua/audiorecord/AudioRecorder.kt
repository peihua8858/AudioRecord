package com.peihua.audiorecord

import android.Manifest
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.annotation.RequiresPermission
import java.io.BufferedOutputStream
import java.io.DataOutputStream
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

/**
 * 音频录制
 */
class AudioRecorder(
    val simpleRateInHz: Int = SAMPLE_RATE_INHZ,
    val channelConfig: Int = CHANNEL_CONFIG,
    val audioFormat: Int = AUDIO_FORMAT,
) {
    private var minBuffer = 0

    var pcmFilePath: String = ""
    private var isRecording = false
    private var mChannelCount: Int = 0
    private var audioRecord: AudioRecord? = null
    private var outputStream: FileOutputStream? = null
    private var mLogger: (String) -> Unit = {

    }
    private var mUpdateStatus: (String) -> Unit = {

    }
    val channelCount: Int
        get() {
            if (mChannelCount > 0) {
                return mChannelCount
            }
            // 检查并返回声道数量
            var count = 0;
            if (channelConfig and AudioFormat.CHANNEL_IN_LEFT != 0) {
                count++ // 检查左声道
            }

            if (channelConfig and AudioFormat.CHANNEL_IN_RIGHT != 0) {
                count++ // 检查右声道
            }
            if (channelConfig and AudioFormat.CHANNEL_IN_FRONT != 0) {
                count++ // 检查右声道
            }
            if (channelConfig and AudioFormat.CHANNEL_IN_BACK != 0) {
                count++ // 检查右声道
            }
            if (channelConfig and AudioFormat.CHANNEL_IN_FRONT != 0) {
                count++ // 检查右声道
            }
            return count
        }

    fun setLogger(mLogger: (String) -> Unit) {
        this.mLogger = mLogger
    }

    fun setUpdateStatus(mUpdateStatus: (String) -> Unit) {
        this.mUpdateStatus = mUpdateStatus
    }

    fun setRecording(recording: Boolean) {
        isRecording = recording
    }

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    fun startRecordingPcm() {
        minBuffer = AudioRecord.getMinBufferSize(
            simpleRateInHz, channelConfig,
            audioFormat
        )

        addLog("Initializing audio recorder...")
        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC, simpleRateInHz,
            channelConfig,
            audioFormat, minBuffer
        )

        // Start audio recording
        addLog("Creating output file stream")
        try {
            outputStream = FileOutputStream(pcmFilePath)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        val bufferedOutputStream = BufferedOutputStream(outputStream)
        val dataOutputStream = DataOutputStream(bufferedOutputStream)
        addLog("started audio recording")
        updateStatus("Recording...")
        audioRecord!!.startRecording()

        val buffer = ByteArray(minBuffer) // Use minBuffer for reading
        var bytesRead: Int
        while (isRecording) {
            addLog("reading to short array buffer, buffer sze- $minBuffer")
            bytesRead = audioRecord!!.read(buffer, 0, buffer.size)
            addLog("bytes read=$bytesRead")
            if (bytesRead > 0) {
                addLog("encoding bytes to mp3 buffer..")
                try {
                    addLog("writing mp3 buffer to output stream with $bytesRead bytes")
                    outputStream!!.write(buffer, 0, bytesRead)
                } catch (e: IOException) {
                    e.printStackTrace()
                    break // Exit if there's an error while writing
                }
            }
        }

        addLog("stopped recording")
        updateStatus("Recording stopped")

        addLog("flushing final mp3buffer")
        try {
            dataOutputStream.flush() // Flush any remaining data in the buffer
            dataOutputStream.close() // Close the data output stream
        } catch (e: IOException) {
            e.printStackTrace()
        }
        addLog("releasing audio recorder")
        audioRecord!!.stop()
        audioRecord!!.release()
        isRecording = false
    }

    /**
     * 开始录音&#xff0c;返回临时缓存文件&#xff08;.pcm&#xff09;的文件路径
     */
    fun startRecordAudio(): String? {
        val audioCacheFilePath = pcmFilePath!!
        try {
            // 获取最小录音缓存大小&#xff0c;
            val minBufferSize =
                AudioRecord.getMinBufferSize(simpleRateInHz, channelConfig, audioFormat)
            val audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                simpleRateInHz,
                channelConfig,
                audioFormat,
                minBufferSize
            )
            this.audioRecord = audioRecord
            // 开始录音
            this.isRecording = true
            this.mChannelCount = audioRecord.channelCount
            audioRecord!!.startRecording()

            // 创建数据流&#xff0c;将缓存导入数据流
            val file = File(audioCacheFilePath)
            Logcat.i("audio cache pcm file path:" + audioCacheFilePath)
            /*
             *  以防万一&#xff0c;看一下这个文件是不是存在&#xff0c;如果存在的话&#xff0c;先删除掉
             */
            if (file.exists()) {
                file.delete()
            }

            try {
                file.createNewFile()
            } catch (e: IOException) {
                e.printStackTrace()
            }

            var fos: FileOutputStream? = null
            try {
                fos = FileOutputStream(file)
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
                Logcat.e("临时缓存文件未找到")
            }
            if (fos == null) {
                return null
            }

            val data = ByteArray(minBufferSize)
            var read: Int
            if (fos != null) {
                while (isRecording) {
                    read = audioRecord!!.read(data, 0, minBufferSize)
                    if (AudioRecord.ERROR_INVALID_OPERATION != read) {
                        try {
                            fos.write(data)
                            Logcat.i("audioRecordTest", "写录音数据-&gt;" + read)
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                }
            }

            try {
                // 关闭数据流
                fos.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        } catch (e: IllegalStateException) {
            Logcat.w("需要获取录音权限")
        } catch (e: SecurityException) {
            Logcat.w("需要获取录音权限")
        }
        return audioCacheFilePath
    }

    fun addLog(log: String) {
        mLogger.invoke(log)
    }

    fun updateStatus(status: String) {
        mUpdateStatus.invoke(status)
    }

    companion object {
        // 声道数。CHANNEL_IN_MONO and CHANNEL_IN_STEREO. 其中CHANNEL_IN_MONO是可以保证在所有设备能够使用的。
        val CHANNEL_CONFIG: Int = AudioFormat.CHANNEL_IN_STEREO

        // 返回的音频数据的格式。 ENCODING_PCM_8BIT, ENCODING_PCM_16BIT, and ENCODING_PCM_FLOAT.
        val AUDIO_FORMAT: Int = AudioFormat.ENCODING_PCM_16BIT
        const val SAMPLE_RATE_INHZ: Int = 44100
    }
}

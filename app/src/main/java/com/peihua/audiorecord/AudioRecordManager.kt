package com.peihua.audiorecord

import android.content.Context
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.os.Build
import android.os.Environment
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder

class AudioRecordManager {
    var audioRecord: AudioRecord? = null
    var pcmFile: File? = null
    var isRecording = false
    var mp3File: File? = null
    var minBuffer = 0;
    var inSamplerate = 8000;
    var outputStream: FileOutputStream? = null;
    private var addLog: (String) -> Unit = {}
    private var updateStatus: (String) -> Unit = {}
    fun setAddLog(addLog: (String) -> Unit) {
        this.addLog = addLog
    }

    fun setUpdateStatus(updateStatus: (String) -> Unit) {
        this.updateStatus = updateStatus
    }

    @RequiresPermission(android.Manifest.permission.RECORD_AUDIO)
    fun startRecordPcm(context: Context, isRecord: Boolean) {
        if (!isRecord) {
            isRecording = false
            return
        }
        startRecording(context)
    }

//    private suspend fun stopRecording() {
//        withContext(Dispatchers.IO) {
//            if (audioRecord != null) {
//                try {
//                    // 停止录音
//                    audioRecord?.stop()
//                } catch (e: Exception) {
//                    e.printStackTrace()
//                } finally {
//                    audioRecord?.release()
//                    audioRecord = null
//                    startRecording = false
//                }
//            }
//        }
//    }

    @RequiresPermission(android.Manifest.permission.RECORD_AUDIO)
    private fun startRecording(context: Context) {
        //缓冲区大小
        minBuffer = AudioRecord.getMinBufferSize(
            inSamplerate, AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        addLog("初始化的录音机..");
        val parentFile = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
        pcmFile = File(parentFile, "test33.mp3")
        if (pcmFile?.exists() == true) {
            pcmFile?.delete()
        }
        pcmFile?.createNewFile()
        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC, inSamplerate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT, minBuffer * 2
        );
        addLog("创建短缓冲区数组");
        val buffer = ShortArray(inSamplerate * 2 * 5);
        addLog("创建PCM缓冲区");
        val mp3buffer = ByteArray((7200 + buffer.size * 2 * 1.25).toInt())
        outputStream = pcmFile?.outputStream()
        addLog("开始录音");
        updateStatus("正在录音...");
        audioRecord?.startRecording()
        isRecording = true
        try {
            var bytesRead = 0;
            while (isRecording) {
                addLog("读取短阵列缓冲区，缓冲区尺寸 - " + minBuffer);
                bytesRead = audioRecord?.read(buffer, 0, minBuffer) ?: 0;
                addLog("bytes read=" + bytesRead);
                // 只有当写入的大小大于0时才写入文件
                if (bytesRead > 0) {
                    try {
                        addLog("将PCM缓冲区写入输出流 $bytesRead bytes");
                        outputStream?.write(mp3buffer, 0, bytesRead);
                    } catch (e: IOException) {
                        e.printStackTrace();
                    }
                }
            }
            addLog("停止录音");
            updateStatus("录音停止了");

        } finally {
            try {
                outputStream?.flush()
                addLog("flushing final mp3buffer");
                outputStream?.close();
                addLog("releasing audio recorder");
                audioRecord?.stop()
                audioRecord?.release()
                addLog("closing android lame");
                isRecording = false;
                audioRecord = null
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun startPlayPcm(isPlay: Boolean) {
        val pcmFile = pcmFile
        if (isPlay && pcmFile?.exists() == true) {
            val bufferSize = AudioTrack.getMinBufferSize(
                RECORDER_SAMPLERATE,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )

            val audioTrack = AudioTrack(
                AudioManager.STREAM_MUSIC,
                RECORDER_SAMPLERATE,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize,
                AudioTrack.MODE_STREAM
            )

            audioTrack.play()

            val inputStream = FileInputStream(pcmFile)
            val byteArrayOutputStream = ByteArray(bufferSize)
            var read: Int

            while (inputStream.read(byteArrayOutputStream).also { read = it } > 0) {
                audioTrack.write(byteArrayOutputStream, 0, read)
            }

            audioTrack.stop()
            audioTrack.release()
            inputStream.close()
        }
    }
//    fun startPlayPcm(isPlay: Boolean) {
//        // 假设你有一个MediaPlayer实例用于播放MP3文件
//        val mediaPlayer = MediaPlayer()
//        val pcmFile = pcmFile
//        if (isPlay && pcmFile?.exists() == true) {
//            try {
//                mediaPlayer.setDataSource(pcmFile.absolutePath) // 设置MP3文件路径
//                mediaPlayer.prepareAsync() // 异步准备媒体资源
//                mediaPlayer.setOnPreparedListener {
//                    dLog { "startPlayPcm" }
//                    it.start() // 开始播放
//                }
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//        } else {
//            mediaPlayer.stop()
//        }
//    }

    fun startPlayMp3(isPlay: Boolean) {
        val mp3File = this@AudioRecordManager.mp3File
        if (isPlay && mp3File?.exists() == true) {
            val bufferSize = AudioTrack.getMinBufferSize(
                RECORDER_SAMPLERATE,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )

            val audioTrack = AudioTrack(
                AudioManager.STREAM_MUSIC,
                RECORDER_SAMPLERATE,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize,
                AudioTrack.MODE_STREAM
            )

            audioTrack.play()

            val inputStream = FileInputStream(mp3File)
            val byteArrayOutputStream = ByteArray(bufferSize)
            var read: Int

            while (inputStream.read(byteArrayOutputStream).also { read = it } > 0) {
                audioTrack.write(byteArrayOutputStream, 0, read)
            }

            audioTrack.stop()
            audioTrack.release()
            inputStream.close()
        }
    }

    fun startPlayMp3(mp3File: String) {
        startPlayMp3(File(mp3File))
    }

    fun startPlayMp3(mp3File: File) {
        if (mp3File.exists() == true) {
            val bufferSize = AudioTrack.getMinBufferSize(
                RECORDER_SAMPLERATE,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )

            val audioTrack = AudioTrack(
                AudioManager.STREAM_MUSIC,
                RECORDER_SAMPLERATE,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize,
                AudioTrack.MODE_STREAM
            )

            audioTrack.play()

            val inputStream = FileInputStream(mp3File)
            val byteArrayOutputStream = ByteArray(bufferSize)
            var read: Int

            while (inputStream.read(byteArrayOutputStream).also { read = it } > 0) {
                audioTrack.write(byteArrayOutputStream, 0, read)
            }

            audioTrack.stop()
            audioTrack.release()
            inputStream.close()
        }
    }

    suspend fun convertPcmToMp3() {
        withContext(Dispatchers.IO) {
            if (pcmFile?.exists() == true) {
                val mp3File = File(pcmFile?.parentFile, "test.mp3")
                if (mp3File.exists()) {
                    mp3File.delete()
                }
                mp3File.createNewFile()
                rawToWave(pcmFile!!, mp3File)
            }
        }
    }


    companion object {
        final const val RECORDER_SAMPLERATE = 44100;

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
package com.peihua.audiorecord

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import java.io.File
import java.io.FileInputStream

class AudioPlayer(val audioRecorder: AudioRecorder, val audioConverter: AudioConverter) {
    fun startPlayPcm() {
        val pcmFile = File(audioRecorder.pcmFilePath)
        startPlayPcm(pcmFile)
    }

    fun startPlayPcm(pcmFile: File) {
        if (pcmFile.exists()) {
            val bufferSize = AudioTrack.getMinBufferSize(
                audioRecorder.simpleRateInHz,
                audioRecorder.channelConfig,
                audioRecorder.audioFormat
            )

            val audioTrack = AudioTrack(
                AudioManager.STREAM_MUSIC,
                audioRecorder.simpleRateInHz,
                audioRecorder.channelConfig,
                audioRecorder.audioFormat,
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

    fun startPlayMp3() {
        val mp3File = audioConverter.convertMp3File
        startPlayMp3(mp3File)
    }

    fun startPlayMp3(file: File) {
        val mp3File = file
        if (mp3File.exists() == true) {
            val mediaInfo = MediaDecoder(mp3File).mediaInfo
            val bufferSize = AudioTrack.getMinBufferSize(
                mediaInfo.sampleRate.toInt(),
                AudioFormat.CHANNEL_IN_STEREO,
                AudioFormat.ENCODING_PCM_16BIT
            )

            val audioTrack = AudioTrack(
                AudioManager.STREAM_MUSIC,
                mediaInfo.sampleRate.toInt(),
                AudioFormat.CHANNEL_IN_STEREO,
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
}
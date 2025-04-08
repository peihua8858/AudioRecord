package com.peihua.audiorecord

import com.arthenica.ffmpegkit.FFmpegKit
import com.score.rahasak.utils.OpusDecoder
import com.score.rahasak.utils.OpusEncoder
import linc.com.pcmdecoder.PCMDecoder
import java.io.ByteArrayOutputStream
import java.io.File

class AudioConverter(val audioRecorder: AudioRecorder) {
    private val pcmToWavUtil = PcmToWavUtil()
    private val convertMp3FileName = "convertOutput.mp3"
    private val convertWavFileName = "convertOutput.wav"
    private val convertOpusFileName = "convertOutput.opus"
    var filePath: String = ""
    val pcmFilePath: String
        get() = audioRecorder.pcmFilePath
    val convertParentFile: File
        get() {
            val pcmFilePath = audioRecorder.pcmFilePath
            val pcmFile = File(pcmFilePath)
            return pcmFile.parentFile
        }
    val convertMp3File: File
        get() = File(convertParentFile, convertMp3FileName)
    val convertWavFile: File
        get() = File(convertParentFile, convertWavFileName)
    val convertOpusFile: File
        get() = File(convertParentFile, convertOpusFileName)

    private fun addLog(log: String) {
        audioRecorder.addLog(log)
    }

    private fun parseFile(defaultFile: File, isMp3: Boolean): File {
        val audioFile = if (filePath.isEmpty()) {
            defaultFile
        } else {
            val file = File(filePath)
            if (isMp3) {
                if (file.extension == "mp3") {
                    file
                } else {
                    defaultFile
                }
            } else {
                if (file.extension == "wav") {
                    file
                } else {
                    defaultFile
                }
            }
        }
        if (audioFile.exists()) {
            audioFile.delete()
        }
        return audioFile
    }

    fun convertPcmToMp3() {
        addLog("Initialising lame..");
        val mp3File = parseFile(convertMp3File, true)

//        val mediaInfo = MediaDecoder(audioRecorder.pcmFilePath, ::addLog).mediaInfo
        val bitRate = audioRecorder.bitRate
        val simpleRate = audioRecorder.simpleRateInHz
        val channelCount = audioRecorder.channelCount
        //采样位
//        val digit = mediaInfo.digit
        val byteRate = audioRecorder.byteRate
        addLog("MediaFormat bitRate:$bitRate, simpleRate:$simpleRate, channelCount:$channelCount, byteRate:$byteRate");
        PCMDecoder.encodeToMp3(
            pcmFilePath,
            channelCount,
            bitRate.toInt(),
            simpleRate.toInt(),
            mp3File.absolutePath
        );
        addLog("pcmToMp3 success");

    }

    fun convertPcmToWav() {
        val wavFile = parseFile(convertWavFile, false)
//        val mediaInfo = MediaDecoder(audioRecorder.pcmFilePath, ::addLog).mediaInfo
        val bitRate = audioRecorder.bitRate
        val simpleRate = audioRecorder.simpleRateInHz
        val channelCount = audioRecorder.channelCount
        //采样位
//        val digit = mediaInfo.digit
        val byteRate = audioRecorder.byteRate
        addLog("MediaFormat bitRate:$bitRate, simpleRate:$simpleRate, channelCount:$channelCount,  byteRate:$byteRate");
        pcmToWavUtil.pcmToWav(
            pcmFilePath,
            wavFile.absolutePath,
            channelCount,
            byteRate.toLong(),
            simpleRate.toLong()
        )
        addLog("pcmToWav success");
    }

    fun convertPcmToOpus() {
        val opusFile = parseFile(convertOpusFile, false)

//        val mediaInfo = MediaDecoder(audioRecorder.pcmFilePath, ::addLog).mediaInfo
        val bitRate = audioRecorder.bitRate
        val simpleRate = audioRecorder.simpleRateInHz
        val channelCount = audioRecorder.channelCount
        //采样位
//        val digit = mediaInfo.digit
        val byteRate = audioRecorder.byteRate
        addLog("MediaFormat bitRate:$bitRate, simpleRate:$simpleRate, channelCount:$channelCount,  byteRate:$byteRate");
        val inputStream = File(pcmFilePath).inputStream()
        val outputStreamByte = ByteArrayOutputStream()
        val byte = ByteArray(1024)
        while (inputStream.read(byte) != -1) {
            outputStreamByte.write(byte)
        }
        val outputStream = opusFile.outputStream()
        val outByte = ByteArray(outputStreamByte.size())
        OpusEncoder().encode(outputStreamByte.toByteArray(), 160, outByte)
        outputStream.write(outByte)
        addLog("pcmToOpus success");
    }

    fun convertOpusToMp3() {
        val opusFile = parseFile(convertMp3File, true)

//        val mediaInfo = MediaDecoder(audioRecorder.pcmFilePath, ::addLog).mediaInfo
        val bitRate = audioRecorder.bitRate
        val simpleRate = audioRecorder.simpleRateInHz
        val channelCount = audioRecorder.channelCount
        //采样位
//        val digit = mediaInfo.digit
        val byteRate = audioRecorder.byteRate
        addLog("MediaFormat bitRate:$bitRate, simpleRate:$simpleRate, channelCount:$channelCount,  byteRate:$byteRate");
        val inputStream = File(pcmFilePath).inputStream()
        val outputStreamByte = ByteArrayOutputStream()
        val byte = ByteArray(1024)
        while (inputStream.read(byte) != -1) {
            outputStreamByte.write(byte)
        }
        val outputStream = opusFile.outputStream()
        val outByte = ByteArray(outputStreamByte.size())
        OpusDecoder().decode(outputStreamByte.toByteArray(), outByte, 160)
        outputStream.write(outByte)
        addLog("pcmToOpus success");
    }
    fun convertOpusToMp3(opusFilePath: String) {
        val mp3File = parseFile(convertMp3File, true)
        convertOpusToMp3(opusFilePath, mp3File.absolutePath)
    }
    fun convertOpusToMp3(opusFilePath: String, mp3FilePath: String) {
       val opusFile= File("/sdcard/Android/data/com.peihua.audiorecord/files/Music/2222.opus")
        FFmpegConverter.convertOpusToMp3(opusFile.absolutePath, mp3FilePath) {
            addLog(it)
        }
    }
}
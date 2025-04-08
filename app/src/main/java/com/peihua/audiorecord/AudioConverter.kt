package com.peihua.audiorecord

import linc.com.pcmdecoder.PCMDecoder
import java.io.File

class AudioConverter(val audioRecorder: AudioRecorder) {
    private val pcmToWavUtil = PcmToWavUtil()
    private val convertMp3FileName = "convertOutput.mp3"
    private val convertWavFileName = "convertOutput.wav"
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

        val mediaInfo = MediaDecoder(audioRecorder.pcmFilePath, ::addLog).mediaInfo
        val bitRate = mediaInfo.bitRate
        val simpleRate = mediaInfo.sampleRate
        val channelCount = mediaInfo.channels
        //采样位
        val digit = mediaInfo.digit
        val byteRate = mediaInfo.byteRate
        addLog("MediaFormat bitRate:$bitRate, simpleRate:$simpleRate, channelCount:$channelCount, digit:$digit, byteRate:$byteRate");
        PCMDecoder.encodeToMp3(
            pcmFilePath,
            channelCount,
            bitRate.toInt(),
            simpleRate.toInt(),
            mp3File.absolutePath
        );
        addLog("pcmToMp3 success");

        pcmConvertMp3.convertWaveToMp3(pcmFile, mp3File);
    }

    fun convertPcmToWav() {
        val wavFile = parseFile(convertWavFile, false)
        val mediaInfo = MediaDecoder(audioRecorder.pcmFilePath, ::addLog).mediaInfo
        val bitRate = mediaInfo.bitRate
        val simpleRate = mediaInfo.sampleRate
        val channelCount = mediaInfo.channels
        //采样位
        val digit = mediaInfo.digit
        val byteRate = mediaInfo.byteRate
        addLog("MediaFormat bitRate:$bitRate, simpleRate:$simpleRate, channelCount:$channelCount, digit:$digit, byteRate:$byteRate");
        pcmToWavUtil.pcmToWav(
            pcmFilePath,
            wavFile.absolutePath,
            channelCount,
            byteRate,
            simpleRate
        )
        addLog("pcmToWav success");
    }
    fun convertOpusToMp3() {
        addLog("Initialising lame..");
    }
}
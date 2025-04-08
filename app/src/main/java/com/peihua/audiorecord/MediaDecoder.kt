package com.peihua.audiorecord

import android.media.MediaExtractor
import android.media.MediaFormat
import java.io.File

class MediaDecoder(private val filePath: String, val addLog: (String) -> Unit = {}) {
    constructor(file: File, addLog: (String) -> Unit = {}) : this(file.absolutePath, addLog)

    val mediaInfo: MediaInfo

    init {
        val mediaExtractor = MediaExtractor()
        addLog("MediaExtractor init success, filePath:$filePath");
        try {
            mediaExtractor.setDataSource(filePath)
            addLog("MediaExtractor setDataSource success, filePath:$filePath");
        } catch (e: Exception) {
            e.printStackTrace()
           addLog("MediaExtractor setDataSource error, filePath:$filePath");
        }

        val mediaFormat = mediaExtractor.getTrackFormat(0)
        addLog("MediaFormat init success");
        val bitRate = mediaFormat.getLong(MediaFormat.KEY_BIT_RATE)
        val simpleRate = mediaFormat.getLong(MediaFormat.KEY_SAMPLE_RATE)
        val channelCount = mediaFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
        //采样位
        val digit = bitRate * 8L / (simpleRate * channelCount)
        val byteRate = bitRate * simpleRate * channelCount / 8L
        mediaInfo = MediaInfo(
            mime = mediaFormat.getString(MediaFormat.KEY_MIME) ?: "",
            sampleRate = simpleRate,
            channels = channelCount,
            bitRate = bitRate,
            byteRate = byteRate,
            digit = digit,
            duration = mediaFormat.getLong(MediaFormat.KEY_DURATION)
        )
    }
}

data class MediaInfo(
    val mime: String,
    val sampleRate: Long,
    val channels: Int,
    val bitRate: Long,
    val byteRate: Long,
    val digit: Long,
    val duration: Long,
)
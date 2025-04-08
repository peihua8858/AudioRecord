package com.peihua.audiorecord

import android.content.Context
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode

object FFmpegConverter {
    /**
     * 参数详细说明
     * -f s16le:
     *
     * -f: 指定输入或输出文件的格式。
     * s16le: 表示输入文件的格式为 16 位小端 PCM（Pulse Code Modulation）。这里 s16le 是 "signed 16-bit little-endian" 的缩写。它表示该音频数据是使用 16 位量化的，并且是小端格式，这对 PCM 文件来说是常见的设置。
     * -ar 8000:
     *
     * -ar: 指定音频的采样率（sample rate）。采样率是指每秒钟对音频信号的采样次数。
     * 8000: 设置采样率为 8000 Hz（每秒 8000 次采样）。这个采样率通常用于电话音频或低质量录音。如果您的 PCM 文件的实际采样率不同，则需要相应地调整此值。
     * -ac 1:
     *
     * -ac: 指定音频的通道数（number of audio channels）。
     * 1: 设置通道数为 1，表示单声道（Mono）。如果您的 PCM 文件是立体声（Stereo），应将此值更改为 2。
     * -i %s:
     *
     * -i: 指定输入文件。当 FFmpeg 处理音频或视频时，必须提供输入文件。
     * %s: 这是 Java 的格式化字符串部分，它在运行时会被替换为实际的 PCM 文件路径，即 pcmFilePath。
     * -acodec libmp3lame:
     *
     * -acodec: 指定音频编解码器（audio codec）。FFmpeg 里有多种编解码器供选择。
     * libmp3lame: 指定使用 LAME MP3 编码器将音频编码为 MP3 格式。LAME 是一种流行且高效的 MP3 编码库，能够产生质量良好的 MP3 文件。
     * %s:
     *
     * 这是另一个 Java 格式化字符串部分，将在运行时替换为输出文件的路径，即 mp3FilePath。这将是您转换后想要保存 MP3 文件的位置。
     * @param context
     * @param sampleRate
     * @param pcmFilePath
     * @param mp3FilePath
     */
    fun convertPcmToMp3(
        sampleRate: Int,
        channels: Int,
        pcmFilePath: String,
        mp3FilePath: String?,
        addLog: (String) -> Unit
    ) {
        // FFmpeg 命令字符串
        addLog("组装FFmpeg 命令")
        val cmd = String.format(
            "-f s16le -ar " + sampleRate + " -ac " + channels + " -i %s -acodec libmp3lame %s",
            pcmFilePath,
            mp3FilePath
        )
        addLog("FFmpeg 命令: $cmd")
        addLog("执行FFmpeg 命令")
        val session = FFmpegKit.execute(cmd)
        addLog("执行FFmpeg 命令完成")
        if (ReturnCode.isSuccess(session.getReturnCode())) {
            // Handle success case
            addLog("Conversion Success ")
            Logcat.d("Conversion Success with return code: " + session.getReturnCode())
        } else {
            // Handle failure case
            addLog("Conversion failed with return code: " + session.getReturnCode())
            Logcat.d("Conversion failed with return code: " + session.getReturnCode())
        }
    }

    fun convertOpusToMp3(opusFilePath: String, mp3FilePath: String, addLog: (String) -> Unit) {
        // Construct the FFmpeg command
        addLog("组装FFmpeg 命令")
        val cmd = "-i $opusFilePath -acodec libmp3lame $mp3FilePath"
        addLog("FFmpeg 命令: $cmd")
        addLog("执行FFmpeg 命令")
        // Execute the command
        val session = FFmpegKit.execute(cmd)
        addLog("执行FFmpeg 命令完成")
        if (ReturnCode.isSuccess(session.getReturnCode())) {
            // Handle success case
            addLog("Conversion Success ")
            Logcat.d("Conversion Success with return code: " + session.getReturnCode())
        } else {
            // Handle failure case
            addLog("Conversion failed with return code: " + session.getReturnCode())
            Logcat.d("Conversion failed with return code: " + session.getReturnCode())
        }
    }
}
package com.peihua.audiorecord;

import android.content.Context;

import com.arthenica.ffmpegkit.FFmpegKit;
import com.arthenica.ffmpegkit.FFmpegSession;
import com.arthenica.ffmpegkit.ReturnCode;


public class PcmToMp3Converter {
    public static void convertPcmToMp3(Context context, String pcmFilePath, String mp3FilePath) {
        // FFmpeg 命令字符串
        String cmd = String.format("-f s16le -ar 8000 -ac 1 -i %s -acodec libmp3lame %s", pcmFilePath, mp3FilePath);
        FFmpegSession session = FFmpegKit.execute(cmd);
        if (ReturnCode.isSuccess(session.getReturnCode())) {
            // Handle success case
            Logcat.d("Conversion Success with return code: " + session.getReturnCode());
        } else {
            // Handle failure case
           Logcat.d("Conversion failed with return code: " + session.getReturnCode());
        }
    }
}
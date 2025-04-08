package com.peihua.coverter;

public class OpusConverter {
    static {
        System.loadLibrary("OpusCodec");
    }
    public static native void pcmToOpus(String pcmFilePath, String opusFilePath);
    public static native void opusToMp3(String opusFilePath, String mp3FilePath);


}

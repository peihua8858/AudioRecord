package com.peihua.coverter;

public class OpusConverter {
    static {
        System.loadLibrary("opusConverter");
    }
    public static native void pcmToOpus(String pcmFilePath, String opusFilePath);
    public static native void opusToMp3(String opusFilePath, String mp3FilePath);


}

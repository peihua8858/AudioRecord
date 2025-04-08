//package com.peihua.audiorecord;
//
//import com.naman14.lame.Lame;
//
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileOutputStream;
//import java.io.IOException;
//
//public class AudioConverter {
//    public void convertPToMP3(File pcmFile, File mp3File) {
//        int sampleRate = 44100; // Sample rate in Hz
//        int bitRate = 128; // Bit rate in kbps
//        try {
//            FileInputStream fis = new FileInputStream(pcmFile);
//            FileOutputStream fos = new FileOutputStream(mp3File);
//            Lame.init(sampleRate, 1, sampleRate, bitRate);
//            byte[] buffer = new byte[1024];
//            int bytesRead;
//            while ((bytesRead = fis.read(buffer)) > 0) {
//                byte[] mp3buffer = new byte[(int) (1.25 * bytesRead) + 7200];
//                int bytesEncoded = Lame.encode(buffer, buffer, bytesRead, mp3buffer);
//                if (bytesEncoded > 0) {
//                    fos.write(mp3buffer, 0, bytesEncoded);
//                    //登录后复制
//                    int outputSize = Lame.flush(mp3buffer);
//                    if (outputSize > 0) §
//                    fos.write(mp3buffer, 0, outputSize);
//                }
//                Lame.close();
//                fis.close();
//                fos.close;
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//}
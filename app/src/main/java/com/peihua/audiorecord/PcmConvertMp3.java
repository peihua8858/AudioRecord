package com.peihua.audiorecord;

import com.naman14.androidlame.AndroidLame;
import com.naman14.androidlame.LameBuilder;
import com.naman14.androidlame.WaveReader;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class PcmConvertMp3 {
    private static final int OUTPUT_STREAM_BUFFER = 8192;
    BufferedOutputStream outputStream;
    WaveReader waveReader;
    private AudioRecordManager2.Logger mLogger;

    public void setLogger(AudioRecordManager2.Logger mLogger) {
        this.mLogger = mLogger;
    }

    private void addLog(final String log) {
        if (mLogger != null) {
            mLogger.addLog(log);
        }
    }

    private void updateStatus(final String status) {
        if (mLogger != null) {
            mLogger.updateStatus(status);
        }
    }
    public void convertWaveToMp3(File pcmFile, File mp3File) {
        encode(pcmFile, mp3File);
    }
    private void encode(File pcmFile, File mp3File) {
        int CHUNK_SIZE = 8192;
        addLog("Initialising wav reader");
        waveReader = new WaveReader(pcmFile);

        try {
            waveReader.openWave();
        } catch (IOException e) {
            e.printStackTrace();
        }

        addLog("Intitialising encoder");
        AndroidLame androidLame = new LameBuilder()
                .setInSampleRate(waveReader.getSampleRate())
                .setOutChannels(waveReader.getChannels())
                .setOutBitrate(128)
                .setOutSampleRate(waveReader.getSampleRate())
                .setQuality(5)
                .build();

        try {
            outputStream = new BufferedOutputStream(new FileOutputStream(mp3File), OUTPUT_STREAM_BUFFER);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        int bytesRead = 0;

        short[] buffer_l = new short[CHUNK_SIZE];
        short[] buffer_r = new short[CHUNK_SIZE];
        byte[] mp3Buf = new byte[CHUNK_SIZE];

        int channels = waveReader.getChannels();

        addLog("started encoding");
        while (true) {
            try {
                if (channels == 2) {

                    bytesRead = waveReader.read(buffer_l, buffer_r, CHUNK_SIZE);
                    addLog("bytes read=" + bytesRead);

                    if (bytesRead > 0) {

                        int bytesEncoded = 0;
                        bytesEncoded = androidLame.encode(buffer_l, buffer_r, bytesRead, mp3Buf);
                        addLog("bytes encoded=" + bytesEncoded);

                        if (bytesEncoded > 0) {
                            try {
                                addLog("writing mp3 buffer to outputstream with " + bytesEncoded + " bytes");
                                outputStream.write(mp3Buf, 0, bytesEncoded);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                    } else break;
                } else {

                    bytesRead = waveReader.read(buffer_l, CHUNK_SIZE);
                    addLog("bytes read=" + bytesRead);

                    if (bytesRead > 0) {
                        int bytesEncoded = 0;

                        bytesEncoded = androidLame.encode(buffer_l, buffer_l, bytesRead, mp3Buf);
                        addLog("bytes encoded=" + bytesEncoded);

                        if (bytesEncoded > 0) {
                            try {
                                addLog("writing mp3 buffer to outputstream with " + bytesEncoded + " bytes");
                                outputStream.write(mp3Buf, 0, bytesEncoded);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                    } else break;
                }


            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        addLog("flushing final mp3buffer");
        int outputMp3buf = androidLame.flush(mp3Buf);
        addLog("flushed " + outputMp3buf + " bytes");
        if (outputMp3buf > 0) {
            try {
                addLog("writing final mp3buffer to outputstream");
                outputStream.write(mp3Buf, 0, outputMp3buf);
                addLog("closing output stream");
                outputStream.close();
                addLog("Output recording saved in " + mp3File.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }
}

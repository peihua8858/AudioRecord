package com.peihua.audiorecord.ui.theme;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import androidx.annotation.RequiresPermission;

import com.naman14.androidlame.AndroidLame;
import com.naman14.androidlame.LameBuilder;
import com.peihua.audiorecord.AudioRecorder;
import com.peihua.audiorecord.MediaDecoder;
import com.peihua.audiorecord.PcmConvertMp3;
import com.peihua.audiorecord.PcmToMp3;
import com.peihua.audiorecord.PcmToMp3Converter;
import com.peihua.audiorecord.PcmToWavUtil;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import linc.com.pcmdecoder.PCMDecoder;

public class AudioRecordManager2 {
    private static class InstanceHolder {
        private static final AudioRecordManager2 INSTANCE = new AudioRecordManager2();
    }

    public static AudioRecordManager2 getInstance() {
        return InstanceHolder.INSTANCE;
    }

    private AudioRecorder audioRecorder = new AudioRecorder();
    private PcmConvertMp3 pcmConvertMp3 = new PcmConvertMp3();
    private MediaDecoder mediaDecoder = new MediaDecoder();
    private PcmToWavUtil pcmToWavUtil = new PcmToWavUtil();

    int minBuffer;
    int inSamplerate = 8000;

    String filePath;
    String pcmFilePath;
    String convertFilePath;
    boolean isRecording = false;

    AudioRecord audioRecord;
    AndroidLame androidLame;
    FileOutputStream outputStream;
    private Logger mLogger;

    public String getFilePath() {
        return filePath;
    }

    public String getPcmFilePath() {
        return pcmFilePath;
    }

    public String getConvertFilePath() {
        return convertFilePath;
    }

    public void setRecording(boolean recording) {
        isRecording = recording;
        audioRecorder.setRecording(recording);
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public void setPcmFilePath(String pcmFilePath) {
        this.pcmFilePath = pcmFilePath;
        audioRecorder.setPcmFilePath(pcmFilePath);
    }

    public void setLogger(Logger logger) {
        this.mLogger = logger;
        audioRecorder.setLogger(logger);
        pcmConvertMp3.setLogger(logger);
    }

    @SuppressLint("MissingPermission")
    public void startRecordPcm() {
//        audioRecorder.startRecordingPcm();
        audioRecorder.startRecordAudio(new File(pcmFilePath));
    }
    public void convertPcmToWav() {
        pcmToWavUtil.pcmToWav(pcmFilePath, convertFilePath);
    }
    public void convertPcmToMp3(Context context) throws IOException {
        addLog("Initialising lame..");
//        File pcmFile = new File("/storage/emulated/0/Android/data/com.peihua.audiorecord/files/Music/test112233.pcm");
        File pcmFile = new File(pcmFilePath);
        File mp3File = new File(pcmFile.getParentFile(), "convertOutput.mp3");
        if (mp3File.exists()) {
            mp3File.delete();
        }
        PcmToMp3Converter.convertPcmToMp3(context, pcmFile.getAbsolutePath(), mp3File.getAbsolutePath());
//         pcmConvertMp3.convertWaveToMp3(pcmFile, mp3File);
//        convertFilePath = mp3File.getAbsolutePath();
//        mediaDecoder.setDataSrcPath(pcmFile.getAbsolutePath());
//        mediaDecoder.setDataDestPath(mp3File.getAbsolutePath());
//        mediaDecoder.start();
//        PcmToMp3.convertAudioFiles(pcmFile.getAbsolutePath(), mp3File.getAbsolutePath());
//        PCMDecoder.encodeToMp3(pcmFilePath, 1, 96000, 22000, mp3File.getAbsolutePath());
    }

    public void convertPcmToMp32() throws IOException {
        addLog("Initialising lame..");
        File pcmFile = new File(pcmFilePath);
        File mp3File = new File(pcmFile.getParentFile(), "convertOutput.mp3");
        convertFilePath = mp3File.getAbsolutePath();
        androidLame = new LameBuilder()
                .setInSampleRate(inSamplerate)
                .setOutChannels(1)
                .setOutBitrate(32)
                .setOutSampleRate(inSamplerate)
                .build();
        try {

            FileInputStream pcmInputStream = new FileInputStream(pcmFile);
            FileOutputStream mp3OutputStream = new FileOutputStream(mp3File);
            BufferedInputStream bufferedInputStream = new BufferedInputStream(pcmInputStream);
            DataInputStream dataInputStream = new DataInputStream(bufferedInputStream);
            short[] buffer = new short[inSamplerate * 2 * 5];
            byte[] mp3buffer = new byte[(int) (7200 + buffer.length * 2 * 1.25)];
            short bytesRead = 0;
            addLog("creating short buffer array");
            int index = 0;
            while ((bytesRead = dataInputStream.readShort()) != -1) {
                addLog("reading to short array buffer, buffer sze- " + buffer.length);
                addLog("encoding bytes to mp3 buffer..");
                buffer[index] = bytesRead;
                addLog("index=" + index + ",bytesRead=" + bytesRead);
                addLog("index=" + index + ",buffer[index]=" + buffer[index]);
                index++;
                addLog("<><>index=" + index + ",buffer.length=" + buffer.length);
                if (index == buffer.length) {
                    addLog("index=" + index + ",buffer.length=" + buffer.length);
                    index = 0;
                    int bytesEncoded = androidLame.encode(buffer, buffer, buffer.length, mp3buffer);
                    addLog("bytes encoded=" + bytesEncoded);
                    if (bytesEncoded > 0) {
                        addLog("writing mp3 buffer to outputstream with " + bytesEncoded + " bytes");
                        mp3OutputStream.write(mp3buffer, 0, bytesEncoded);
                    }
                }
            }
//            mp3OutputStream.flush();
            mp3OutputStream.close();
            pcmInputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public short[] toShortArray(byte[] data) {
        int count = data.length >> 1;
        short[] shortArray = new short[count];
        for (int i = 0; i < count; i++) {
            shortArray[i] = (short) (data[i * 2] << 8 | (data[i * 2 + 1] & 0xff));
        }
        return shortArray;
    }

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    public void startRecording() {
        minBuffer = AudioRecord.getMinBufferSize(inSamplerate, AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT);

        addLog("Initialising audio recorder..");
        audioRecord = new AudioRecord(
                MediaRecorder.AudioSource.MIC, inSamplerate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT, minBuffer * 2);

        //5 seconds data
        addLog("creating short buffer array");
        short[] buffer = new short[inSamplerate * 2 * 5];

        // 'mp3buf' should be at least 7200 bytes long
        // to hold all possible emitted data.
        addLog("creating mp3 buffer");
        byte[] mp3buffer = new byte[(int) (7200 + buffer.length * 2 * 1.25)];

        try {
            outputStream = new FileOutputStream(new File(filePath));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        addLog("Initialising Andorid Lame");
        androidLame = new LameBuilder()
                .setInSampleRate(inSamplerate)
                .setOutChannels(1)
                .setOutBitrate(32)
                .setOutSampleRate(inSamplerate)
                .build();

        addLog("started audio recording");
        updateStatus("Recording...");
        audioRecord.startRecording();

        int bytesRead = 0;

        while (isRecording) {

            addLog("reading to short array buffer, buffer sze- " + minBuffer);
            bytesRead = audioRecord.read(buffer, 0, minBuffer);
            addLog("bytes read=" + bytesRead);

            if (bytesRead > 0) {

                addLog("encoding bytes to mp3 buffer..");
                int bytesEncoded = androidLame.encode(buffer, buffer, bytesRead, mp3buffer);
                addLog("bytes encoded=" + bytesEncoded);

                if (bytesEncoded > 0) {
                    try {
                        addLog("writing mp3 buffer to outputstream with " + bytesEncoded + " bytes");
                        outputStream.write(mp3buffer, 0, bytesEncoded);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        addLog("stopped recording");
        updateStatus("Recording stopped");

        addLog("flushing final mp3buffer");
        int outputMp3buf = androidLame.flush(mp3buffer);
        addLog("flushed " + outputMp3buf + " bytes");

        if (outputMp3buf > 0) {
            try {
                addLog("writing final mp3buffer to outputstream");
                outputStream.write(mp3buffer, 0, outputMp3buf);
                addLog("closing output stream");
                outputStream.close();
                updateStatus("Output recording saved in " + filePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        addLog("releasing audio recorder");
        audioRecord.stop();
        audioRecord.release();

        addLog("closing android lame");
        androidLame.close();

        isRecording = false;
    }

    private void stopRecording() {
        isRecording = false;
    }

    private void addLog(final String log) {
        mLogger.addLog(log);
    }

    private void updateStatus(final String status) {
        mLogger.updateStatus(status);
    }

    public interface Logger {
        void addLog(String log);

        void updateStatus(String status);
    }


    //开始录音
    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    public void StartRecord() {
        //16K采集率
        int frequency = 16000;
        //格式
        int channelConfiguration = AudioFormat.CHANNEL_IN_MONO;
        //16Bit
        int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
        //生成PCM文件
        File file = new File(pcmFilePath);
        addLog("生成文件" + file.getAbsolutePath());
        // 如果存在，就先删除再创建
        if (file.exists()) {
            file.delete();
            addLog("删除文件" + file.getAbsolutePath());
        }
        try {
            file.createNewFile();
            addLog("创建文件" + file.getAbsolutePath());
        } catch (IOException e) {
            addLog("未能创建" + e.getMessage());
            throw new IllegalStateException("未能创建" + file.toString());
        }
        try {
            //输出流
            OutputStream os = new FileOutputStream(file);
            BufferedOutputStream bos = new BufferedOutputStream(os);
            DataOutputStream dos = new DataOutputStream(bos);
            int bufferSize = AudioRecord.getMinBufferSize(frequency, channelConfiguration, audioEncoding);
            AudioRecord audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, frequency, channelConfiguration, audioEncoding, bufferSize);
            short[] buffer = new short[bufferSize];
            audioRecord.startRecording();
            addLog("开始录音");
            while (isRecording) {
                addLog("reading to short array buffer, buffer sze- " + bufferSize);
                int bufferReadResult = audioRecord.read(buffer, 0, bufferSize);
                addLog("bytes read=" + bufferReadResult);
                for (int i = 0; i < bufferReadResult; i++) {
                    addLog("writing mp3 buffer to outputstream with " + buffer[i] + " bytes");
                    dos.writeShort(buffer[i]);
                }
            }
            addLog("stopped recording");
            updateStatus("Recording stopped");
            addLog("releasing audio recorder");
            dos.close();
            audioRecord.stop();
            audioRecord.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

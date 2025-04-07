package com.peihua.audiorecord;

import android.Manifest;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import androidx.annotation.RequiresPermission;

import com.peihua.audiorecord.ui.theme.AudioRecordManager2;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class AudioRecorder {
    private int minBuffer;
    private int inSamplerate = 8000;

    private String pcmFilePath;
    private boolean isRecording = false;

    private AudioRecord audioRecord;
    private FileOutputStream outputStream;
    private AudioRecordManager2.Logger mLogger;

    public void setPcmFilePath(String pcmFilePath) {
        this.pcmFilePath = pcmFilePath;
    }

    public void setLogger(AudioRecordManager2.Logger mLogger) {
        this.mLogger = mLogger;
    }

    public void setRecording(boolean recording) {
        isRecording = recording;
    }

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    public void startRecordingPcm() {
        minBuffer = AudioRecord.getMinBufferSize(inSamplerate, AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT);

        addLog("Initializing audio recorder...");
        audioRecord = new AudioRecord(
                MediaRecorder.AudioSource.MIC, inSamplerate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT, minBuffer);

        // Start audio recording
        addLog("Creating output file stream");
        try {
            outputStream = new FileOutputStream(pcmFilePath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);
        DataOutputStream dataOutputStream = new DataOutputStream(bufferedOutputStream);
        addLog("started audio recording");
        updateStatus("Recording...");
        audioRecord.startRecording();

        short[] buffer = new short[minBuffer]; // Use minBuffer for reading
        int bytesRead;
        while (isRecording) {
            addLog("reading to short array buffer, buffer sze- " + minBuffer);
            bytesRead = audioRecord.read(buffer, 0, buffer.length);
            addLog("bytes read=" + bytesRead);
            if (bytesRead > 0) {
                addLog("encoding bytes to mp3 buffer..");
                try {
                    addLog("writing mp3 buffer to outputstream with " + bytesRead + " bytes");
                    for (int i = 0; i < bytesRead; i++) {
                        // Ensure we write raw PCM data correctly
                        dataOutputStream.writeShort(buffer[i]);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    break; // Exit if there's an error while writing
                }
            }
        }

        addLog("stopped recording");
        updateStatus("Recording stopped");

        addLog("flushing final mp3buffer");
        try {
            dataOutputStream.flush(); // Flush any remaining data in the buffer
            dataOutputStream.close();  // Close the data output stream
        } catch (IOException e) {
            e.printStackTrace();
        }
        addLog("releasing audio recorder");
        audioRecord.stop();
        audioRecord.release();
        isRecording = false;
    }

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    public void startRecordingWave() {
        minBuffer = AudioRecord.getMinBufferSize(inSamplerate, AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_DEFAULT);

        addLog("Initializing audio recorder...");
        audioRecord = new AudioRecord(
                MediaRecorder.AudioSource.MIC, inSamplerate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT, minBuffer);

        // Start audio recording
        addLog("Creating output file stream");
        try {
            outputStream = new FileOutputStream(pcmFilePath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);
        DataOutputStream dataOutputStream = new DataOutputStream(bufferedOutputStream);
        addLog("started audio recording");
        updateStatus("Recording...");
        audioRecord.startRecording();

        short[] buffer = new short[minBuffer]; // Use minBuffer for reading
        int bytesRead;
        while (isRecording) {
            addLog("reading to short array buffer, buffer sze- " + minBuffer);
            bytesRead = audioRecord.read(buffer, 0, buffer.length);
            addLog("bytes read=" + bytesRead);
            if (bytesRead > 0) {
                addLog("encoding bytes to mp3 buffer..");
                try {
                    addLog("writing mp3 buffer to outputstream with " + bytesRead + " bytes");
                    for (int i = 0; i < bytesRead; i++) {
                        // Ensure we write raw PCM data correctly
                        dataOutputStream.writeShort(buffer[i]);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    break; // Exit if there's an error while writing
                }
            }
        }

        addLog("stopped recording");
        updateStatus("Recording stopped");

        addLog("flushing final mp3buffer");
        try {
            dataOutputStream.flush(); // Flush any remaining data in the buffer
            dataOutputStream.close();  // Close the data output stream
        } catch (IOException e) {
            e.printStackTrace();
        }
        addLog("releasing audio recorder");
        audioRecord.stop();
        audioRecord.release();
        isRecording = false;
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
}

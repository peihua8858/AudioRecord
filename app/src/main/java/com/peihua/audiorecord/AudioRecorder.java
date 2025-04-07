package com.peihua.audiorecord;

import android.Manifest;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import androidx.annotation.RequiresPermission;

import com.peihua.audiorecord.ui.theme.AudioRecordManager2;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class AudioRecorder {
    // 声道数。CHANNEL_IN_MONO and CHANNEL_IN_STEREO. 其中CHANNEL_IN_MONO是可以保证在所有设备能够使用的。
    public static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_STEREO;

    // 返回的音频数据的格式。 ENCODING_PCM_8BIT, ENCODING_PCM_16BIT, and ENCODING_PCM_FLOAT.
    public static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    public static final int SAMPLE_RATE_INHZ = 44100;
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

    /**
     * 开始录音&#xff0c;返回临时缓存文件&#xff08;.pcm&#xff09;的文件路径
     */
    public String startRecordAudio(File pcmFile) {
        String audioCacheFilePath = pcmFile.getAbsolutePath();
        try {
            // 获取最小录音缓存大小&#xff0c;
            int minBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE_INHZ, CHANNEL_CONFIG, AUDIO_FORMAT);
            this.audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE_INHZ, CHANNEL_CONFIG, AUDIO_FORMAT, minBufferSize);


            // 开始录音
            this.isRecording = true;
            audioRecord.startRecording();

            // 创建数据流&#xff0c;将缓存导入数据流
            File file = new File(audioCacheFilePath);
            Logcat.i( "audio cache pcm file path:" + audioCacheFilePath);

            /*
             *  以防万一&#xff0c;看一下这个文件是不是存在&#xff0c;如果存在的话&#xff0c;先删除掉
             */
            if (file.exists()) {
                file.delete();
            }

            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Logcat.e( "临时缓存文件未找到");
            }
            if (fos == null) {
                return null;
            }

            byte[] data = new byte[minBufferSize];
            int read;
            if (fos != null) {
                while (isRecording) {
                    read = audioRecord.read(data, 0, minBufferSize);
                    if (AudioRecord.ERROR_INVALID_OPERATION != read) {
                        try {
                            fos.write(data);
                            Logcat.i("audioRecordTest", "写录音数据-&gt;" + read);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            try {
                // 关闭数据流
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IllegalStateException e) {
            Logcat.w( "需要获取录音权限");
        } catch (SecurityException e) {
            Logcat.w( "需要获取录音权限");
        }
        return audioCacheFilePath;
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

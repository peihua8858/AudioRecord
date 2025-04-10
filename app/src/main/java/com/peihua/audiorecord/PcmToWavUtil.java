package com.peihua.audiorecord;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaPlayer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by lzt
 * time 2021/6/9 15:42
 * <p>
 *
 * @author lizhengting
 * 描述&#xff1a;pcm格式的音频转换为wav格式的工具类
 */
public class PcmToWavUtil {
    private int mBufferSize; //缓存的音频大小
    private int mSampleRate = 44100;// 此处的值必须与录音时的采样率一致
    private int mChannel = AudioFormat.CHANNEL_IN_STEREO; //立体声
    private int mEncoding = AudioFormat.ENCODING_PCM_16BIT;

    private static class SingleHolder {
        static PcmToWavUtil mInstance = new PcmToWavUtil();
    }

    public static PcmToWavUtil getInstance() {
        return SingleHolder.mInstance;
    }


    public PcmToWavUtil() {
        this.mBufferSize = AudioRecord.getMinBufferSize(mSampleRate, mChannel, mEncoding);
    }

    /**
     * @param sampleRate sample rate、采样率
     * @param channel    channel、声道
     * @param encoding   Audio data format、音频格式
     */
    public PcmToWavUtil(int sampleRate, int channel, int encoding) {
        this.mSampleRate = sampleRate;
        this.mChannel = channel;
        this.mEncoding = encoding;
        this.mBufferSize = AudioRecord.getMinBufferSize(mSampleRate, mChannel, mEncoding);
    }


    /**
     * pcm文件转wav文件
     * <p>
     *
     * @param inFilename  源文件路径
     * @param outFilename 目标文件路径
     * @param deleteOrg   是否删除源文件
     */
    public void pcmToWav(String inFilename, String outFilename, boolean deleteOrg) {
        long longSampleRate = mSampleRate;
        int channels = 2;
        long byteRate = 16 * mSampleRate * channels / 8;
        pcmToWav(inFilename, outFilename, deleteOrg, channels, byteRate, longSampleRate);
    }

    public void pcmToWav(String inFilename, String outFilename,
                         int channels,
                         long byteRate,
                         long sampleRate) {
        pcmToWav(inFilename, outFilename, false, channels, byteRate, sampleRate);
    }

    public void pcmToWav(String inFilename, String outFilename,
                         boolean deleteOrg,
                         int channels,
                         long byteRate,
                         long sampleRate) {
        FileInputStream in;
        FileOutputStream out;
        long totalAudioLen;
        long totalDataLen;
        byte[] data = new byte[mBufferSize];
        try {
            in = new FileInputStream(inFilename);
            out = new FileOutputStream(outFilename);
            totalAudioLen = in.getChannel().size();
            totalDataLen = totalAudioLen + 36;

            writeWaveFileHeader(out, totalAudioLen, totalDataLen,
                    sampleRate, channels, byteRate);
            while (in.read(data) != -1) {
                out.write(data);
            }
            in.close();
            out.close();
            if (deleteOrg) {
                new File(inFilename).delete();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void pcmToWav(String inFilename, String outFilename) {
        pcmToWav(inFilename, outFilename, false);
    }

    /**
     * 加入wav文件头
     */
    private void writeWaveFileHeader(FileOutputStream out, long totalAudioLen,
                                     long totalDataLen, long longSampleRate, int channels, long byteRate)
            throws IOException {
        byte[] header = new byte[44];
        header[0] = 'R'; // RIFF/WAVE header
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W'; //WAVE
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        header[12] = 'f'; // 'fmt ' chunk
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = 16; // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1; // format = 1
        header[21] = 0;
        header[22] = (byte) channels;
        header[23] = 0;
        header[24] = (byte) (longSampleRate & 0xff);
        header[25] = (byte) ((longSampleRate >> 8) & 0xff);
        header[26] = (byte) ((longSampleRate >> 16) & 0xff);
        header[27] = (byte) ((longSampleRate >> 24) & 0xff);
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        header[32] = (byte) (2 * 16 / 8); // block align
        header[33] = 0;
        header[34] = 16; // bits per sample
        header[35] = 0;
        header[36] = 'd'; //data
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);
        out.write(header, 0, 44);
    }

    /**
     * 播放一个wav文件
     */
    protected void playWav(String filePath) {
        File wavFile = new File(filePath);
        try {
            FileInputStream fis = new FileInputStream(wavFile);
            byte[] buffer = new byte[1024 * 1024 * 2];//2M
            int len = fis.read(buffer);
            Logcat.i("fis len=" + len);
            Logcat.i("0:" + (char) buffer[0]);
            int pcmlen = 0;
            pcmlen += buffer[0x2b];
            pcmlen = pcmlen * 256 + buffer[0x2a];
            pcmlen = pcmlen * 256 + buffer[0x29];
            pcmlen = pcmlen * 256 + buffer[0x28];

            int channel = buffer[0x17];
            channel = channel * 256 + buffer[0x16];

            int bits = buffer[0x23];
            bits = bits * 256 + buffer[0x22];
            Logcat.i("pcmlen=" + pcmlen + ",channel=" + channel + ",bits=" + bits);
            AudioTrack at = new AudioTrack(AudioManager.STREAM_MUSIC,
                    mSampleRate * 2,
                    channel,
                    AudioFormat.ENCODING_PCM_16BIT,
                    pcmlen,
                    AudioTrack.MODE_STATIC);
            at.write(buffer, 0x2C, pcmlen);

            at.play();

        } catch (Exception e) {

        } finally {
            wavFile = null;
        }
    }

    /**
     * 使用MediaPlayer播放文件&#xff0c;并且指定一个当播放完成后会触发的监听器
     *
     * @param filePath
     * @param onCompletionListener
     */
    protected void playWavWithMediaPlayer(String filePath, MediaPlayer.OnCompletionListener onCompletionListener) {
        //File wavFile = new File(filePath);
        try {
            MediaPlayer mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(filePath);
            mediaPlayer.setOnCompletionListener(onCompletionListener);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (Exception e) {

        }
    }
}

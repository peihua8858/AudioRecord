//package com.peihua.audiorecord
//
//import android.annotation.TargetApi
//import android.media.MediaCodec
//import android.media.MediaExtractor
//import android.media.MediaFormat
//import android.os.Build
//import android.util.Log
//import java.io.File
//import java.io.FileNotFoundException
//import java.io.FileOutputStream
//import java.io.IOException
//
///**
// * Created by lrannn on 2018/3/2.
// *
// * @e-mail lran7master@gmail.com
// */
//class MediaDecoder {
//    private var mMediaExtractor: MediaExtractor? = null
//
//    private var mSourcePath: String? = null
//    private var mRawFile: String? = null
//
//    private var mMediaInfo: MediaInfo? = null
//    private var mTrackFormat: MediaFormat? = null
//    private var outputStream: FileOutputStream? = null
//
//    private var mListener: OnReadRawDataListener? = null
//
//    fun setDataSrcPath(src: String) {
//        mSourcePath = src
//    }
//
//    fun setDataDestPath(destPath: String) {
//        this.mRawFile = destPath
//        initialRawFile()
//    }
//
//    fun setOnReadRawDataListener(listener: OnReadRawDataListener) {
//        this.mListener = listener
//    }
//
//    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
//    fun start() {
//        mMediaExtractor = MediaExtractor()
//        try {
//            mMediaExtractor!!.setDataSource(mSourcePath!!)
//            mMediaInfo = parseMediaFormat(mMediaExtractor!!)
//
//            Thread(mDecodingRunnable).start()
//        } catch (e: IOException) {
//            e.printStackTrace()
//        }
//    }
//
//    private fun initialRawFile() {
//        val mOutFile = File(mRawFile)
//        try {
//            if (!mOutFile.exists()) {
//                mOutFile.createNewFile()
//            }
//            outputStream = FileOutputStream(mOutFile)
//        } catch (e: FileNotFoundException) {
//            e.printStackTrace()
//        } catch (e: IOException) {
//            e.printStackTrace()
//        }
//    }
//
//    private fun parseMediaFormat(extractor: MediaExtractor): MediaInfo {
//        mTrackFormat = extractor.getTrackFormat(0)
//
//        val info = MediaInfo()
//
//        if (mTrackFormat!!.containsKey(MediaFormat.KEY_MIME)) {
//            info.mime = mTrackFormat!!.getString(MediaFormat.KEY_MIME)
//        }
//        if (mTrackFormat!!.containsKey(MediaFormat.KEY_SAMPLE_RATE)) {
//            info.sampleRate = mTrackFormat!!.getInteger(MediaFormat.KEY_SAMPLE_RATE)
//        }
//        if (mTrackFormat!!.containsKey(MediaFormat.KEY_CHANNEL_COUNT)) {
//            info.channels = mTrackFormat!!.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
//        }
//        if (mTrackFormat!!.containsKey(MediaFormat.KEY_DURATION)) {
//            info.duration = mTrackFormat!!.getLong(MediaFormat.KEY_DURATION)
//        }
//        if (mTrackFormat!!.containsKey(MediaFormat.KEY_BIT_RATE)) {
//            info.bitRate = mTrackFormat!!.getInteger(MediaFormat.KEY_BIT_RATE)
//        }
//        return info
//    }
//
//    private val mDecodingRunnable: Runnable = object : Runnable {
//        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
//        override fun run() {
//            try {
//                mMediaExtractor!!.selectTrack(0)
//                val codec = MediaCodec.createDecoderByType(mMediaInfo!!.mime!!)
//                codec.configure(mTrackFormat, null, null, 0)
//                codec.start()
//
//                val inBuffers = codec.getInputBuffers()
//                val outBuffers = codec.getOutputBuffers()
//
//                val info = MediaCodec.BufferInfo()
//                var eos = false
//                while (true) {
//                    if (!eos) {
//                        val inBufferIndex = codec.dequeueInputBuffer(TIMEOUT_US.toLong())
//                        if (inBufferIndex >= 0) {
//                            val buffer = inBuffers[inBufferIndex]
//                            buffer.clear()
//                            val readSampleData = mMediaExtractor!!.readSampleData(buffer, 0)
//                            if (readSampleData < 0) {
//                                eos = true
//                                codec.queueInputBuffer(
//                                    inBufferIndex, 0, 0, 0,
//                                    MediaCodec.BUFFER_FLAG_END_OF_STREAM
//                                )
//                            } else {
//                                codec.queueInputBuffer(
//                                    inBufferIndex, 0, readSampleData,
//                                    mMediaExtractor!!.getSampleTime(), 0
//                                )
//                                mMediaExtractor!!.advance()
//                            }
//                        }
//                    }
//
//                    val res = codec.dequeueOutputBuffer(info, TIMEOUT_US.toLong())
//                    if (res >= 0) {
//                        val outBuffer = outBuffers[res]
//                        val chunk = ByteArray(info.size)
//                        outBuffer.get(chunk)
//                        outBuffer.clear()
//                        if (mListener != null) {
//                            mListener!!.onRawData(chunk)
//                        }
//                        if (outputStream != null) {
//                            writePCMData(chunk)
//                        }
//                        codec.releaseOutputBuffer(res, false)
//                    } else if (res == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
//                        val outputFormat = codec.getOutputFormat()
//                        Log.d(TAG, "run: OutputFormat has change to " + outputFormat)
//                    } else if (res == MediaCodec.INFO_TRY_AGAIN_LATER) {
//                        Log.d(TAG, "run: Info try again later")
//                    }
//
//                    if ((info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
//                        break
//                    }
//                }
//
//                codec.stop()
//                codec.release()
//
//                mMediaExtractor!!.release()
//                mMediaExtractor = null
//            } catch (e: IOException) {
//                e.printStackTrace()
//            }
//        }
//    }
//
//    private fun writePCMData(data: ByteArray?) {
//        try {
//            outputStream!!.write(data)
//        } catch (e: IOException) {
//            e.printStackTrace()
//        }
//    }
//
//
//    internal class MediaInfo {
//        var mime: String? = null
//        var sampleRate: Int = 0
//        var channels: Int = 0
//        var bitRate: Int = 0
//        var duration: Long = 0
//    }
//
//     interface OnReadRawDataListener {
//        fun onRawData(data: ByteArray?)
//    }
//
//    companion object {
//        private const val TAG = "MediaDecoder"
//        private const val TIMEOUT_US = 1000
//    }
//}

package com.knox.kavrecorder.recorder;

import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.projection.MediaProjection;
import android.util.Log;
import android.view.Surface;

import com.knox.kavrecorder.net.StreamWrapper;
import com.knox.kavrecorder.utils.KMd5;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

import static android.media.MediaFormat.KEY_FRAME_RATE;
import static android.media.MediaFormat.KEY_HEIGHT;
import static android.media.MediaFormat.KEY_WIDTH;

/**
 * @author Knox.Tsang
 * @time 2017/9/19  14:08
 * @desc ${TODD}
 */


public class VRecorder extends Thread {
    private static final String TAG = "VRecorder";

    private static final String THREAD_NAME = "VR";
    private int mWidth;
    private int mHeight;
    private int mBitRate;
    private int mDpi;
    private MediaProjection mMediaProjection;
    // parameters for the encoder
    private static final String MIME_TYPE = "video/avc"; // H.264 Advanced Video Coding
    private static final int FRAME_RATE = 30; // 30 fps
    private static final int IFRAME_INTERVAL = 10; // 10 seconds between I-frames
    private static final int TIMEOUT_US = 10000;

    private MediaCodec mEncoder;
    private Surface mSurface;
    private AtomicBoolean mQuit = new AtomicBoolean(false);
    private MediaCodec.BufferInfo mBufferInfo = new MediaCodec.BufferInfo();
    private VirtualDisplay mVirtualDisplay;
    private StreamWrapper mStreamWrapper;
    private MediaFormat mNewFormat;

    private int noFrameCnt = 0;

    public VRecorder(int width, int height, int bitrate, int dpi, MediaProjection mp, StreamWrapper streamWrapper) {
        super(THREAD_NAME);
        mWidth = width;
        mHeight = height;
        mBitRate = bitrate;
        mDpi = dpi;
        mMediaProjection = mp;
        mStreamWrapper = streamWrapper;
    }

    /**
     * stop task
     */
    public final void quit() {
        mQuit.set(true);
    }

    @Override
    public void run() {
        try {
            try {
                prepareEncoder();
            } catch (IOException e) {
                Log.e(TAG, "prepareEncoder err: IOException");
                throw new RuntimeException(e);
            }
            /*
            * mediaCodec拿出来的surface给这里用, 所以就是
            * MediaProjection, VirtualDisplay, MediaCodec, Surface联合使用
            */
            mVirtualDisplay = mMediaProjection.createVirtualDisplay(TAG + "-display",
                    mWidth, mHeight, mDpi, DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC,
                    mSurface, null, null);
            Log.e(TAG, "created virtual display: " + mVirtualDisplay);
            recordVirtualDisplay();
        } finally {
            release();
        }
    }


    private void recordVirtualDisplay() {
        while (!mQuit.get()) {
            int index = mEncoder.dequeueOutputBuffer(mBufferInfo, TIMEOUT_US);
            Log.e(TAG, "dequeue output buffer index=" + index + ", noFrameCnt " + noFrameCnt);
            if (index == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                recordOutputFormat();
                noFrameCnt = 0;

            } else if (index == MediaCodec.INFO_TRY_AGAIN_LATER) {
                Log.e(TAG, "retrieving buffers time out!");
                noFrameCnt++;
                try {
                    // wait 10ms
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    Log.e(TAG, Thread.currentThread().getName() + "sleep 10ms: InterruptedException");
                }
            } else if (index >= 0) {
                noFrameCnt = 0;
                encodeToVideoTrack(index);

                mEncoder.releaseOutputBuffer(index, false);
            }
        }
    }

    private void encodeToVideoTrack(int index) {
        ByteBuffer encodedData = mEncoder.getOutputBuffer(index);
        byte[] array = new byte[mBufferInfo.size];
        if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
            // The codec config data was pulled out and fed to the muxer when we got
            // the INFO_OUTPUT_FORMAT_CHANGED status.
            // Ignore it.
            try {
                encodedData.position(mBufferInfo.offset);
                encodedData.limit(mBufferInfo.offset + mBufferInfo.size);
                encodedData.get(array);
                Log.e(TAG, "BUFFER_FLAG_CODEC_CONFIG: " + KMd5.bytes2Hex0(array));
                mStreamWrapper.write264Es(array, -1, mNewFormat.getInteger(KEY_WIDTH),
                        mNewFormat.getInteger(KEY_HEIGHT), mNewFormat.getInteger(KEY_FRAME_RATE));
            } catch (Exception e) {
                e.printStackTrace();
            }
            Log.e(TAG, "ignoring BUFFER_FLAG_CODEC_CONFIG");
            mBufferInfo.size = 0;
        }
        if (mBufferInfo.size == 0) {
            Log.e(TAG, "info.size == 0, drop it.");
            encodedData = null;
        } else {
            Log.e(TAG, "got buffer, info: size=" + mBufferInfo.size
                    + ", presentationTimeUs=" + mBufferInfo.presentationTimeUs
                    + ", offset=" + mBufferInfo.offset);
        }
        if (encodedData != null) {
            encodedData.position(mBufferInfo.offset);
            encodedData.limit(mBufferInfo.offset + mBufferInfo.size);
            encodedData.get(array);
            mStreamWrapper.write264Es(array, mBufferInfo.presentationTimeUs, mNewFormat.getInteger(KEY_WIDTH),
                    mNewFormat.getInteger(KEY_HEIGHT), mNewFormat.getInteger(KEY_FRAME_RATE));
            Log.e(TAG, "sent " + mBufferInfo.size + " bytes to muxer..." + encodedData);
        }
    }

    private void recordOutputFormat() {
        // should happen before receiving buffers, and should only happen once
        mNewFormat = mEncoder.getOutputFormat();
        Log.e(TAG, "output format changed: " + mNewFormat.toString());
    }

    private void prepareEncoder() throws IOException {
        MediaFormat format = MediaFormat.createVideoFormat(MIME_TYPE, mWidth, mHeight);
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        format.setInteger(MediaFormat.KEY_BIT_RATE, mBitRate);
        format.setInteger(KEY_FRAME_RATE, FRAME_RATE);
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, IFRAME_INTERVAL);

        Log.e(TAG, "video format: " + format);
        mEncoder = MediaCodec.createEncoderByType(MIME_TYPE);
        mEncoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        mSurface = mEncoder.createInputSurface();
        Log.e(TAG, "input surface: " + mSurface);
        mEncoder.start();
    }

    private void release() {
        Log.e(TAG, "release: ");
        if (mEncoder != null) {
            mEncoder.stop();
            mEncoder.release();
            mEncoder = null;
        }
        if (mVirtualDisplay != null) {
            mVirtualDisplay.release();
        }
        if (mMediaProjection != null) {
            mMediaProjection.stop();
        }
    }
}

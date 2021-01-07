/*
 * Copyright (c) 2014 Yrom Wang <http://www.yrom.net>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jx.jzrecord.recording;

import android.hardware.display.VirtualDisplay;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Surface;


import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;

import static android.media.MediaFormat.MIMETYPE_AUDIO_AAC;
import static android.media.MediaFormat.MIMETYPE_VIDEO_AVC;

/**
 * @author Yrom
 */
public class ScreenRecorder  {
    private static final String TAG = "TAGScreenRecorder";
    private static final boolean VERBOSE = true;
    private static final int INVALID_INDEX = -1;
    static final String VIDEO_AVC = MIMETYPE_VIDEO_AVC; // H.264 Advanced Video Coding
    static final String AUDIO_AAC = MIMETYPE_AUDIO_AAC; // H.264 Advanced Audio Coding
    private String mDstPath;  //保存视频路径
    private VideoEncoder mVideoEncoder;  //视频编码对象
    private MicRecorder mAudioEncoder;   //音频编码对象

    private MediaFormat mVideoOutputFormat = null, mAudioOutputFormat = null;
    private int mVideoTrackIndex = INVALID_INDEX, mAudioTrackIndex = INVALID_INDEX;
    private MediaMuxer mMuxer;  //合成视频对象
    private boolean mMuxerStarted = false;

    //AtomicBoolean：在这个Boolean值的变化的时候不允许其他线程在操作它，保持操作的原子性
    private AtomicBoolean mForceQuit = new AtomicBoolean(false); //是否退出
    private AtomicBoolean mIsRunning = new AtomicBoolean(false);  //是否在录屏
    private AtomicBoolean mIsPause = new AtomicBoolean(false);  //是否在暂停
    private long pauseVideoPtsOffset, pauseAudioPtsOffset;//记录视音频的开始暂停时的时间戳

    private VirtualDisplay mVirtualDisplay;  //录屏内容，拿去编码最后在MediaCodec的输出缓冲区中拿到编码后的ByteBuffer即可

    private HandlerThread mWorker;
    private CallbackHandler mHandler;

    private Callback mCallback;
    private LinkedList<Integer> mPendingVideoEncoderBufferIndices = new LinkedList<>();
    private LinkedList<Integer> mPendingAudioEncoderBufferIndices = new LinkedList<>();
    private LinkedList<MediaCodec.BufferInfo> mPendingAudioEncoderBufferInfos = new LinkedList<>();
    private LinkedList<MediaCodec.BufferInfo> mPendingVideoEncoderBufferInfos = new LinkedList<>();


    /**
     * @param display for {@link VirtualDisplay#setSurface(Surface)}
     * @param dstPath saving path
     */
    public ScreenRecorder(VideoEncodeConfig video,
                          AudioEncodeConfig audio,
                          VirtualDisplay display,
                          String dstPath) {
        mVirtualDisplay = display;
        mDstPath = dstPath;
        mVideoEncoder = new VideoEncoder(video);
        mAudioEncoder = audio == null ? null : new MicRecorder(audio);
        pauseVideoPtsOffset=0;
        pauseAudioPtsOffset=0;
    }

    /**
     * stop task
     */
    public final void quit() {
        mForceQuit.set(true);
        if (!mIsRunning.get()) {
            //释放录屏所用到的所有对象
            release();
        } else {
            //结束，false表示还会写入结束帧在退出
            signalStop(false);
        }

    }

    public void start() {
        Log.w("TAGScreenRecoder","start");
        if (mWorker != null) throw new IllegalStateException();
        //步骤1：创建HandlerThread实例对象 * 传入参数 = 线程名字，作用 = 标记该线程
        mWorker = new HandlerThread(TAG);
       //步骤2：启动线程
        mWorker.start();
        //步骤3：创建工作线程Handler & 复写handleMessage（）
         //作用：关联HandlerThread的Looper对象、实现消息处理操作 & 与其他线程进行通信
        //消息处理操作（HandlerMessage（））的执行线程 = mHandlerThread所创建的工作线程中执行
        mHandler = new CallbackHandler(mWorker.getLooper());
        //步骤4：使用工作线程Handler向工作线程的消息队列发送消息
        //     在工作线程中，当消息循环时取出对应消息 & 在工作线程执行相关操作
        mHandler.sendEmptyMessage(MSG_START);
    }

    public void pause() {
        Log.w(TAG,"pause");
        if (mIsRunning.get()) {
            mIsPause.set(true);
        }
    }

    public void screen_continue() {
        Log.w(TAG,"screen_continue");
        mIsPause.set(false);
    }

    public boolean getmIsPause(){
        return mIsPause.get();
    }

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

//    获取保存文件的路径
    public String getSavedPath() {
        return mDstPath;
    }


    interface Callback {
        void onStop(Throwable error);

    }

    private static final int MSG_START = 0;
    private static final int MSG_STOP = 1;
    private static final int MSG_ERROR = 2;
    private static final int STOP_WITH_EOS = 1;

    private class CallbackHandler extends Handler {
        CallbackHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_START:
                    try {
                        record();  //状态变为录屏状态
                        break;
                    } catch (Exception e) {
                        msg.obj = e;
                    }
                case MSG_STOP:
                case MSG_ERROR:
                    Log.w("TAG111111111","*"+msg.what);
                    stopEncoders();  //结束视音频编码器
                    if (msg.arg1 != STOP_WITH_EOS) signalEndOfStream();
                    if (mCallback != null) {
                        mCallback.onStop((Throwable) msg.obj);
                    }
                    release();
                    break;
            }
        }
    }

    private void signalEndOfStream() {
        MediaCodec.BufferInfo eos = new MediaCodec.BufferInfo();
        ByteBuffer buffer = ByteBuffer.allocate(0);
        eos.set(0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
        if (VERBOSE) Log.i(TAG, "Signal EOS to muxer ");
        if (mVideoTrackIndex != INVALID_INDEX) {
            writeSampleData(mVideoTrackIndex, eos, buffer);
        }
        if (mAudioTrackIndex != INVALID_INDEX) {
            writeSampleData(mAudioTrackIndex, eos, buffer);
        }
        mVideoTrackIndex = INVALID_INDEX;
        mAudioTrackIndex = INVALID_INDEX;
    }

    private void record() {
        if (mIsRunning.get() || mForceQuit.get()) {
            //状态为没录制，没退出则不会执行该方法
            throw new IllegalStateException();
        }
        if (mVirtualDisplay == null) {
            throw new IllegalStateException("maybe release");
        }
        mIsRunning.set(true);

        try {
            // create muxer  参数1：输出的路径  参数2：输出的格式
            mMuxer = new MediaMuxer(mDstPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            // create encoder and input surface
            prepareVideoEncoder();  //视频编码准备
            prepareAudioEncoder();  //音频编码准备
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // "turn on" VirtualDisplay after VideoEncoder prepared
        mVirtualDisplay.setSurface(mVideoEncoder.getInputSurface()); ////提供一个虚拟的”盒子“给mediaProjection装录屏的原始数据
        if (VERBOSE) Log.d(TAG, "set surface to display: " + mVirtualDisplay.getDisplay());
    }

    private void muxVideo(int index, MediaCodec.BufferInfo buffer) {
        if (!mIsRunning.get()) {
            Log.w(TAG, "muxVideo: Already stopped!");
            return;
        }
        //未开始合成，记录缓冲区的索引和数据
        if (!mMuxerStarted || mVideoTrackIndex == INVALID_INDEX) {
            mPendingVideoEncoderBufferIndices.add(index);
            mPendingVideoEncoderBufferInfos.add(buffer);
            return;
        }
        Log.w("muxVideo", "muxVideo里的index="+index);
        ByteBuffer encodedData = mVideoEncoder.getOutputBuffer(index);//获取输出缓冲区数据
        //生成一帧
        writeSampleData(mVideoTrackIndex, buffer, encodedData);
        ///处理结束，释放输出缓存区资源
        mVideoEncoder.releaseOutputBuffer(index);
        if ((buffer.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
            if (VERBOSE)
                Log.w(TAG, "Stop encoder and muxer, since the buffer has been marked with EOS");
            // send release msg
            mVideoTrackIndex = INVALID_INDEX;
            //发送释放消息
            signalStop(true);
        }
    }


    private void muxAudio(int index, MediaCodec.BufferInfo buffer) {
        if (!mIsRunning.get()) {
            Log.w(TAG, "muxAudio: Already stopped!");
            return;
        }
        if (!mMuxerStarted || mAudioTrackIndex == INVALID_INDEX) {
            mPendingAudioEncoderBufferIndices.add(index);
            mPendingAudioEncoderBufferInfos.add(buffer);
            return;

        }
        ByteBuffer encodedData = mAudioEncoder.getOutputBuffer(index);  //解码之后的数据，完整帧（不是I帧，P帧，B帧，而是一帧图片）
        writeSampleData(mAudioTrackIndex, buffer, encodedData);
        mAudioEncoder.releaseOutputBuffer(index);  //释放缓冲区索引
        if ((buffer.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
            if (VERBOSE)
                Log.d(TAG, "Stop encoder and muxer, since the buffer has been marked with EOS");
            mAudioTrackIndex = INVALID_INDEX;
            signalStop(true);
        }
    }
    //track：判断视频/音频
    private void writeSampleData(int track, MediaCodec.BufferInfo buffer, ByteBuffer encodedData) {
        if ((buffer.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
            // The codec config data was pulled out and fed to the muxer when we got
            // the INFO_OUTPUT_FORMAT_CHANGED status.
            // 编解码器特定的数据，而不是媒体数据。不用转换成MP4格式
            if (VERBOSE) Log.w(TAG, "Ignoring BUFFER_FLAG_CODEC_CONFIG");
            buffer.size = 0;
        }
        boolean eos = (buffer.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0;  //数据流末尾

        //暂停
        if(mIsPause.get()){
            Log.w(TAG,"-----------------------------------暂停时间-----------------------------------------------------------");
            if(track == mVideoTrackIndex&&pauseVideoPtsOffset==0){
                //视频
                pauseVideoPtsOffset=buffer.presentationTimeUs;
            }else if(track == mAudioTrackIndex&&pauseAudioPtsOffset==0){
                //音频
                pauseAudioPtsOffset=buffer.presentationTimeUs;
            }
            buffer.size = 0;
        }

        if ((buffer.size == 0 && !eos)) {
            if (VERBOSE) Log.w(TAG, "info.size == 0, drop it.");
            encodedData = null;
        } else {
            //presentationTimeUs：long类型: 这个buffer提交呈现的时间戳（微妙），它通常是指这个buffer应该提交或渲染的media time。当使用output surface时，将作为timestamp传递给frame（转换为纳秒后）。
            if (buffer.presentationTimeUs != 0) { // maybe 0 if eos
                Log.w(TAG, "writeSampleData里面的track="+track+"--mVideoTrackIndex="+mVideoTrackIndex);
                Log.w(TAG, "writeSampleData里面的track="+track+"--mAudioTrackIndex="+mAudioTrackIndex);
                    if (track == mVideoTrackIndex) {
                        resetVideoPts(buffer);
                    } else if (track == mAudioTrackIndex) {
                        resetAudioPts(buffer);
                }
            }
            if (VERBOSE)
                Log.w(TAG, "writeSampleData"+"[" + Thread.currentThread().getId() + "] Got buffer, track=" + track
                        + ", info: size=" + buffer.size+", info:offset="+buffer.offset
                        + ", presentationTimeUs=" + buffer.presentationTimeUs);
        }
        if (encodedData != null&&buffer.size!=0) {
            encodedData.position(buffer.offset);  ////编码数据的偏移（开始位置）
            encodedData.limit(buffer.offset + buffer.size);  ////解码数据的长度（结束位置）
            /**-----------------------一帧-------------**/
                mMuxer.writeSampleData(track, encodedData, buffer);
            if (VERBOSE)
                Log.w(TAG, "Sent " + buffer.size + " bytes to MediaMuxer on track ");
            Log.w(TAG, "------------------------下一个循环----------------------");
        }
    }

    private long mVideoPtsOffset, mAudioPtsOffset;

    //跟时间一样，选取一个作为基准时间，然后每次减去上一个
    private void resetAudioPts(MediaCodec.BufferInfo buffer) {
        //音频暂停后重置起始音频时间戳
        if(pauseAudioPtsOffset!=0){
            mAudioPtsOffset=mAudioPtsOffset+(buffer.presentationTimeUs-pauseAudioPtsOffset)+100; //暂停后偷偷使时间戳快了0.01秒
            pauseAudioPtsOffset=0;
            Log.w(TAG,"resetAudioPts里面的pauseAudioPtsOffset="+pauseAudioPtsOffset);
        }
        Log.w(TAG,"resetAudioPtsmAudioPtsOffset="+mAudioPtsOffset);
        Log.w(TAG,"resetAudioPtspresentationTimeUs="+buffer.presentationTimeUs);
        if (mAudioPtsOffset == 0) {
            mAudioPtsOffset = buffer.presentationTimeUs;
            buffer.presentationTimeUs = 0;
        } else {
                buffer.presentationTimeUs -= mAudioPtsOffset;
        }
    }

    private void resetVideoPts(MediaCodec.BufferInfo buffer) {
        //视频暂停后重置起始视频时间戳
        if(pauseVideoPtsOffset!=0){
            mVideoPtsOffset=mVideoPtsOffset+(buffer.presentationTimeUs-pauseVideoPtsOffset)+100;
            pauseVideoPtsOffset=0;
            Log.w(TAG,"resetVideoPts里面的pauseVideoPtsOffset="+pauseVideoPtsOffset);
        }

        Log.w(TAG,"mVideoPtsOffset="+mVideoPtsOffset);
        Log.w(TAG,"presentationTimeUs="+buffer.presentationTimeUs);
        if (mVideoPtsOffset == 0) {
            mVideoPtsOffset = buffer.presentationTimeUs;
            buffer.presentationTimeUs = 0;//第一帧设置基准时间戳
        } else {
            buffer.presentationTimeUs -= mVideoPtsOffset;//其他帧与第一帧的差值
        }
    }

    private void resetVideoOutputFormat(MediaFormat newFormat) {
        // should happen before receiving buffers, and should only happen once
        if (mVideoTrackIndex >= 0 || mMuxerStarted) {
            throw new IllegalStateException("output format already changed!");
        }
        if (VERBOSE)
            Log.i(TAG, "Video output format changed.\n New format: " + newFormat.toString());
        mVideoOutputFormat = newFormat;
    }

    private void resetAudioOutputFormat(MediaFormat newFormat) {
        // should happen before receiving buffers, and should only happen once
        if (mAudioTrackIndex >= 0 || mMuxerStarted) {
            throw new IllegalStateException("output format already changed!");
        }
        if (VERBOSE)
            Log.w(TAG, "Audio output format changed.\n New format: " + newFormat.toString());
        mAudioOutputFormat = newFormat;
    }

      /**---------------创建MediaMuxer， addTrack（）-------------------**/
    private void startMuxerIfReady() {
        Log.w(TAG, "startMuxerIfReady");
        if (mMuxerStarted || mVideoOutputFormat == null
                || (mAudioEncoder != null && mAudioOutputFormat == null)) {
            return;
        }


        mVideoTrackIndex = mMuxer.addTrack(mVideoOutputFormat); //添加视频轨道
        Log.w(TAG, "startMuxerIfReady里的mVideoOutputFormat="+mVideoTrackIndex);
        mAudioTrackIndex = mAudioEncoder == null ? INVALID_INDEX : mMuxer.addTrack(mAudioOutputFormat); //添加音频轨道
        mMuxer.start();//开始合成文件
        mMuxerStarted = true;
        if (VERBOSE) Log.w(TAG, "Started media muxer, mVideoOutputFormat=" + mVideoTrackIndex);
        if (mPendingVideoEncoderBufferIndices.isEmpty() && mPendingAudioEncoderBufferIndices.isEmpty()) {
            return;
        }
        if (VERBOSE) Log.w(TAG, "Mux pending video output buffers...");
        MediaCodec.BufferInfo info;
        while ((info = mPendingVideoEncoderBufferInfos.poll()) != null) {
            int index = mPendingVideoEncoderBufferIndices.poll();
            Log.w("muxVideo", "startMuxerIfReady里面的muxVideo的index="+index+"info="+info);
            muxVideo(index, info); //生成视频文件
        }
        if (mAudioEncoder != null) {
            while ((info = mPendingAudioEncoderBufferInfos.poll()) != null) {
                int index = mPendingAudioEncoderBufferIndices.poll();
                Log.w(TAG, "startMuxerIfReady里面的muxAudio的index="+index+"info="+info);
                muxAudio(index, info); // 生成音频文件
            }
        }
        if (VERBOSE) Log.w(TAG, "Mux pending video output buffers done.");
    }

    // @WorkerThread
    private void prepareVideoEncoder() throws IOException {
        Log.e(TAG, "prepareVideoEncoder");
        VideoEncoder.Callback callback = new VideoEncoder.Callback() {
            boolean ranIntoError = false;

            @Override
            public void onOutputBufferAvailable(BaseEncoder codec, int index, MediaCodec.BufferInfo info) {
                if (VERBOSE) Log.w(TAG, "VideoEncoder output buffer available: index=" + index);
                try {
                    Log.w(TAG, "prepareVideoEncoder里的muxVideo:info="+info.size);
                        muxVideo(index, info);   //生成MP4文件
                } catch (Exception e) {
                    Log.e(TAG, "Muxer encountered an error! ", e);
                    Message.obtain(mHandler, MSG_ERROR, e).sendToTarget();
                }
            }

            @Override
            public void onError(Encoder codec, Exception e) {
                ranIntoError = true;
                Log.e(TAG, "VideoEncoder ran into an error! ", e);
                Message.obtain(mHandler, MSG_ERROR, e).sendToTarget();
            }

            @Override
            public void onOutputFormatChanged(BaseEncoder codec, MediaFormat format) {
                Log.w(TAG, "onOutputFormatChanged= "+format);
                resetVideoOutputFormat(format);
                startMuxerIfReady();
            }
        };
        mVideoEncoder.setCallback(callback);
        mVideoEncoder.prepare();
    }

    private void prepareAudioEncoder() throws IOException {
        Log.e(TAG, "prepareAudioEncoder");
        final MicRecorder micRecorder = mAudioEncoder;
        if (micRecorder == null) return;
        AudioEncoder.Callback callback = new AudioEncoder.Callback() {
            boolean ranIntoError = false;

            @Override
            public void onOutputBufferAvailable(BaseEncoder codec, int index, MediaCodec.BufferInfo info) { //info每个缓冲区元数据包括一个偏移量和大小，用于指定关联的编解码器缓冲区中有效数据的范围。
                Log.w(TAG, "onOutputBufferAvailable");
                if (VERBOSE)
                    Log.w(TAG, "[" + Thread.currentThread().getId() + "] AudioEncoder output buffer available: index=" + index);
                 try {
                        muxAudio(index, info);
                } catch (Exception e) {
                    Log.e(TAG, "Muxer encountered an error! ", e);
                    Message.obtain(mHandler, MSG_ERROR, e).sendToTarget();
                }
            }

            @Override
            public void onOutputFormatChanged(BaseEncoder codec, MediaFormat format) {
                Log.w(TAG, "onOutputFormatChanged");
                if (VERBOSE)
                    Log.d(TAG, "[" + Thread.currentThread().getId() + "] AudioEncoder returned new format " + format);
                resetAudioOutputFormat(format);
                startMuxerIfReady();
            }

            @Override
            public void onError(Encoder codec, Exception e) {
                Log.e(TAG, "onError");
                ranIntoError = true;
                Log.e(TAG, "MicRecorder ran into an error! ", e);
                Message.obtain(mHandler, MSG_ERROR, e).sendToTarget();
            }


        };
        micRecorder.setCallback(callback);
        micRecorder.prepare();
    }

    private void signalStop(boolean stopWithEOS) {
        Log.e(TAG, "signalStop"+stopWithEOS);
        Message msg = Message.obtain(mHandler, MSG_STOP, stopWithEOS ? STOP_WITH_EOS : 0, 0);
        mHandler.sendMessageAtFrontOfQueue(msg);
    }

    private void stopEncoders() {
        Log.e(TAG, "stopEncoders");
        mIsRunning.set(false);
        mPendingAudioEncoderBufferInfos.clear();
        mPendingAudioEncoderBufferIndices.clear();
        mPendingVideoEncoderBufferInfos.clear();
        mPendingVideoEncoderBufferIndices.clear();
        // maybe called on an error has been occurred
        try {
            if (mVideoEncoder != null) mVideoEncoder.stop();
        } catch (IllegalStateException e) {
            // ignored
        }
        try {
            if (mAudioEncoder != null) mAudioEncoder.stop();
        } catch (IllegalStateException e) {
            // ignored
        }

    }

    private void release() {
        Log.e(TAG, "release");
        if (mVirtualDisplay != null) {
            mVirtualDisplay.setSurface(null);
            mVirtualDisplay = null;
        }

        mVideoOutputFormat = mAudioOutputFormat = null;
        mVideoTrackIndex = mAudioTrackIndex = INVALID_INDEX;
        mMuxerStarted = false;

        if (mWorker != null) {
            //线程安全结束
            mWorker.quitSafely();
            mWorker = null;
        }
        if (mVideoEncoder != null) {
            mVideoEncoder.release();
            mVideoEncoder = null;
        }
        if (mAudioEncoder != null) {
            mAudioEncoder.release();
            mAudioEncoder = null;
        }

        if (mMuxer != null) {
            try {
                mMuxer.stop();
                mMuxer.release();
            } catch (Exception e) {
                // ignored
            }
            mMuxer = null;
        }
        mHandler = null;  //回调线程
    }

    @Override
    protected void finalize() {
        Log.e(TAG, "finalize");
        if (mVirtualDisplay != null) {
            Log.e(TAG, "release() not called!");
            release();
        }
    }

}

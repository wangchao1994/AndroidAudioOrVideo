package com.raisesail.andoid.androidvideoaudio.resolve_mp4;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import com.raisesail.andoid.androidvideoaudio.R;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * 提取视频文件中的视频数据
 * 除去视频中的音频数据
 * 输出纯视频文件
 */
public class ResolveMp4 extends AppCompatActivity {

    private TextView mTestView;
    private static final String SDCARD_PATH = Environment.getExternalStorageDirectory().getPath();
    private static final String INPUT_FILEPATH = SDCARD_PATH + "/input.mp4";
    private static final String OUTPUT_FILEPATH = SDCARD_PATH + "/output.mp4";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resolve_mp4_kotlin);
        mTestView = findViewById(R.id.textview);
        File mOutputVideoFile = new File(OUTPUT_FILEPATH);
        if (mOutputVideoFile.exists()){
            mOutputVideoFile.delete();
            try {
                mOutputVideoFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                transCode(INPUT_FILEPATH,OUTPUT_FILEPATH);
            }
        }).start();
    }

    private void transCode(String inputFilePath, String outputFilePath) {
        MediaMuxer mMediaMuxer= null;//生成一个音频或视频文件；还可以把音频与视频混合成一个音视频文件
        int videoTrackIndex = -1;
        int mFrameRate = 0;//输出帧率
        //int audioTrackIndex = -1;
        //创建MediaExtractor
        //该类用于音视频混合数据的分离
        MediaExtractor mMediaExtractor = new MediaExtractor();
        try {
            mMediaExtractor.setDataSource(INPUT_FILEPATH);
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("resolve","data source is not found--------->"+e.getMessage());
        }
        //这里通过MediaExtractor.getTrackFormat获取媒体格式类
        //如果是自己手动创建的话
        //MediaFormat format = MediaFormat.createVideoFormat("video/avc",320,240);
        //必须设置csd0 csd1
        //对于H264视频的话，它对应的是sps和pps，对于AAC音频的话，对应的是ADTS，一般存在于编码器生成的IDR帧之中
        /**
         * byte[] csd0 = {x,x,x,x,x,x,x...}
         * byte[] csd1 = {x,x,x,x,x,x,x...}
         * format.setByteBuffer("csd-0",ByteBuffer.wrap(csd0));
         * format.setByteBuffer("csd-1",ByteBuffer.wrap(csd1));
         */
        for (int i = 0;i < mMediaExtractor.getTrackCount();i++) {
            //媒体格式类，用于描述媒体的格式参数，如视频帧率、音频采样率等。
            MediaFormat trackFormat = mMediaExtractor.getTrackFormat(i);
            //获取当前通道对应的文件格式
            String formatString = trackFormat.getString(MediaFormat.KEY_MIME);
            if (!formatString.startsWith("video/")) {
                continue;
            }
            //视频帧率
            //mFrameRate = trackFormat.getInteger(MediaFormat.KEY_FRAME_RATE);
            //选择视频轨道
            mMediaExtractor.selectTrack(i);
            //设置视频输出位置和格式
            try {
                mMediaMuxer = new MediaMuxer(OUTPUT_FILEPATH, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
                //记录视频轨道值
                videoTrackIndex = mMediaMuxer.addTrack(trackFormat);
                mMediaMuxer.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (mMediaMuxer == null){
            return;
        }
        //读取轨道信息时，以及向新文件写入轨道信息时。用于保存每一帧的信息(如时长，size,flag)
        MediaCodec.BufferInfo mBufferInfo = new MediaCodec.BufferInfo();
        //当前演示时长为0
        mBufferInfo.presentationTimeUs = 0;
        int sampleData = 0;
        //申请内存空间并进行写入文件
        ByteBuffer mByteBuffer = ByteBuffer.allocate(1024 * 1024 * 2);

        while (true){
            sampleData = mMediaExtractor.readSampleData(mByteBuffer, 0);
            if (sampleData < 0){
                break;
            }
            //保存帧信息
            mBufferInfo.size = sampleData;
            mBufferInfo.flags = mMediaExtractor.getSampleFlags();
            mBufferInfo.offset = 0;
            mBufferInfo.presentationTimeUs = mMediaExtractor.getSampleTime();
//            info.presentationTimeUs += 1000*1000 / framerate; // (1000*1000 / framerate) 是一帧的微秒时长
            boolean keyframe = (mBufferInfo.flags & MediaCodec.BUFFER_FLAG_SYNC_FRAME) > 0;
            Log.d("resolve_mp4","write sample " + keyframe + ", " + sampleData + ", " + mBufferInfo.presentationTimeUs);
            //保存帧数据，向指定轨道写入
            mMediaMuxer.writeSampleData(videoTrackIndex,mByteBuffer,mBufferInfo);
            mMediaExtractor.advance();//下一帧
        }
        mMediaExtractor.release();
        mMediaMuxer.stop();
        mMediaMuxer.release();
        Log.d("resolve_mp4","resolve mp4 complete!-------->");
    }
}

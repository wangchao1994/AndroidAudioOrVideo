package com.raisesail.andoid.surfaceviewcanvas.two;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.text.TextUtils;
import android.util.Log;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

/**
 * 实现录音
 */
public class AudioRecorder  {
    private static AudioRecorder mAudioRecorder;
    private int minBufferSize;//字节大小
    private AudioRecord mAudioRecord;//录音对象
    private String mFileName;//文件名称
    //音频输入-麦克风
    private final static int AUDIO_INPUT = MediaRecorder.AudioSource.MIC;
    //采用频率
    //44100是目前的标准，但是某些设备仍然支持22050，16000，11025
    //采样频率一般共分为22.05KHz、44.1KHz、48KHz三个等级
    private final static int AUDIO_SAMPLE_RATE = 16000;
    //声道 单声道
    private final static int AUDIO_CHANNEL = AudioFormat.CHANNEL_IN_MONO;
    //编码
    private final static int AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;

    //录音状态
    private Status status = Status.STATUS_NO_READY;
    //录音文件
    private List<String> mFileList = new ArrayList<>();
    private AudioTrack mAudioTrack;
    private DataInputStream dis;

    /**
     * 录音对象的状态
     */
    public enum Status {
        //未开始
        STATUS_NO_READY,
        //预备
        STATUS_READY,
        //录音
        STATUS_START,
        //暂停
        STATUS_PAUSE,
        //停止
        STATUS_STOP
    }
    /**
     * 获取AudioRecorder实例
     * @return
     */
    public static AudioRecorder getInstance(){
        if (mAudioRecorder == null){
            synchronized (AudioRecorder.class){
                if (null == mAudioRecorder){
                    mAudioRecorder = new AudioRecorder();
                }
            }
        }
        return mAudioRecorder;
    }

    /**
     * 创建录音对象
     * @param fileName
     * @param audidoSource
     * @param sampleRateInHz
     * @param channelConfig
     * @param audioFormat
     */
    public void createAudio(String fileName,int audidoSource,int sampleRateInHz,int channelConfig,int audioFormat){
        mFileName = fileName;
        //获取缓冲区字节大小
        minBufferSize = AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat);
        mAudioRecord = new AudioRecord(audidoSource,sampleRateInHz,channelConfig,audioFormat,minBufferSize);
    }

    /**
     * 创建默认的录音对象
     * @param fileName
     */
    public void createDefaultAudio(String fileName){
        mFileName = fileName;
        minBufferSize = AudioRecord.getMinBufferSize(AUDIO_SAMPLE_RATE,AUDIO_CHANNEL,AUDIO_ENCODING);
        mAudioRecord = new AudioRecord(AUDIO_INPUT,AUDIO_SAMPLE_RATE,AUDIO_CHANNEL,AUDIO_ENCODING,minBufferSize);
        status = Status.STATUS_READY;
    }

    /**
     * 开始录音
     * @param listener
     */
   public void startRecord(final RecordStreamListener listener){
       if (status == Status.STATUS_NO_READY || TextUtils.isEmpty(mFileName)){
           throw new IllegalStateException("录音对象为初始化,请检查当前录音权限是否允许");
       }
       if (status == Status.STATUS_START){
           throw new IllegalStateException("当前正在录音,请稍后重试!");
       }
       //开始录音并将写入磁盘
       mAudioRecord.startRecording();
       new Thread(new Runnable() {
           @Override
           public void run() {
               writeDataToFile(listener);
           }
       }).start();
   }

    /**
     * 暂停录音
     */
   public void pauseRecord(){
       if (status != Status.STATUS_START){
            throw new IllegalStateException("当前不是正在录音状态");
       }else{
           mAudioRecord.stop();
           status = Status.STATUS_PAUSE;
       }
   }

    /**
     * 停止录音
     */
   public void stopRecord(){
       if (status == Status.STATUS_NO_READY || status == Status.STATUS_READY){
           throw  new IllegalStateException("录音为开始");
       }else{
           mAudioRecord.stop();
           status = Status.STATUS_STOP;
           release();
       }
   }


    /**
     * 释放资源
     */
    public void release() {
        if (mFileList.size() > 0){
            List<String> mPathList = new ArrayList<>();
            for (String mFileName : mFileList) {
                mPathList.add(FileUtils.getPcmFileAbsolutePath(mFileName));
            }
            //清除
            mFileList.clear();
            //将多个pcm文件转化为wav文件
            mergePCMFilesToWAVFile(mPathList);
        }
        if (mAudioRecord != null){
            mAudioRecord.release();
            mAudioRecord = null;
        }
        status = Status.STATUS_NO_READY;
    }

    /**
     * 将pcm合并成wav
     * @param mPathList
     */
    private void mergePCMFilesToWAVFile(final List<String> mPathList) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (PcmToWav.mergePCMFilesToWAVFile(mPathList, FileUtils.getWavFileAbsolutePath(mFileName))) {
                    //操作成功
                } else {
                    //操作失败
                    Log.e("AudioRecorder", "mergePCMFilesToWAVFile fail");
                    throw new IllegalStateException("mergePCMFilesToWAVFile fail");
                }
                mFileName = null;
            }
        }).start();
    }

    /**
     * 将Data写入磁盘文件
     * @param listener 音频流的监听
     */
    private void writeDataToFile(RecordStreamListener listener) {
        byte[] mAudioData = new byte[minBufferSize];
        int mReadSize = 0;
        String currentFileName = mFileName;
        FileOutputStream mFileOutputStream = null;
            try {
                if (status == Status.STATUS_PAUSE){
                    //当前录音状态为暂停录音 后缀加数字防止覆盖原始录音文件
                    currentFileName += mFileList.size();
                }
                mFileList.add(currentFileName);
                File file = new File(FileUtils.getPcmFileAbsolutePath(currentFileName));
                if (file.exists()){
                    file.delete();
                }
                mFileOutputStream = new FileOutputStream(file);
            } catch (IllegalStateException e) {
                Log.e("AudioRecorder", e.getMessage());
                throw new IllegalStateException(e.getMessage());
            } catch (FileNotFoundException e) {
                Log.e("AudioRecorder", e.getMessage());

            }
            //将录音状态设置为正在录音
            status = Status.STATUS_START;
            while (status == Status.STATUS_START){
                mReadSize = mAudioRecord.read(mAudioData,0,minBufferSize);
                if (AudioRecord.ERROR_INVALID_OPERATION != mReadSize && mFileOutputStream != null){
                    try {
                        mFileOutputStream.write(mAudioData);
                        if (listener != null){
                            listener.recordOfByte(mAudioData,0,mAudioData.length);
                        }
                    } catch (IOException e) {
                        Log.e("AudioRecorder", e.getMessage());
                    }
                }
            }
            if (mFileOutputStream != null){
                try {
                    mFileOutputStream.close();
                } catch (IOException e) {
                    Log.e("AudioRecorder", e.getMessage());
                }
            }

    }
    /**
     * 将单个pcm文件转化为wav文件
     */
    private void makePCMFileToWAVFile() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (PcmToWav.makePCMFileToWAVFile(FileUtils.getPcmFileAbsolutePath(mFileName), FileUtils.getWavFileAbsolutePath(mFileName), true)) {
                    //操作成功
                } else {
                    //操作失败
                    Log.e("AudioRecorder", "makePCMFileToWAVFile fail");
                    throw new IllegalStateException("makePCMFileToWAVFile fail");
                }
                mFileName = null;
            }
        }).start();
    }

    /**
     * 获取当前录音Status
     */
    public Status getStatus(){
        return status;
    }
    /**
     * 获取本次录音文件个数
     */
    public int getPcmFileSize(){
        return mFileList.size();
    }


    /**
     * AudioTrack读取WAV文件并播放
     */
    private void playWav(String filePath) {
        int bufferSizeInBytes = AudioTrack.getMinBufferSize(AUDIO_SAMPLE_RATE,AUDIO_CHANNEL,AUDIO_ENCODING);
        mAudioTrack = new AudioTrack(AUDIO_INPUT, AUDIO_SAMPLE_RATE, AUDIO_CHANNEL, AUDIO_ENCODING, bufferSizeInBytes, AudioTrack.MODE_STREAM);
        File fileWav = new File(filePath);
        try {
            dis = new DataInputStream(new FileInputStream(fileWav));
            readWavHeader(dis);
            new Thread(mReadDataRunnable).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readWavHeader(DataInputStream dis) {
        try {
            byte[] byteIntValue = new byte[4];
            byte[] byteShortValue = new byte[2];
            //读取四个
            String mChunkID = "" + (char) dis.readByte() + (char) dis.readByte() + (char) dis.readByte() + (char) dis.readByte();
            Log.e("Wav_Header", "mChunkID:" + mChunkID);
            dis.read(byteIntValue);
            int chunkSize = byteArrayToInt(byteIntValue);
            Log.e("Wav_Header", "chunkSize:" + chunkSize);
            String format = "" + (char) dis.readByte() + (char) dis.readByte() + (char) dis.readByte() + (char) dis.readByte();
            Log.e("Wav_Header", "format:" + format);
            String subchunk1ID = "" + (char) dis.readByte() + (char) dis.readByte() + (char) dis.readByte() + (char) dis.readByte();
            Log.e("Wav_Header", "subchunk1ID:" + subchunk1ID);
            dis.read(byteIntValue);
            int subchunk1Size = byteArrayToInt(byteIntValue);
            Log.e("Wav_Header", "subchunk1Size:" + subchunk1Size);
            dis.read(byteShortValue);
            short audioFormat = byteArrayToShort(byteShortValue);
            Log.e("Wav_Header", "audioFormat:" + audioFormat);
            dis.read(byteShortValue);
            short numChannels = byteArrayToShort(byteShortValue);
            Log.e("Wav_Header", "numChannels:" + numChannels);
            dis.read(byteIntValue);
            int sampleRate = byteArrayToInt(byteIntValue);
            Log.e("Wav_Header", "sampleRate:" + sampleRate);
            dis.read(byteIntValue);
            int byteRate = byteArrayToInt(byteIntValue);
            Log.e("Wav_Header", "byteRate:" + byteRate);
            dis.read(byteShortValue);
            short blockAlign = byteArrayToShort(byteShortValue);
            Log.e("Wav_Header", "blockAlign:" + blockAlign);
            dis.read(byteShortValue);
            short btsPerSample = byteArrayToShort(byteShortValue);
            Log.e("Wav_Header", "btsPerSample:" + btsPerSample);
            String subchunk2ID = "" + (char) dis.readByte() + (char) dis.readByte() + (char) dis.readByte() + (char) dis.readByte();
            Log.e("Wav_Header", "subchunk2ID:" + subchunk2ID);
            dis.read(byteIntValue);
            int subchunk2Size = byteArrayToInt(byteIntValue);
            Log.e("subchunk2Size", "subchunk2Size:" + subchunk2Size);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * byte -> int
     */
    private int byteArrayToInt(byte[] byteIntValue) {
        return ByteBuffer.wrap(byteIntValue).order(ByteOrder.LITTLE_ENDIAN).getInt();
    }

    /**
     * byte -> Short
     */
    private short byteArrayToShort(byte[] byteShortValue) {
        return ByteBuffer.wrap(byteShortValue).order(ByteOrder.LITTLE_ENDIAN).getShort();
    }

    /**
     * 线程持续读取数据,并播放数据
     */
    private Runnable mReadDataRunnable = new Runnable() {
        @Override
        public void run() {
            byte[] buffer = new byte[1024 * 2];
            while (readData(buffer, 0, buffer.length) > 0) {
                if (mAudioTrack.write(buffer, 0, buffer.length) != buffer.length) {
                }

                mAudioTrack.play();
            }
            mAudioTrack.stop();
            mAudioTrack.release();
            try {
                if (dis != null) {
                    dis.close();
                    dis = null;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };
    public int readData(byte[] buffer, int offset, int count) {
        try {
            int nbytes = dis.read(buffer, offset, count);
            if (nbytes == -1) {
                return 0;
            }
            return nbytes;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return -1;
    }

}

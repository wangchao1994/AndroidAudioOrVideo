package com.raisesail.andoid.androidvideoaudio.audiorecord

import android.annotation.SuppressLint
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import com.raisesail.andoid.androidvideoaudio.R
import com.raisesail.andoid.surfaceviewcanvas.two.AudioRecorder
import com.raisesail.andoid.surfaceviewcanvas.two.RecordStreamListener
import java.text.SimpleDateFormat
import java.util.*

/**
 * 录音
 */
class AudioRecordActivity : AppCompatActivity(),View.OnClickListener,RecordStreamListener{

    override fun recordOfByte(data: ByteArray?, begin: Int, end: Int) {
        Log.d("data_audio","data-------${data?.size}   ")
    }


    private lateinit var mAudioRecorder : AudioRecorder
    private lateinit var mStartBtn : Button
    private lateinit var mPauseBtn : Button
    private lateinit var mStopBtn : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        init()
    }

    private fun init() {
        mAudioRecorder = AudioRecorder.getInstance()
        mStartBtn = findViewById(R.id.start_button)
        mPauseBtn = findViewById(R.id.pause_button)
        mStopBtn = findViewById(R.id.stop_button)
        mStartBtn.setOnClickListener(this)
        mPauseBtn.setOnClickListener(this)
        mStopBtn.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.start_button -> startRecord()
            R.id.pause_button -> pauseRecord()
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun startRecord() {
        if (mAudioRecorder.status == AudioRecorder.Status.STATUS_NO_READY){
            var mFileName = SimpleDateFormat("yyyyMMddhhmmss").format(Date())
            mAudioRecorder.createDefaultAudio(mFileName)
            mAudioRecorder.startRecord(null)
            //mAudioRecorder.startRecord(this)
            Log.d("audio","start--------------------------------------->")
        }else{
            mAudioRecorder.stopRecord()
            Log.d("audio","stop--------------------------------------->")

        }
    }

    private fun pauseRecord() {
        if (mAudioRecorder.status == AudioRecorder.Status.STATUS_START){
            mAudioRecorder.pauseRecord()
            Log.d("audio","pause--------------------------------------->")
        }else{
            //mAudioRecorder.startRecord(this)
            mAudioRecorder.startRecord(null)
            Log.d("audio","start--------------------------------------->")

        }
    }

    override fun onPause() {
        super.onPause()
        if (mAudioRecorder.status == AudioRecorder.Status.STATUS_START){
            mAudioRecorder.pauseRecord()
            Log.d("audio","pause--------------------------------------->")

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mAudioRecorder.release()
    }

}

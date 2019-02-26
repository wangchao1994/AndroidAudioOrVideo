package com.raisesail.andoid.androidvideoaudio.camera_h264

import android.annotation.SuppressLint
import android.graphics.ImageFormat
import android.hardware.Camera
import android.media.MediaCodecList
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.raisesail.andoid.androidvideoaudio.R

class CameraNV21Activity : AppCompatActivity() ,SurfaceHolder.Callback,Camera.PreviewCallback{

    private lateinit var mSurfaceView: SurfaceView
    private lateinit var mSurfaceHolder: SurfaceHolder
    private var mCamera: Camera?=null
    private lateinit var parameters: Camera.Parameters
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera_nv21)
        initView()
        if (supportH264Codec()) {
            Log.e("camera_n", "support H264 hard codec");
        } else {
            Log.e("camera_n", "not support H264 hard codec");
        }

    }

    @SuppressLint("ObsoleteSdkInt")
    private fun supportH264Codec(): Boolean {
        if (Build.VERSION.SDK_INT >= 18){
            for (j in MediaCodecList.getCodecCount() - 1 downTo 0){
                val codecInfoAt = MediaCodecList.getCodecInfoAt(j)
                val supportedTypes = codecInfoAt.supportedTypes
                for (i in 0 .. (supportedTypes.size -1)){
                    if (supportedTypes[i].equals("video/avc",true)){
                        return true
                    }
                }
            }
        }
        return false
    }

    private fun initView() {
        mSurfaceView = findViewById(R.id.surface_view)
        mSurfaceHolder = mSurfaceView.holder
        mSurfaceHolder.addCallback(this)
    }

    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
            mCamera?.setPreviewCallback(null)
            mCamera?.stopPreview()
            mCamera?.release()
    }

    override fun surfaceCreated(holder: SurfaceHolder?) {
        mCamera = Camera.open()
        mCamera?.setDisplayOrientation(90)
        parameters = mCamera?.parameters!!
        parameters.setPreviewSize(1280,720)
        parameters.previewFormat = ImageFormat.NV21
        mCamera?.parameters = parameters
        mCamera?.setPreviewCallback(this)
        mCamera?.setPreviewDisplay(holder)
        mCamera?.startPreview()
    }

    override fun onPreviewFrame(data: ByteArray?, camera: Camera?) {
        Log.d("camera_n","data---${data?.size}")
    }
}

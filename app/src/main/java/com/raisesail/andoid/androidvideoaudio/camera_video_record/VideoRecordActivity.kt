package com.raisesail.andoid.androidvideoaudio.camera_video_record

import android.Manifest
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.raisesail.andoid.androidvideoaudio.R
import com.raisesail.andoid.androidvideoaudio.camera_video_record.view.CameraFragment

class VideoRecordActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_record);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.CAMERA
                ), 5
            )
        }
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().add(R.id.container, CameraFragment(), CameraFragment.TAG)
                .commitAllowingStateLoss()
        }
    }
}

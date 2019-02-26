package com.raisesail.andoid.androidvideoaudio.camera_video_record;

import android.Manifest;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import com.raisesail.andoid.androidvideoaudio.R;
import com.raisesail.andoid.androidvideoaudio.camera_video_record.view.CameraFragment;

public class VideoRecordAcitiviyJava extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_record);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.CAMERA}, 5);
        }
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().add(R.id.container, new CameraFragment(), CameraFragment.TAG).commitAllowingStateLoss();
        }
    }
}

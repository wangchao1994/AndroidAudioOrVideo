package com.raisesail.andoid.androidvideoaudio.camera_h264;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import com.raisesail.andoid.androidvideoaudio.R;

import java.io.IOException;

/**
 * BaseCamera
 */
public class CameraNV21ActivityJava extends AppCompatActivity implements SurfaceHolder.Callback,Camera.PreviewCallback {
    private static final int FRAME_RATE = 30;
    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
    private Camera mCamera;
    private Camera.Parameters mCameraParameters;
    private static final int FINAL_ROTATION_0 = 0;
    private static final int FINAL_ROTATION_90 = 90;
    private static final int FINAL_ROTATION_180 = 180;
    private static final int FINAL_ROTATION_270 = 270;
    private static final int ROTATION_360 = 360;
    private final int mCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
    private H264Encoder mH264Encoder;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_nv21);
        initView();
        if (supportH264Codec()) {
            Log.e("camera_n", "support H264 hard codec");
        } else {
            Log.e("camera_n", "not support H264 hard codec");
        }
    }

    private void initView() {
        mSurfaceView = findViewById(R.id.surface_view);
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(this);
    }
    private boolean supportH264Codec() {
        // 遍历支持的编码格式信息
        if (Build.VERSION.SDK_INT >= 18) {
            for (int j = MediaCodecList.getCodecCount() - 1; j >= 0; j--) {
                MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(j);
                String[] types = codecInfo.getSupportedTypes();
                for (int i = 0; i < types.length; i++) {
                    if (types[i].equalsIgnoreCase("video/avc")) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (mCamera == null){
            try {
                mCamera = Camera.open(mCameraId);
            }catch (Exception e){
                new AlertDialog.Builder(this)
                        .setMessage("Camera open failed")
                        .setPositiveButton(android.R.string.ok,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        finish();
                                    }
                                }).show();
            }
        }

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (holder.getSurface() == null)return;
        //改变设置前先关闭相机
        try {
            mCamera.stopPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Camera.Size bestPreviewSize = getBestPreviewSize(width, height);
        try {
            mCamera.setPreviewDisplay(holder);
            setDisplayOrientation();
            mCameraParameters = mCamera.getParameters();
            mCameraParameters.setPreviewSize(bestPreviewSize.width,bestPreviewSize.height);
            mCameraParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);//设置连续自动对焦
            mCameraParameters.setPreviewFormat(ImageFormat.NV21);
            mCamera.setPreviewCallback(this);
            mCamera.startPreview();
            mCamera.cancelAutoFocus();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //mH264Encoder = new H264Encoder(bestPreviewSize.width,bestPreviewSize.height,FRAME_RATE);
        //mH264Encoder.startEncoder();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (mCamera != null){
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
        if (mH264Encoder != null){
            mH264Encoder.stopEncoder();
            mH264Encoder = null;
        }
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        Log.d("main_camera","data----?"+ data.length);
        if (mH264Encoder != null){
            mH264Encoder.putData(data);
        }
    }

    public int getDisplayOrientation(int degrees, int cameraId) {
        // See android.hardware.Camera.setDisplayOrientation for
        // documentation.
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % ROTATION_360;
            result = (ROTATION_360 - result) % ROTATION_360; // compensate the
            // mirror
        } else { // back-facing
            result = (info.orientation - degrees + ROTATION_360) % ROTATION_360;
        }
        return result;
    }

    private void setDisplayOrientation() {
        int mDisplayRotation = getDisplayRotation(this);
        int mDisplayOrientation = getDisplayOrientation(mDisplayRotation, mCameraId);
        mCamera.setDisplayOrientation(mDisplayOrientation);

    }
    public int getDisplayRotation(Activity activity) {
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        int finalRotation = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                finalRotation = FINAL_ROTATION_0;
                break;
            case Surface.ROTATION_90:
                finalRotation = FINAL_ROTATION_90;
                break;
            case Surface.ROTATION_180:
                finalRotation = FINAL_ROTATION_180;
                break;
            case Surface.ROTATION_270:
                finalRotation = FINAL_ROTATION_270;
                break;
            default:
                finalRotation = FINAL_ROTATION_0;
                break;
        }
        return finalRotation;
    }

    private Camera.Size getBestPreviewSize(int width, int height) {
        Camera.Size result = null;
        final Camera.Parameters p = mCamera.getParameters();
        //特别注意此处需要规定rate的比是大的比小的，不然有可能出现rate = height/width，但是后面遍历的时候，current_rate = width/height,所以我们限定都为大的比小的。
        float rate = (float) Math.max(width, height)/ (float)Math.min(width, height);
        float tmp_diff;
        float min_diff = -1f;
        for (Camera.Size size : p.getSupportedPreviewSizes()) {
            float current_rate = (float) Math.max(size.width, size.height)/ (float)Math.min(size.width, size.height);
            tmp_diff = Math.abs(current_rate-rate);
            if( min_diff < 0){
                min_diff = tmp_diff ;
                result = size;
            }
            if( tmp_diff < min_diff ){
                min_diff = tmp_diff ;
                result = size;
            }
        }
        return result;
    }
}

package com.raisesail.andoid.androidvideoaudio.camera_video_record.view;

import java.io.IOException;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import com.raisesail.andoid.androidvideoaudio.R;
import com.raisesail.andoid.androidvideoaudio.camera_video_record.encoder.MediaAudioEncoder;
import com.raisesail.andoid.androidvideoaudio.camera_video_record.encoder.MediaEncoder;
import com.raisesail.andoid.androidvideoaudio.camera_video_record.encoder.MediaMuxerWrapper;
import com.raisesail.andoid.androidvideoaudio.camera_video_record.encoder.MediaVideoEncoder;
import com.raisesail.andoid.androidvideoaudio.camera_video_record.widget.RotateProgress;

public class CameraFragment extends Fragment {
	private static final boolean DEBUG = false;	// TODO set false on release
	public static final String TAG = "CameraFragment";

	/**
	 * for camera preview display
	 */
	private CameraGLView mCameraView;
	/**
	 * for scale mode display
	 */
	private TextView mScaleModeView;
	/**
	 * button for start/stop recording
	 */
	private ImageButton mRecordButton;
	/**
	 * muxer for audio/video recording
	 */
	private MediaMuxerWrapper mMuxer;
    /**
     * video 宽度
     */
	private int mVideoSizeWidth = 1280;
    /**
     * videoSize 高度
     */
	private int mVideoSizeHeight = 720;
	private RotateProgress mRotateProgress;

	public CameraFragment() {
		// need default constructor
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		final View rootView = inflater.inflate(R.layout.fragment_main, container, false);
		mCameraView = (CameraGLView)rootView.findViewById(R.id.cameraView);
		mCameraView.setVideoSize(mVideoSizeWidth, mVideoSizeHeight);
		mCameraView.setOnClickListener(mOnClickListener);
		mScaleModeView = (TextView)rootView.findViewById(R.id.scalemode_textview);
		updateScaleModeText();
		mRecordButton = (ImageButton)rootView.findViewById(R.id.record_button);
		mRecordButton.setOnClickListener(mOnClickListener);
		mRotateProgress = new RotateProgress(getActivity());
		return rootView;
	}

	@Override
	public void onResume() {
		super.onResume();
		if (DEBUG) Log.v(TAG, "onResume:");
		mCameraView.onResume();
	}

	@Override
	public void onPause() {
		if (DEBUG) Log.v(TAG, "onPause:");
		stopRecording();
		mCameraView.onPause();
		super.onPause();
	}

	/**
	 * method when touch record button
	 */
	private final OnClickListener mOnClickListener = new OnClickListener() {
		@Override
		public void onClick(final View view) {
			switch (view.getId()) {
			case R.id.cameraView:
				final int scale_mode = (mCameraView.getScaleMode() + 1) % 4;
				mCameraView.setScaleMode(scale_mode);
				updateScaleModeText();
				break;
			case R.id.record_button:
				if (mMuxer == null){
					startRecording();
					mRecordButton.setImageResource(R.drawable.btn_shutter_video_stop);
				}else{
					mRotateProgress.show();
					mRecordButton.setImageResource(R.drawable.btn_shutter_video);
					stopRecording();
					new Handler().postDelayed(new Runnable() {
						@Override
						public void run() {
							mRotateProgress.hide();
						}
					},1500);
				}
				break;
			}
		}
	};



	private void updateScaleModeText() {
		final int scale_mode = mCameraView.getScaleMode();
		mScaleModeView.setText(
			scale_mode == 0 ? "scale to fit"
			: (scale_mode == 1 ? "keep aspect(viewport)"
			: (scale_mode == 2 ? "keep aspect(matrix)"
			: (scale_mode == 3 ? "keep aspect(crop center)" : ""))));
	}

	/**
	 * start resorcing
	 * This is a sample project and call this on UI thread to avoid being complicated
	 * but basically this should be called on private thread because prepareing
	 * of encoder is heavy work
	 */
	private void startRecording() {
		if (DEBUG) Log.v(TAG, "startRecording:");
		try {
			mRecordButton.setColorFilter(0xffff0000);	// turn red
			mMuxer = new MediaMuxerWrapper(".mp4");	// if you record audio only, ".m4a" is also OK.
			// for video capturing
			new MediaVideoEncoder(mMuxer, mMediaEncoderListener, mCameraView.getVideoWidth(), mCameraView.getVideoHeight());
			// for audio capturing
			new MediaAudioEncoder(mMuxer, mMediaEncoderListener);
			mMuxer.prepare();
			mMuxer.startRecording();
		} catch (final IOException e) {
			mRecordButton.setColorFilter(0);
			Log.e(TAG, "startCapture:", e);
		}
	}

	/**
	 * request stop recording
	 */
	private void stopRecording() {
		if (DEBUG) Log.v(TAG, "stopRecording:mMuxer=" + mMuxer);
		mRecordButton.setColorFilter(0);	// return to default color
		if (mMuxer != null) {
			mMuxer.stopRecording();
			mMuxer = null;
			// you should not wait here
		}
	}

	/**
	 * callback methods from encoder
	 */
	private final MediaEncoder.MediaEncoderListener mMediaEncoderListener = new MediaEncoder.MediaEncoderListener() {
		@Override
		public void onPrepared(final MediaEncoder encoder) {
			if (DEBUG) Log.v(TAG, "onPrepared:encoder=" + encoder);
			if (encoder instanceof MediaVideoEncoder)
				mCameraView.setVideoEncoder((MediaVideoEncoder)encoder);
		}

		@Override
		public void onStopped(final MediaEncoder encoder) {
			if (DEBUG) Log.v(TAG, "onStopped:encoder=" + encoder);
			if (encoder instanceof MediaVideoEncoder)
				mCameraView.setVideoEncoder(null);
		}
	};
}

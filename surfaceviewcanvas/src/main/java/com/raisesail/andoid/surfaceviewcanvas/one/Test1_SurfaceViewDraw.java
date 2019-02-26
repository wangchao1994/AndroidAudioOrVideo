package com.raisesail.andoid.surfaceviewcanvas.one;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import com.raisesail.andoid.surfaceviewcanvas.R;

/**
 * Android音视频开发-1
 * SurfaceView 绘制图片
 * （ImageView , 自定义View）
 */
public class Test1_SurfaceViewDraw extends SurfaceView implements SurfaceHolder.Callback {
    private Paint mPaint;
    private Bitmap bitmap;
    private SurfaceHolder mSurfaceHolder;

    public Test1_SurfaceViewDraw(Context context) {
        this(context, null);
    }

    public Test1_SurfaceViewDraw(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Test1_SurfaceViewDraw(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context,attrs,defStyleAttr);
        mSurfaceHolder = getHolder();
        mSurfaceHolder.addCallback(this);
        mPaint = new Paint();
        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher_round);
    }
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Canvas canvas = mSurfaceHolder.lockCanvas();
        canvas.drawBitmap(bitmap,new Matrix(),mPaint);
        //解锁画布
        mSurfaceHolder.unlockCanvasAndPost(canvas);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
}

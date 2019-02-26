/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein is
 * confidential and proprietary to MediaTek Inc. and/or its licensors. Without
 * the prior written permission of MediaTek inc. and/or its licensors, any
 * reproduction, modification, use or disclosure of MediaTek Software, and
 * information contained herein, in whole or in part, shall be strictly
 * prohibited.
 * 
 * MediaTek Inc. (C) 2014. All rights reserved.
 * 
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER
 * ON AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL
 * WARRANTIES, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NONINFRINGEMENT. NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH
 * RESPECT TO THE SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY,
 * INCORPORATED IN, OR SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES
 * TO LOOK ONLY TO SUCH THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO.
 * RECEIVER EXPRESSLY ACKNOWLEDGES THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO
 * OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES CONTAINED IN MEDIATEK
 * SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK SOFTWARE
 * RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S
 * ENTIRE AND CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE
 * RELEASED HEREUNDER WILL BE, AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE
 * MEDIATEK SOFTWARE AT ISSUE, OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE
 * CHARGE PAID BY RECEIVER TO MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek
 * Software") have been modified by MediaTek Inc. All revisions are subject to
 * any receiver's applicable license agreements with MediaTek Inc.
 */

package com.raisesail.andoid.androidvideoaudio.camera_video_record.widget;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.raisesail.andoid.androidvideoaudio.R;


public class RotateProgress{
    @SuppressWarnings("unused")
    private static final String TAG = "RotateProgress";
    
    private ProgressBar mRotateDialogSpinner;
    private TextView mRotateDialogText;
    
    private String mMessage;
    private Context mContext;
    private Activity mActivity;
    private View mView;
    private boolean mShowing = false;
    private boolean mIsEnabled = true;
    
    public RotateProgress(Activity activity) {
    	//mContext = context;
    	mActivity = activity;
    }
    
    protected View getView() {
        View v = mActivity.getLayoutInflater().inflate(R.layout.rotate_progress, null);
        mRotateDialogSpinner = (ProgressBar) v.findViewById(R.id.rotate_dialog_spinner);
        mRotateDialogText = (TextView) v.findViewById(R.id.rotate_dialog_text);
        return v;
    }
    
    protected void onRefresh() {
        mRotateDialogText.setText(mMessage);
        mRotateDialogText.setVisibility(View.VISIBLE);
        mRotateDialogSpinner.setVisibility(View.VISIBLE);
    }
    
    public void showProgress(String msg) {
        mMessage = msg;
        show();
    }
    
    public void show() {
        if (mView == null) {
            mView = getView();
            addView(mView);
        }
        if (mView != null && !mShowing) {
            mShowing = true;
            setEnabled(true);
            refresh();// refresh view state
            mView.setVisibility(View.VISIBLE);
        } else if (mShowing) {
            refresh();
        }
    }
    
    public void hide() {
        if (mView != null && mShowing) {
            mShowing = false;
            mView.setVisibility(View.GONE);
        }
    }
    
    public boolean isShowing() {
        return mShowing;
    }
    
    private void addView(View view) {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        params.topMargin = 0;
        if (mView != null) {
        	mActivity.addContentView(mView, params);
        }
    }
    
    public void setEnabled(boolean enabled) {
        mIsEnabled = enabled;
        if (mView != null) {
            mView.setEnabled(mIsEnabled);
        }
    }
    private void refresh() {
        if (mShowing) {
            onRefresh();
        }
    }
}

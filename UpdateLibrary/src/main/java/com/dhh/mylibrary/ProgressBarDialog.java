package com.dhh.mylibrary;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Created by 79393 on 2018/12/15.
 */

public class ProgressBarDialog extends DialogFragment {

    private ProgressBar mProgressBar;
    private TextView tv_percent;
    private TextView tv_stopOrStart;
    private TextView tv_cancel;
    private TextView tv_gottoback;
    private View view;
    private static final int STOP = 1;
    private static final int GOAGAIN = 0;
    private CusServiceConnection.OnClickListener mListener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        view =  inflater.inflate(R.layout.progressbar_dialog,null);
        return view;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = new Dialog(getActivity(),R.style.chooseFragmentDialog);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.e("onActivityCreated","yes");
        mProgressBar = view.findViewById(R.id.pb_load);
        tv_stopOrStart = view.findViewById(R.id.tv_stop);
        tv_stopOrStart.setTag(STOP);
        tv_cancel = view.findViewById(R.id.tv_stop);
        tv_percent = view.findViewById(R.id.tv_percent);
        tv_gottoback = view.findViewById(R.id.tv_gotoback);
    }

    public void setProgressBarProgress(int progress){
        mProgressBar.setProgress(progress);
        tv_percent.setText(progress + "%");
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if(mListener != null)
            setListen();
    }

    private void setListen(){
        view.findViewById(R.id.tv_stop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if((int)tv_stopOrStart.getTag() == STOP){
                    mListener.stop();
                    tv_stopOrStart.setText("继续");
                }
                else {
                    mListener.goAgain();
                    tv_stopOrStart.setText("暂停");
                }
            }
        });
        view.findViewById(R.id.tv_gotoback).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.gottoback();
            }
        });
        view.findViewById(R.id.tv_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                mListener.cancel();
            }
        });
    }

    public void setListen(final CusServiceConnection.OnClickListener listener){
        mListener = listener;
    }
}


package com.team.meeting;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class SpeechService extends Service {
    protected AudioManager mAudioManager;
    protected SpeechRecognizer mSpeechRecognizer;
    protected Intent mSpeechRecognizerIntent;
    protected final Messenger mServerMessenger = new Messenger(new IncomingHandler(this));

    public static boolean mIsListening;
    protected volatile boolean mIsCountDownOn;
    private static String TAG = "SpeechService";
    SpeechCallback scb;
    public static String mString = "";
    public static String mProcessedString = "";
    public static boolean mCancelledFromParent;
    private static Context mContext;

    static final int MSG_RECOGNIZER_START_LISTENING_OVERRIDE = 0;
    static final int MSG_RECOGNIZER_START_LISTENING = 1;
    static final int MSG_RECOGNIZER_CANCEL = 2;
    static final int MSG_RECOGNIZER_CANCEL_OVERRIDE = 3;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this.getBaseContext();
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        mSpeechRecognizer.setRecognitionListener(new SpeechRecognitionListener());
        mSpeechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
                this.getPackageName());
    }

    @Override
    public IBinder onBind(Intent arg0) {
        Log.d(TAG, "onBind");

        return mServerMessenger.getBinder();
    }

    protected static class IncomingHandler extends Handler {
        private WeakReference<SpeechService> mtarget;

        IncomingHandler(SpeechService target) {
            mtarget = new WeakReference<SpeechService>(target);
        }

        @Override
        public void handleMessage(Message msg) {
            final SpeechService target = mtarget.get();

            switch (msg.what) {
                case MSG_RECOGNIZER_START_LISTENING_OVERRIDE:

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        // turn off beep sound
                        target.mAudioManager.setStreamMute(AudioManager.STREAM_SYSTEM, true);
                    }
                    mCancelledFromParent = false;
                    target.mSpeechRecognizer.startListening(target.mSpeechRecognizerIntent);
                    target.mIsListening = true;
                    mString = "";
                    mProcessedString = "";
                    Log.d(TAG, "message start listening"); //$NON-NLS-1$

                    break;
                case MSG_RECOGNIZER_START_LISTENING:

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        // turn off beep sound
                        target.mAudioManager.setStreamMute(AudioManager.STREAM_SYSTEM, true);
                    }
                    if (!target.mIsListening) {
                        target.mSpeechRecognizer.startListening(target.mSpeechRecognizerIntent);
                        target.mIsListening = true;
                        Log.d(TAG, "message start listening"); //$NON-NLS-1$
                    }
                    break;

                case MSG_RECOGNIZER_CANCEL:
                    target.mSpeechRecognizer.cancel();
                    target.mIsListening = false;
                    Log.d(TAG, "message canceled recognizer"); //$NON-NLS-1$
                    break;

                case MSG_RECOGNIZER_CANCEL_OVERRIDE:
                    target.mSpeechRecognizer.cancel();
                    target.mIsListening = false;
                    mCancelledFromParent = true;
                    Log.d(TAG, "message canceled recognizer override"); //$NON-NLS-1$
                    Intent intent = new Intent(mContext, EditActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                    intent.putExtra("original", mString);
                    intent.putExtra("processed", mProcessedString);
                    mString = "";
                    mProcessedString = "";
                    mContext.startActivity(intent);
                    break;
            }
        }
    }

    // Count down timer for Jelly Bean work around
    protected CountDownTimer mNoSpeechCountDown = new CountDownTimer(5000, 5000) {

        @Override
        public void onTick(long millisUntilFinished) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onFinish() {
            Log.d(TAG, "timer called");
            mIsCountDownOn = false;
            Message message = Message.obtain(null,
                    MSG_RECOGNIZER_CANCEL);
            try {
                mServerMessenger.send(message);
                if (!mCancelledFromParent) {
                    message = Message.obtain(null, MSG_RECOGNIZER_START_LISTENING);
                    mServerMessenger.send(message);
                }

            } catch (RemoteException e) {
            }

        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mIsCountDownOn) {
            mNoSpeechCountDown.cancel();
        }
        if (mSpeechRecognizer != null) {
            mSpeechRecognizer.destroy();
        }
    }

    protected class SpeechRecognitionListener implements RecognitionListener {

        private static final String TAG = "SpeechService";

        @Override
        public void onBeginningOfSpeech() {
            // speech input will be processed, so there is no need for count
            // down anymore
            if (mIsCountDownOn)
            {
                mIsCountDownOn = false;
                mNoSpeechCountDown.cancel();
            }
            Log.d(TAG, "onBeginingOfSpeech"); //$NON-NLS-1$
        }

        @Override
        public void onBufferReceived(byte[] buffer)
        {

        }

        @Override
        public void onEndOfSpeech()
        {
            if (mIsCountDownOn) {
                mIsCountDownOn = false;
                mNoSpeechCountDown.cancel();
            }
            mIsListening = false;
            Message message = Message.obtain(null, MSG_RECOGNIZER_START_LISTENING);
            try {
                mServerMessenger.send(message);
            } catch (RemoteException e) {

            }
            Log.d(TAG, "onEndOfSpeech"); //$NON-NLS-1$
        }

        @Override
        public void onError(int error)
        {
            //Log.d(TAG, "error = " + error); //$NON-NLS-1$
        }

        @Override
        public void onEvent(int eventType, Bundle params) {

        }

        @Override
        public void onPartialResults(Bundle partialResults) {

        }

        @Override
        public void onReadyForSpeech(Bundle params)
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                mIsCountDownOn = true;
                mNoSpeechCountDown.start();
                mAudioManager.setStreamMute(AudioManager.STREAM_SYSTEM, false);
            }
            Log.d(TAG, "onReadyForSpeech"); //$NON-NLS-1$
        }

        @Override
        public void onResults(Bundle results) {

            String str = new String();
            ArrayList data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            for (int i = 0; i < data.size(); i++)
            {
                if (str == null) {
                    str = data.get(i) + "";
                } else {
                    str = str + "," + data.get(i);
                }
            }
            mString = mString + ":::" + str;
            mProcessedString = mProcessedString + ":::" + data.get(0);
            Log.d(TAG, "onResults -" + str);
            Toast.makeText(getApplicationContext(), str, Toast.LENGTH_SHORT).show();
            mIsListening = false;
            Message message = Message.obtain(null, MSG_RECOGNIZER_START_LISTENING);
            try {
                mServerMessenger.send(message);
            } catch (RemoteException e) {

            }
        }

        @Override
        public void onRmsChanged(float rmsdB)
        {

        }

    }

}

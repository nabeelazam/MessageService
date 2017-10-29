package com.messageservice;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import java.util.Locale;

public class MessageService extends Service {
    private static final String TAG = "MessageService";
    private Handler handler = new Handler();
    private TextToSpeech textToSpeech;
    private ResultReceiver mReceiver;
    final static String KEY_FROM_SERVICE = "fromService", KEY_FROM_CLIENT = "fromClient", KEY_ERROR = "error", KEY_COMPLETED = "completed";
    final static int TTS_INIT_ERROR = 0, TTS_LANG_NOT_SUPPORTED = -1, TTS_COMPLETED = 200;

    @Override
    public IBinder onBind(final Intent intent) {

        // Call text to speech init
        initializeTextToSpeech(intent);

        return new IConsumeMessage.Stub() {
            @Override
            public void speakTextMessage(final String message, ResultReceiver receiver) throws RemoteException {

                Log.e(TAG, "speakTextMessage : " + message);

                // Assigning receiver
                mReceiver = receiver;

                // If have to show dialog, this has to be in handler or thread
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        // Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();

                        Bundle params = new Bundle();
                        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "");
                        textToSpeech.speak(message, TextToSpeech.QUEUE_FLUSH, params, "Speech");

                        textToSpeech.setOnUtteranceCompletedListener(new TextToSpeech.OnUtteranceCompletedListener() {
                            @Override
                            public void onUtteranceCompleted(String utteranceId) {
                                // Send completion message
                                sendMessageToClientApp(intent, TTS_COMPLETED, KEY_COMPLETED);
                            }
                        });
                    }
                }, 500);

            }
        };
    }


    /**
     * TextToSpeech initialization
     */
    private void initializeTextToSpeech(final Intent intent) {
        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = textToSpeech.setLanguage(Locale.US);

                    if (result == TextToSpeech.LANG_MISSING_DATA
                            || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e(TAG, "Given language/locale is not supported");
                        // Send language not supported error
                        sendMessageToClientApp(intent, TTS_LANG_NOT_SUPPORTED, KEY_ERROR);
                    }
                } else {
                    Log.e(TAG, "TextToSpeech initialization failed.");
                    // Send TTS Initialization failed message
                    sendMessageToClientApp(intent, TTS_INIT_ERROR, KEY_ERROR);
                }
            }
        });
    }


    /**
     * Send messages to client application i.e. TTS init, error or TTS completion
     */
    private void sendMessageToClientApp(Intent intent, int code, String value) {
        try {
            // Getting receiver from intent
            //receiver = intent.getParcelableExtra(KEY_FROM_CLIENT);
            Bundle bundle = new Bundle();
            bundle.putString(KEY_FROM_SERVICE, value);
            mReceiver.send(code, bundle);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }
}

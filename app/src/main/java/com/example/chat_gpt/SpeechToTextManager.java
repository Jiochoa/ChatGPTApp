package com.example.chat_gpt;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;

public class SpeechToTextManager {
    private MainActivity mainActivity;
    private Context context;
    private SpeechRecognizer speechRecognizer;
    private RecognitionListener recognitionListener;
    private Intent intent;
    private boolean readyToSend;

    // getters and setters
    public SpeechRecognizer getSpeechRecognizer() {
        return speechRecognizer;
    }

    // constructor
    public SpeechToTextManager(MainActivity mainActivity,Context context) {
        this.mainActivity = mainActivity;
        this.context = context;
        readyToSend = false;

        this.speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
        this.intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle bundle) {

            }

            @Override
            public void onBeginningOfSpeech() {

            }

            @Override
            public void onRmsChanged(float v) {

            }

            @Override
            public void onBufferReceived(byte[] bytes) {

            }

            @Override
            public void onEndOfSpeech() {

            }

            @Override
            public void onError(int i) {

            }

            @Override
            public void onResults(Bundle bundle) {
                ArrayList<String> data = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                String prompt = data.get(0);
                mainActivity.processPrompts(prompt);

            }

            @Override
            public void onPartialResults(Bundle bundle) {

            }

            @Override
            public void onEvent(int i, Bundle bundle) {

            }
        });

    }

    public void startListening(){
        this.speechRecognizer.startListening(this.intent);
    }
}

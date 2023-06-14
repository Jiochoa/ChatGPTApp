package com.example.chat_gpt;

import android.content.Context;
import android.speech.tts.TextToSpeech;

import java.util.Locale;

public class TextToSpeechManager {
    private Context context;
    private TextToSpeech textToSpeech;
    private boolean isEnabled = false;

    // constructor
    public TextToSpeechManager(Context context) {
        textToSpeech = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    textToSpeech.setLanguage(Locale.US);
                }
            }
        });

        this.context = context;
    }

    // getters and setters
    public TextToSpeech getTextToSpeech() {
        return textToSpeech;
    }
    public boolean isEnabled() {
        return isEnabled;
    }
    public void setEnabled(boolean enable) {
        isEnabled = enable;
    }

}

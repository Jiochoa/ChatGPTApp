package com.example.chat_gpt;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;
import com.stfalcon.chatkit.commons.ImageLoader;
import com.stfalcon.chatkit.messages.MessagesList;
import com.stfalcon.chatkit.messages.MessagesListAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


public class MainActivity extends AppCompatActivity {
    EditText editText;
    ImageButton sendBtn, micBtn;
    MessagesList messagesList;
    User us, chatgpt;
    MessagesListAdapter<Message> adapter;
    TextToSpeechManager ttsManager;
    ChatGPTManager chatGPTManager;
    SpeechToTextManager sttManager;
    //---------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editText = findViewById(R.id.editTextText);
        sendBtn = findViewById(R.id.imageButton);
        micBtn = findViewById(R.id.imageButton2);
        messagesList = findViewById(R.id.messagesList);
        ttsManager = new TextToSpeechManager(getApplicationContext());
        ttsManager.setEnabled(true);
        chatGPTManager = new ChatGPTManager(this, this);
        sttManager = new SpeechToTextManager(this,this);

        ImageLoader imageLoader = new ImageLoader() {
            @Override
            public void loadImage(ImageView imageView, @Nullable String url, @Nullable Object payload) {
                Picasso.get().load(url).into(imageView);
            }
        };

        // --------- permissions ------------
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, 121);
        }

        adapter = new MessagesListAdapter<>("1", imageLoader);
        messagesList.setAdapter(adapter);

        us = new User("1", "John Doe", "");
        chatgpt = new User("2", "ChatGPT", "");

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String prompt = editText.getText().toString();
                processPrompts(prompt);
            }
        });

        micBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sttManager.startListening();
            }
        });
    }

    public void processTextResponse(String response) {
        Message message = new Message("m2", response.trim(), chatgpt, Calendar.getInstance().getTime(), null);
        adapter.addToStart(message, true);
        if (ttsManager.isEnabled()) {
            ttsManager.getTextToSpeech().speak(response, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    public void processImagesResponse(ArrayList<String> responseArray) {
        for (int i = 0; i < responseArray.size(); i++) {
            Message message1 = new Message("m2", "image", chatgpt, Calendar.getInstance().getTime(), responseArray.get(i).trim());
            adapter.addToStart(message1, true);
        }
    }

    public void processPrompts (String prompt){
        Message message = new Message("m1", prompt, us, Calendar.getInstance().getTime(), null);
        adapter.addToStart(message, true);

        boolean isPromptingImage = prompt.startsWith("generate image");
        if (isPromptingImage) {
            chatGPTManager.sendImageQuery(prompt);
        } else {
            chatGPTManager.sendTextQuery(prompt);
        }
        editText.setText("");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.voice) {
            if (ttsManager.isEnabled()) {
                ttsManager.setEnabled(false);
                ttsManager.getTextToSpeech().stop();
                item.setIcon(R.drawable.baseline_voice_over_off_24);
            } else {
                ttsManager.setEnabled(true);
                item.setIcon(R.drawable.baseline_record_voice_over_24);
            }
        }
        return super.onOptionsItemSelected(item);
    }

}

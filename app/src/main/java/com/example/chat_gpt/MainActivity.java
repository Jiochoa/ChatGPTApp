package com.example.chat_gpt;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.stfalcon.chatkit.commons.ImageLoader;
import com.stfalcon.chatkit.messages.MessagesList;
import com.stfalcon.chatkit.messages.MessagesListAdapter;

import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    EditText editText;
    ImageButton sendBtn, micBtn;
    MessagesList messagesList;
    User us, chatgpt;
    MessagesListAdapter<Message> adapter;
    //-------------------------------
    TextToSpeechManager ttsManager;
    ChatGPTManager chatGPTManager;
    SpeechToTextManager sttManager;
    //---------------------
    private StringBuilder conversationHistory;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editText = findViewById(R.id.promptText);
        sendBtn = findViewById(R.id.imageButton);
        micBtn = findViewById(R.id.imageButton2);
        messagesList = findViewById(R.id.messagesList);
        ttsManager = new TextToSpeechManager(getApplicationContext());
        ttsManager.setEnabled(true);
        chatGPTManager = new ChatGPTManager(this, this);
        sttManager = new SpeechToTextManager(this,this);
        conversationHistory = new StringBuilder();

        us = new User("1", "John Doe", "");
        chatgpt = new User("2", "ChatGPT", "");
        ImageLoader imageLoader = new ImageLoader() {
            @Override
            public void loadImage(ImageView imageView, @Nullable String url, @Nullable Object payload) {
                Picasso.get().load(url).into(imageView);
            }
        };
        adapter = new MessagesListAdapter<>("1", imageLoader);
        messagesList.setAdapter(adapter);

        // --------- permissions ------------
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, 121);
        }


        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String prompt = editText.getText().toString();
                processUserInput(prompt);
            }
        });

        micBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sttManager.startListening();
            }
        });
    }

    // use the prompt text to figure out if the user wants a text answer or to generate an image
    public void processUserInput (String prompt){
        // Append the user's prompt to the conversation history
        conversationHistory.append(prompt).append("\n");

        // show prompt text in the GUI
        Message message = new Message("m1", prompt, us, Calendar.getInstance().getTime(), null);
        adapter.addToStart(message, true);


        // decide if generate image or text response
        boolean isPromptingImage = prompt.startsWith("generate image");
        if (isPromptingImage) {
            chatGPTManager.sendImageQuery(prompt);
//            chatGPTManager.sendImageQuery(conversationHistory.toString());

        } else {
//            chatGPTManager.sendTextQuery(prompt);
            chatGPTManager.sendTextQuery(conversationHistory.toString());

        }
        editText.setText("");
    }

    // take the text response from chatgpt and show it in the GUI
    public void displayTextResponse(String response) {
        // Append the response to the conversation history
        conversationHistory.append(response).append("\n");

        Message message = new Message("m2", response.trim(), chatgpt, Calendar.getInstance().getTime(), null);
        adapter.addToStart(message, true);
        if (ttsManager.isEnabled()) {
            ttsManager.getTextToSpeech().speak(response, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    // take the images response from chatgpt and show it in the GUI
    public void displayImagesResponse(ArrayList<String> responseArray) {
        for (int i = 0; i < responseArray.size(); i++) {
            Message message1 = new Message("m2", "image", chatgpt, Calendar.getInstance().getTime(), responseArray.get(i).trim());
            adapter.addToStart(message1, true);
        }
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

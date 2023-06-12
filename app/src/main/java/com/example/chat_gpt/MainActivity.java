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

// 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
public class MainActivity extends AppCompatActivity {
    private String theKey = "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";
    //    TextView resultTV;
    // test comment
    EditText editText;
    ImageButton sendBtn,micBtn;
    MessagesList messagesList;
    User us, chatgpt;
    MessagesListAdapter<Message> adapter;
    TextToSpeech tts;
    boolean isTTS = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        resultTV = findViewById(R.id.tv);
        editText = findViewById(R.id.editTextText);
        sendBtn = findViewById(R.id.imageButton);
        micBtn = findViewById(R.id.imageButton2);
        messagesList = findViewById(R.id.messagesList);
        ImageLoader imageLoader = new ImageLoader() {
            @Override
            public void loadImage(ImageView imageView, @Nullable String url, @Nullable Object payload) {
                Picasso.get().load(url).into(imageView);

            }
        };

        adapter = new MessagesListAdapter<>("1", imageLoader);
        messagesList.setAdapter(adapter);

        us = new User("1", "John Doe", "");
        chatgpt = new User("2", "ChatGPT", "");

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Message message = new Message("m1", editText.getText().toString(), us, Calendar.getInstance().getTime(), null);
                adapter.addToStart(message, true);

                if (editText.getText().toString().startsWith("generate image")) {
                    generateImage(editText.getText().toString());
                } else {
                    performAction(editText.getText().toString());

                }

                editText.setText("");
            }
        });

        tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    tts.setLanguage(Locale.US);
                }
            }
        });

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, 121);
        }

        SpeechRecognizer speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        micBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                speechRecognizer.startListening(intent);
            }
        });

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
                Message message = new Message("m1", data.get(0), us, Calendar.getInstance().getTime(), null);
                adapter.addToStart(message, true);

                if (data.get(0).toLowerCase().startsWith("generate image")) {
                    generateImage(data.get(0));
                } else {
                    performAction(data.get(0));

                }

                editText.setText("");
            }

            @Override
            public void onPartialResults(Bundle bundle) {

            }

            @Override
            public void onEvent(int i, Bundle bundle) {

            }
        });

    }

    public void performAction(String message) {
        String url = "https://api.openai.com/v1/chat/completions";

        JSONObject payload;
        try {
            payload = makeRequest(message);
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, payload,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            String answer = extractContentFromJsonObject(response);
                            Message message1 = new Message("m2", answer.trim(), chatgpt, Calendar.getInstance().getTime(), null);
                            adapter.addToStart(message1, true);
                            if (isTTS) {
                                tts.speak(answer, TextToSpeech.QUEUE_FLUSH, null, null);
                            }
//                            resultTV.setText(answer);
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
//                            resultTV.setText("That didn't 2work!");
                        }
                    }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    headers.put("Authorization", "Bearer " + theKey); // Replace with your actual API key
                    return headers;
                }
            };
            RequestQueue queue = Volley.newRequestQueue(this);
            request.setRetryPolicy(new RetryPolicy() {
                @Override
                public int getCurrentTimeout() {
                    return 60000;
                }

                @Override
                public int getCurrentRetryCount() {
                    return 15;
                }

                @Override
                public void retry(VolleyError error) throws VolleyError {
                }
            });
            queue.add(request);

        } catch (JSONException e) {
            throw new RuntimeException(e);
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
            if (isTTS) {
                isTTS = false;
                tts.stop();
                item.setIcon(R.drawable.baseline_voice_over_off_24);
            } else {
                isTTS = true;
                item.setIcon(R.drawable.baseline_record_voice_over_24);
            }
        }
        return super.onOptionsItemSelected(item);
    }

    public void generateImage(String message) {
        String url = "https://api.openai.com/v1/images/generations";

        JSONObject payload2;
        try {
            payload2 = makeRequest2(message);
            Log.d("PAYLOAD2", payload2.toString());
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, payload2,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
//                            String answer = extractImagesFromJsonObject(response);
                            extractImagesFromJsonObject(response);
//                            Message message1 = new Message("m2","image", chatgpt, Calendar.getInstance().getTime(), answer.trim());
//                            adapter.addToStart(message1, true);
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
//                            resultTV.setText("That didn't 2work!");
                        }
                    }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    headers.put("Authorization", "Bearer " + theKey); // Replace with your actual API key
                    return headers;
                }
            };
            RequestQueue queue = Volley.newRequestQueue(this);
            request.setRetryPolicy(new RetryPolicy() {
                @Override
                public int getCurrentTimeout() {
                    return 60000;
                }

                @Override
                public int getCurrentRetryCount() {
                    return 15;
                }

                @Override
                public void retry(VolleyError error) throws VolleyError {
                }
            });
            queue.add(request);

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

    }

    // messages
    public JSONObject makeRequest(String message) throws JSONException {
        String json = "{\n" +
                "    \"model\": \"gpt-3.5-turbo\",\n" +
                "    \"messages\": [{\"role\": \"user\", \"content\": \"" + message + "\"}],\n" +
                "    \"max_tokens\": 200,\n" +
                "    \"temperature\": 0.7\n" +
                "}";

        return new JSONObject(json);
    }

    // images
    public JSONObject makeRequest2(String message) throws JSONException {

        String prompt = message;
        String n = "2";
        String size = "1024x1024";

        String jsonInputString = "{\"prompt\":\"" + prompt + "\",\"n\":" + n + ",\"size\":\"" + size + "\"}";


        JSONObject temp = new JSONObject(jsonInputString);
        Log.d("MAKE REQUEST2", temp.toString());

        return temp;
    }


    // pares the response json to get only the answer
    public String extractContentFromJsonObject(JSONObject jsonObject) {
        try {
            JSONArray choicesArray = jsonObject.getJSONArray("choices");
            JSONObject firstChoice = choicesArray.getJSONObject(0);
            JSONObject message = firstChoice.getJSONObject("message");
            return message.getString("content");
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    // images
    public void extractImagesFromJsonObject(JSONObject jsonObject) {
        String answer;
        try {
            JSONArray dataArray = jsonObject.getJSONArray("data");
            for (int i = 0; i < dataArray.length(); i++) {
                JSONObject firstChoice = dataArray.getJSONObject(i);
                answer = dataArray.getJSONObject(i).getString("url");
                Message message1 = new Message("m2", "image", chatgpt, Calendar.getInstance().getTime(), answer.trim());
                adapter.addToStart(message1, true);
            }


//            JSONObject message = firstChoice.getJSONObject("message");
//            return firstChoice.getString("url");
        } catch (JSONException e) {
            e.printStackTrace();
//            return null;
        }
    }

}
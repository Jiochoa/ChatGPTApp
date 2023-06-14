package com.example.chat_gpt;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class ChatGPTManager {
    private MainActivity mainActivity;
    private String apiKey = "";
    private String imageUrl = "https://api.openai.com/v1/images/generations";
    private String textUrl = "https://api.openai.com/v1/chat/completions";
    private Context context;
    private JSONObject payload;
    private String textAnswer;
    private ArrayList<String> imagesAnswer;

    public ChatGPTManager(MainActivity mainActivity,Context context) {
        this.mainActivity = mainActivity;
        this.context = context;
        this.payload = new JSONObject();
        this.imagesAnswer = new ArrayList<>();
        this.textAnswer = "";
    }

    public String sendTextQuery(String query) {
        try {
            // format query
            payload = formatTextQuery(query);

            // send query
            JsonObjectRequest request
                    = new JsonObjectRequest(Request.Method.POST, textUrl, payload,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            extractAnswerFromResponse(response);
                            // this method calls a method from the the MainActivity
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d("ERROR GETTING RESPONSE", "That didn't 3work!");
                        }
                    }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    headers.put("Authorization", "Bearer " + apiKey);
                    return headers;
                }
            };
            RequestQueue queue = Volley.newRequestQueue(context);
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

        if (textAnswer != null) return textAnswer;
        return null;
    }

    public ArrayList<String> sendImageQuery(String query) {
//        ArrayList<String> strResponse;
        try {
            // format query
            payload = formatImageQuery(query);

            // send query
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, imageUrl, payload,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            extractImagesFromResponse(response);
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d("ERROR GETTING RESPONSE", "That didn't 3work!");
                        }
                    }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    headers.put("Authorization", "Bearer " + apiKey);
                    return headers;
                }
            };
            RequestQueue queue = Volley.newRequestQueue(context);
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


        return null;
    }

    private void extractAnswerFromResponse(JSONObject response) {
        try {
            JSONArray choicesArray = response.getJSONArray("choices");
            JSONObject firstChoice = choicesArray.getJSONObject(0);
            JSONObject message = firstChoice.getJSONObject("message");
//            return message.getString("content");
//            textAnswer = message.getString("content");
            String ans = message.getString("content");
            mainActivity.processTextResponse(ans);


        } catch (JSONException e) {
            e.printStackTrace();
//            return null;
        }
    }

    private void extractImagesFromResponse(JSONObject response) {
        String answer;
        try {
            JSONArray dataArray = response.getJSONArray("data");
            for (int i = 0; i < dataArray.length(); i++) {
                JSONObject firstChoice = dataArray.getJSONObject(i);
                answer = dataArray.getJSONObject(i).getString("url");
                imagesAnswer.add(answer);
            }
            mainActivity.processImagesResponse(imagesAnswer);
            imagesAnswer = new ArrayList<>();

        } catch (JSONException e) {
            e.printStackTrace();
//            return null;
        }

    }
    private JSONObject formatTextQuery(String query) throws JSONException {
        String json = "{\n" +
                "    \"model\": \"gpt-3.5-turbo\",\n" +
                "    \"messages\": [{\"role\": \"user\", \"content\": \"" + query + "\"}],\n" +
                "    \"max_tokens\": 200,\n" +
                "    \"temperature\": 0.7\n" +
                "}";

        return new JSONObject(json);

    }
    private JSONObject formatImageQuery(String query) throws JSONException {
        String n = "2";
        String size = "1024x1024";
        String jsonInputString
            = "{\"prompt\":\"" + query + "\",\"n\":" + n + ",\"size\":\"" + size + "\"}";

        JSONObject temp = new JSONObject(jsonInputString);
        return temp;
    }

}


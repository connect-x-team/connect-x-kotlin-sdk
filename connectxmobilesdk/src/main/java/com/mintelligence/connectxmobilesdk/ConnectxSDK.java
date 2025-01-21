package com.mintelligence.connectxmobilesdk;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import okhttp3.*;
import org.json.JSONObject;
import org.json.JSONException;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.List;

public class ConnectxSDK {

    private static ConnectxSDK instance;
    private String token;
    private String organizeId;
    private String userAgent;
    private String cookie;

    private static final String API_DOMAIN = "https://backend.connect-x.tech/connectx/api";
    private static final String GENERATE_COOKIE_URL = "https://backend.connect-x.tech/connectx/api/webtracking/generateCookie";

    private OkHttpClient client;

    private ConnectxSDK(Context context) {
        client = new OkHttpClient();
        String appName = context.getPackageManager().getApplicationLabel(context.getApplicationInfo()).toString();
        String appVersion;
        try {
            appVersion = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (Exception e) {
            appVersion = "1.0";
        }
        userAgent = appName + "/" + appVersion + " (Android " + Build.VERSION.RELEASE + "; " + Build.MODEL + ")";
    }

    public static synchronized ConnectxSDK getInstance(Context context) {
        if (instance == null) {
            instance = new ConnectxSDK(context);
        }
        return instance;
    }

    public void initialize(String token, String organizeId) {
        if (token.isEmpty()) {
            throw new IllegalArgumentException("Token must not be empty.");
        }
        if (organizeId.isEmpty()) {
            throw new IllegalArgumentException("Organize ID must not be empty.");
        }

        this.token = token;
        this.organizeId = organizeId;

        // Get cookie (This could be handled via a network request)
        this.cookie = getUnknownId();
    }

    private String getUnknownId() {
        Request request = new Request.Builder().url(GENERATE_COOKIE_URL).build();
        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                return response.body().string();
            } else {
                return null;
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to set cookies: " + e.getMessage());
        }
    }

    private Map<String, String> getClientData() {
        Map<String, String> clientData = new HashMap<>();
        clientData.put("cx_isBrowser", "false");
        clientData.put("cx_language", Locale.getDefault().getLanguage());
        clientData.put("cx_browserName", "");
        clientData.put("cx_browserVersion", "");
        clientData.put("cx_engineName", "Android");
        clientData.put("cx_engineVersion", Integer.toString(Build.VERSION.SDK_INT));
        clientData.put("cx_userAgent", userAgent);
        clientData.put("cx_source", "Android App");
        clientData.put("cx_type", "Mobile App");
        clientData.put("cx_fingerprint", Build.SERIAL);
        clientData.put("os", "Android");
        clientData.put("osVersion", Build.VERSION.RELEASE);
        clientData.put("device", Build.MODEL);
        return clientData;
    }

    private void cxPost(String endpoint, JSONObject data) throws IOException {
        RequestBody body = RequestBody.create(data.toString(), MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url(API_DOMAIN + endpoint)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer " + token)
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Request failed: " + response.message());
            }
        }
    }

    public void cxTracking(Map<String, String> body) {
        Map<String, String> clientData = getClientData();
        JSONObject data = new JSONObject(clientData);
        try {
            for (Map.Entry<String, String> entry : body.entrySet()) {
                data.put(entry.getKey(), entry.getValue());
            }
            data.put("organizeId", organizeId);
            cxPost("/webtracking", data);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("ConnectxSDK", "JSONException in cxTracking: " + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("ConnectxSDK", "IOException in cxTracking: " + e.getMessage());
        }
    }

    public void cxIdentify(String key, Map<String, Object> customers, Map<String, Object> tracking, Map<String, Object> form, Map<String, Object> options) {
        JSONObject data = new JSONObject();
        try {
            data.put("key", key);
            data.put("customers", new JSONObject(customers));
            JSONObject trackingData = new JSONObject(tracking);
            trackingData.put("organizeId", organizeId);
            trackingData.put("cx_fingerprint", Build.SERIAL);
            trackingData.put("os", "Android");
            data.put("tracking", trackingData);
            data.put("form", form != null ? new JSONObject(form) : null);
            data.put("options", new JSONObject(options));
            cxPost("/webtracking/dropform", data);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("ConnectxSDK", "JSONException in cxIdentify: " + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("ConnectxSDK", "IOException in cxIdentify: " + e.getMessage());
        }
    }

    public void cxCreateObject(String objectName, List<Map<String, Object>> bodies) {
        // Create a TypeToken for the List<Map<String, Object>> type
        Type type = new TypeToken<List<Map<String, Object>>>(){}.getType();

        // Convert the List<Map<String, Object>> to a JSON string using Gson
        Gson gson = new Gson();
        String bodiesJson = gson.toJson(bodies, type);

        JSONObject data = new JSONObject();
        try {
            data.put("objects", new JSONObject(bodiesJson));  // Convert to JSONObject
            cxPost("/object/" + objectName + "/composite", data);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("ConnectxSDK", "JSONException in cxCreateObject: " + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("ConnectxSDK", "IOException in cxCreateObject: " + e.getMessage());
        }
    }
}

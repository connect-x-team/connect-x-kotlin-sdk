package com.connectx.connectxsdk;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.StrictMode;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ConnectXManager {

    private static ConnectXManager instance;
    private String token;
    private String organizeId;
    private String userAgent;
    private String cookie;

    private static Context userContext;

    private String API_DOMAIN = "https://backend.connect-x.tech/connectx/api";
    private String GENERATE_COOKIE_URL = "https://backend.connect-x.tech/connectx/api/webtracking/generateCookie";

    private OkHttpClient client;

    private ConnectXManager(Context context) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        userContext = context;
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

    public static synchronized ConnectXManager getInstance(Context context) {
        if (instance == null) {
            instance = new ConnectXManager(context);
        }
        return instance;
    }

    public void initialize(String token, String organizeId, String env) {
        if (token.isEmpty()) {
            throw new IllegalArgumentException("Token must not be empty.");
        }
        if (organizeId.isEmpty()) {
            throw new IllegalArgumentException("Organize ID must not be empty.");
        }

        this.token = token;
        this.organizeId = organizeId;
//        this.env = env;

        if (env != null && !env.trim().isEmpty()) {
            this.API_DOMAIN = "https://backend-" + env + ".connect-x.tech/connectx/api";
            this.GENERATE_COOKIE_URL = this.API_DOMAIN + "/webtracking/generateCookie";
        }

        // Get cookie (This could be handled via a network request)
        this.cookie = getUnknownIdFromServer();
    }

    public String getUnknownIdFromServer() {
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

    private static String getDeviceType() {
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        float screenWidthDp = metrics.widthPixels / metrics.density;

        if (screenWidthDp < 600) {
            return "Mobile";
        } else {
            return "Tablet";
        }
    }

    private static Map<String, String> getNetworkType(Context context) {
        Map<String, String> result = new HashMap<>();

        ConnectivityManager connectivityManager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager == null) {
            result.put("label", "Other");
            result.put("value", "other");
            return result;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Network network = connectivityManager.getActiveNetwork();
            if (network != null) {
                NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
                if (capabilities != null) {
                    if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                        result.put("label", "WiFi");
                        result.put("value", "wifi");
                        return result;
                    } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                        result.put("label", "Cellular");
                        result.put("value", "cellular");
                        return result;
                    } else {
                        result.put("label", "Other");
                        result.put("value", "other");
                        return result;
                    }
                }
            }
        } else {
            NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
            if (activeNetwork != null) {
                if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                    result.put("label", "WiFi");
                    result.put("value", "wifi");
                    return result;
                } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                    result.put("label", "Cellular");
                    result.put("value", "cellular");
                    return result;
                } else {
                    result.put("label", "Other");
                    result.put("value", "other");
                    return result;
                }
            }
        }

        result.put("label", "Other");
        result.put("value", "other");
        return result;
    }

    private String getAppName() {
        return userContext.getPackageManager().getApplicationLabel(userContext.getApplicationInfo()).toString();
    }

    private String getAppVersion() {
        try {
            PackageInfo packageInfo = userContext.getPackageManager().getPackageInfo(userContext.getPackageName(), 0);
            return packageInfo.versionName; // e.g., "1.0.0"
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("ConnectXManager", "App version not found: " + e.getMessage());
            return "Unknown Version"; // Fallback if version not found
        }
    }

    private String getAppBuildVersion() {
        try {
            PackageInfo packageInfo = userContext.getPackageManager().getPackageInfo(userContext.getPackageName(), 0);
            long versionCode;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) { // API 28+
                versionCode = packageInfo.getLongVersionCode();
            } else {
                versionCode = packageInfo.versionCode; // Deprecated, but works for API <28
            }

            return String.valueOf(versionCode);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("ConnectXManager", "App build version not found: " + e.getMessage());
            return "-1"; // Fallback if build version not found
        }
    }


    public static String getDeviceId(Context context) {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    private Map<String, Object> getClientData() {
        Map<String, Object> clientData = new HashMap<>();
        clientData.put("cx_isBrowser", "false");
        clientData.put("cx_language", Locale.getDefault().getLanguage());
        clientData.put("cx_browserName", "");
        clientData.put("cx_browserVersion", "");
        clientData.put("cx_engineName", "Android");
        clientData.put("cx_engineVersion", Integer.toString(Build.VERSION.SDK_INT));
        clientData.put("cx_userAgent", userAgent);
        clientData.put("cx_source", getAppName());
        clientData.put("cx_type", "Mobile App");
        clientData.put("cx_deviceType", getDeviceType());
        clientData.put("cx_networkType", getNetworkType(userContext));
        clientData.put("cx_appVersion", getAppVersion());
        clientData.put("cx_appBuild", getAppBuildVersion());
        clientData.put("cx_libraryVersion", "1.0.9");
        clientData.put("cx_libraryPlatform", "Android");
        clientData.put("cx_fingerprint", getDeviceId(userContext));
        clientData.put("cx_deviceId", getDeviceId(userContext));
        clientData.put("cx_device", Build.PRODUCT);
        clientData.put("cx_deviceManufacturer", Build.MANUFACTURER);
        clientData.put("cx_os", "Android");
        clientData.put("cx_osVersion", Build.VERSION.RELEASE);
        clientData.put("device", Build.MODEL);
        return clientData;
    }

    private void cxPost(String endpoint, String data) throws IOException {
        RequestBody body = RequestBody.create(data, MediaType.parse("application/json"));
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

    public void trackEvent(Map<String, String> body) {
        Map<String, Object> clientData = getClientData();
        JSONObject data = new JSONObject(clientData);
        try {
            for (Map.Entry<String, String> entry : body.entrySet()) {
                data.put(entry.getKey(), entry.getValue());
            }
            data.put("organizeId", organizeId);
            cxPost("/webtracking", data.toString());
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("ConnectxManager", "JSONException in cxTracking: " + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("ConnectxManager", "IOException in cxTracking: " + e.getMessage());
        }
    }

    public void identify(Map<String, Object> body) {
        JSONObject data = new JSONObject();
        try {
            data.put("key", body.get("key"));
            data.put("customers", new JSONObject((Map) body.get("customers")));
            JSONObject trackingData = new JSONObject(getClientData());
            if (body.containsKey("tracking") && body.get("tracking") instanceof Map) {
                JSONObject trackingBody = new JSONObject((Map<?, ?>) body.get("tracking"));

                // Merge trackingBody into trackingData
                Iterator<String> keys = trackingBody.keys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    trackingData.put(key, trackingBody.get(key)); // Overwrite or add new fields
                }
            }
            trackingData.put("organizeId", organizeId);
            data.put("tracking", trackingData);
            data.put("form", body.get("form") != null ? new JSONObject((Map) body.get("form")) : null);
            data.put("options", body.get("options") != null ?  new JSONObject((Map) body.get("options")): null);
            cxPost("/webtracking/dropform", data.toString());
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("ConnectxManager", "JSONException in cxIdentify: " + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("ConnectxManager", "IOException in cxIdentify: " + e.getMessage());
        }
    }

    public void createRecord(String objectName, List<Map<String, Object>> bodies) {
        // Create a TypeToken for the List<Map<String, Object>> type
        Type type = new TypeToken<List<Map<String, Object>>>() {}.getType();

        // Convert the List<Map<String, Object>> to a JSON string using Gson
        Gson gson = new Gson();
        String bodiesJson = gson.toJson(bodies, type);

        try {
            // Convert the JSON string to a JSONArray
            JSONArray objectsArray = new JSONArray(bodiesJson);


            // Make the POST request
            cxPost("/object/" + objectName + "/composite", objectsArray.toString());
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("ConnectxManager", "JSONException in createRecord: " + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("ConnectxManager", "IOException in createRecord: " + e.getMessage());
        }
    }


    public void openTicket(Map<String, Object> body) {
        JSONObject data = new JSONObject();
        try {
            // Add key
            data.put("key", body.get("key"));

            // Add customers
            data.put("customers", new JSONObject((Map) body.get("customers")));

            // Add ticket with additional organizeId
            JSONObject ticket = new JSONObject((Map) body.get("ticket"));
            ticket.put("organizeId", organizeId);
            data.put("ticket", ticket);

            // Add tracking data with additional organizeId
            JSONObject tracking = new JSONObject(getClientData());
            tracking.put("organizeId", organizeId);
            data.put("tracking", tracking);

            // Add lead
            data.put("lead", new JSONObject((Map) body.get("lead")));

            // Add customs (if present)
            if (body.containsKey("customs")) {
                data.put("customs", body.get("customs"));
            }

            // Perform the POST request
            cxPost("/webtracking/dropformOpenTicket", data.toString());

            Log.d("ConnectXManager", "Ticket opened successfully!");
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("ConnectXManager", "JSONException in cxOpenTicket: " + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("ConnectXManager", "IOException in cxOpenTicket: " + e.getMessage());
        }
    }

    // Helper methods for easier usage

    public static void initialize(Context context, String token, String organizeId) {
        getInstance(context).initialize(token, organizeId, null);
    }

    public static void initialize(Context context, String token, String organizeId, String env) {
        getInstance(context).initialize(token, organizeId, env);
    }

    public static void cxTracking(Map<String, String> trackingData) {
        getInstance(null).trackEvent(trackingData);
    }

    public static void cxIdentify(Map<String, Object> body) {
        getInstance(null).identify(body);
    }

//    public static void cxIdentify(String key, Map<String, Object> customers, Map<String, Object> tracking) {
//        getInstance(null).identify(key, customers, tracking, null, null);
//    }
//
//    public static void cxIdentify(String key, Map<String, Object> customers, Map<String, Object> tracking, Map<String, Object> form, Map<String, Object> options) {
//        getInstance(null).identify(key, customers, tracking, form, options);
//    }

    public static void cxCreateRecord(String objectName, List<Map<String, Object>> bodies) {
        getInstance(null).createRecord(objectName, bodies);
    }

    public static void cxOpenTicket(Map<String, Object> bodies) {
        getInstance(null).openTicket(bodies);
    }

    public static String getUnknownId() {
        return getInstance(null).getUnknownIdFromServer();
    }
}


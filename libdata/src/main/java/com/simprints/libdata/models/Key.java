package com.simprints.libdata.models;

import android.content.Context;
import android.support.annotation.NonNull;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.simprints.libdata.models.realm.rl_ApiKey;
import com.simprints.libdata.network.NetworkRequestCallback;
import com.simprints.libdata.network.NetworkRequestQueue;

import org.json.JSONException;
import org.json.JSONObject;

import io.realm.Realm;

public class Key {

    public String apiKey;
    public Status status;
    public String token;
    public String userId;
    public String androidId;
    public String moduleId;

//    public Key() {
//    }

//    public Key(String apiKey) {
//        this.apiKey = apiKey;
//        this.status = Status.UNVERIFIED;
//    }

    public Key(@NonNull String apiKey, @NonNull String userId, @NonNull String moduleId, @NonNull String androidId) {
        this.apiKey = apiKey;
        this.userId = userId;
        this.moduleId = moduleId;
        this.androidId = androidId;
        this.status = Status.UNVERIFIED;
    }

    /**
     * Checks the validity of this api key with the remote server,
     * sets the status of this api key accordingly then
     * calls the method of the specified callback
     *
     * @param appContext Context to use to perform the network request
     * @param authUrl    Url to use to validate the key
     * @param realm      Realm where to save the status of the api key
     * @param callback   The onResult or onError methods of this callback are called
     *                   asynchronously to notify the caller of the result.
     */
    public void validate(@NonNull final Context appContext, @NonNull final String authUrl,
                         @NonNull final Realm realm, @NonNull final NetworkRequestCallback callback)
    {
        // Builds the json body of the validation request
        JSONObject json = new JSONObject();
        try {
            json.put("apiKey", apiKey);
        } catch (JSONException ignored) {
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, authUrl, json,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Key.this.token = response.getString("token");
                        } catch (JSONException e) {
                            callback.onResult(0);
                        }
                        Key.this.status = Status.VALID;
                        rl_ApiKey.save(realm, apiKey, true);
                        callback.onResult(200);
                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError e) {
                        if (e.networkResponse != null) {
                            switch (e.networkResponse.statusCode) {
                                case 401:
                                    Key.this.status = Status.INVALID;
                                    rl_ApiKey.save(realm, apiKey, false);
                                    break;
                            }
                        }
                        callback.onError(e);
                    }
                }
        );

        NetworkRequestQueue.getInstance(appContext).add(request);
    }


    public enum Status {
        UNVERIFIED(),
        VALID(),
        INVALID()
    }
}

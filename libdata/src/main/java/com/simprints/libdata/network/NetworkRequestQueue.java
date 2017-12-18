package com.simprints.libdata.network;

import android.content.Context;
import android.support.annotation.NonNull;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.HttpStack;
import com.android.volley.toolbox.Volley;

public class NetworkRequestQueue {

    private static NetworkRequestQueue instance;
    private static Context context;
    private RequestQueue requestQueue;

    private NetworkRequestQueue(@NonNull Context context)
    {
        NetworkRequestQueue.context = context;
        requestQueue = null;
    }

    public static synchronized NetworkRequestQueue getInstance(@NonNull Context context)
    {
        if (instance == null) {
            instance = new NetworkRequestQueue(context);
        }
        return instance;
    }

    public <T> void add(Request<T> req)
    {
        if (requestQueue == null) {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            requestQueue = Volley.newRequestQueue(context.getApplicationContext());
        }
        requestQueue.add(req);
    }

    /**
     * Replaces the default request queue by one backed by the specified http stack,
     * to allow debugging / testing easily
     * @param mockHttpStack http stack to use
     */
    public void enableDebugMode(HttpStack mockHttpStack)
    {
        requestQueue = Volley.newRequestQueue(context.getApplicationContext(), mockHttpStack);
    }
}

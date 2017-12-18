package com.simprints.libdata.network;

import com.android.volley.NoConnectionError;
import com.android.volley.VolleyError;

public abstract class NetworkRequestCallback {

    public void onError(VolleyError e)
    {
        if (e instanceof NoConnectionError) {
            onResult(-1);
        } else if (e.networkResponse != null) {
            onResult(e.networkResponse.statusCode);
        } else {
            onResult(0);
        }
    }

    /**
     * @param statusCode
     * > 0 -> http status code
     * 0 -> volley could not extract the status code,
     * usually means there was an error while processing the response
     * -1 -> no connection
     */
    public abstract void onResult(int statusCode);

}

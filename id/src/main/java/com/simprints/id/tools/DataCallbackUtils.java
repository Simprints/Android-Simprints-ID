package com.simprints.id.tools;

import android.os.Handler;
import android.os.Looper;

import com.simprints.id.data.db.DATA_ERROR;
import com.simprints.id.data.db.DataCallback;

import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import timber.log.Timber;


public final class DataCallbackUtils {

    public static void log(String s) {
        Timber.tag("libdata").d(s);
    }

    public static DataCallback wrapCallback(@NonNull final String callDescription, @Nullable final DataCallback callback) {
        return new DataCallback() {
            @Override
            public void onSuccess(final boolean isDataFromRemote) {
                log(String.format(Locale.UK, "%s -> success", callDescription));
                // Call back on UI thread
                if (callback != null)
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onSuccess(isDataFromRemote);
                        }
                    });
            }

            @Override
            public void onFailure(final DATA_ERROR error) {
                log(String.format(Locale.UK, "%s -> failure (%s)", callDescription, error.name()));
                // Call back on UI thread
                if (callback != null)
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onFailure(error);
                        }
                    });
            }
        };
    }

}

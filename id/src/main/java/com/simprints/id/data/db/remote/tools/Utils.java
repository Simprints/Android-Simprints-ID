package com.simprints.id.data.db.remote.tools;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.FirebaseDatabase;
import com.simprints.id.data.db.DATA_ERROR;
import com.simprints.id.data.db.DataCallback;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import timber.log.Timber;


public final class Utils {

    private final static SimpleDateFormat DATE_FORMAT;

    static {
        DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SSS'Z'", Locale.UK);
        DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    private static Map<FirebaseApp, FirebaseDatabase> firebaseDatabases = new HashMap<>();

    public static FirebaseDatabase getDatabase(@Nullable FirebaseApp firebaseApp) {
        FirebaseDatabase fbDb = firebaseDatabases.get(firebaseApp);
        if (fbDb == null) {
            if (firebaseApp == null) {
                fbDb = FirebaseDatabase.getInstance();
            } else {
                fbDb = FirebaseDatabase.getInstance(firebaseApp);
            }
            try {
                fbDb.setPersistenceEnabled(false);
            } catch (DatabaseException ignored) {
                Timber.tag("DatabaseException").d("PERSISTENCE FAILED");
            }
            firebaseDatabases.put(firebaseApp, fbDb);
        }
        return fbDb;
    }

    public static void forceSync(FirebaseApp app) {
        // Making a write forces syncing if we are online
        Routes.INSTANCE.junkRef(app).setValue(true);
    }

    /**
     * Wrapper around {@link String#format(Locale, String, Object...)} with
     * {@link Locale#UK} as first argument
     */
    public static String format(String formatString, Object... formatArgs) {
        return String.format(Locale.UK, formatString, formatArgs);
    }

    /**
     * @return The current date
     */
    public static Date now() {
        return Calendar.getInstance().getTime();
    }

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

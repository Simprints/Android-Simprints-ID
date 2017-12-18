package com.simprints.libdata.models.sql_OLD;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.annotation.NonNull;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.Configuration;
import com.simprints.libdata.DATA_ERROR;
import com.simprints.libdata.DataCallback;
import com.simprints.libdata.models.Key;
import com.simprints.libdata.models.firebase.fb_Person;
import com.simprints.libdata.models.realm.rl_ApiKey;
import com.simprints.libdata.models.realm.rl_Person;
import com.simprints.libdata.tools.Constants;

import io.realm.Realm;

public class MigrationTask extends AsyncTask<Void, Void, Void> {

    private final Context context;
    private final String databaseName;
    private final int databaseVersion;
    private final DataCallback callback;

    public MigrationTask(@NonNull Context context, @NonNull String databaseName, int databaseVersion, @NonNull DataCallback callback) {
        this.context = context;
        this.databaseName = databaseName;
        this.databaseVersion = databaseVersion;
        this.callback = callback;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        try {
            @SuppressWarnings("unchecked")
            Configuration dbConfig = new Configuration.Builder(context)
                    .setDatabaseName(databaseName)
                    .setDatabaseVersion(databaseVersion)
                    .setModelClasses(
                            M_ApiKey.class,
                            M_Fingerprint.class,
                            M_IdEvent.class,
                            M_Match.class,
                            M_Person.class,
                            M_Session.class,
                            M_SyncOp.class)
                    .create();
            ActiveAndroid.initialize(dbConfig);
        } catch (Exception ignored) {
        }

        @SuppressLint("HardwareIds")
        String deviceId = Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);


        for (M_ApiKey key : M_ApiKey.getAll()) {
            Key metaKey = new Key(key.asString(), Constants.GLOBAL_ID, Constants.GLOBAL_ID, deviceId);

            Realm realm = Realm.getDefaultInstance();
            realm.beginTransaction();

            //Transfer API Key
            rl_ApiKey saveKey = new rl_ApiKey();
            saveKey.apiKey = key.asString();
            switch (key.getStatus()) {
                case UNVERIFIED:
                    break;
                case VALID:
                    saveKey.valid = true;
                    break;
                case INVALID:
                    saveKey.valid = false;
                    break;
            }
            realm.copyToRealmOrUpdate(saveKey);

            //Transfer People
            for (M_Person person : M_Person.getAll(key)) {
                if (!person.load().getFingerprints().isEmpty()) {
                    fb_Person fbPerson = new fb_Person(person.load(), metaKey);
                    realm.copyToRealmOrUpdate(new rl_Person(fbPerson));
                    person.delete();
                }
            }

            realm.commitTransaction();
            realm.close();
        }

        try {
            ActiveAndroid.dispose();
            context.deleteDatabase(databaseName);
        } catch (Exception ignored) {
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        callback.onFailure(DATA_ERROR.DATABASE_INIT_RESTART);
    }
}

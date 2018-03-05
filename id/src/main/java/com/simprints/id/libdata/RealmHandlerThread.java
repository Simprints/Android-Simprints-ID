package com.simprints.id.libdata;

import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;

import com.simprints.id.libdata.models.realm.RealmConfig;
import com.simprints.id.libdata.models.realm.rl_Person;
import com.simprints.id.libdata.tools.Utils;

import java.util.List;

import io.realm.Realm;

public class RealmHandlerThread extends HandlerThread {

    private final String apiKey;
    private Handler handler;
    private Realm realm;

    public RealmHandlerThread(@NonNull String apiKey) {
        super("Realm Handler Thread");
        this.apiKey = apiKey;
    }

    @Override
    protected void onLooperPrepared() {
        handler = new Handler(getLooper());
        realm = Realm.getInstance(RealmConfig.get(apiKey));
    }

    /**
     * Does not save person without fingerprints (only use them to eventually update other attributes
     * of the person such as the userId)
     */
    public void savePersonAsync(@NonNull final List<rl_Person> persons, @NonNull final Runnable onCompletion) {
        while (handler == null);
        handler.post(new Runnable() {
            @Override
            public void run() {
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        for (rl_Person person : persons) {
                            if (person.fingerprints.isEmpty()) {
                                rl_Person localPerson = realm.where(rl_Person.class).equalTo("patientId", person.patientId).findFirst();
                                if (localPerson == null)
                                    continue;
                                Utils.log("SavePersonAsync: this person has no fingerprints, only updating other fields");
                                person.fingerprints = localPerson.fingerprints;
                            }
                            realm.copyToRealmOrUpdate(person);
                        }
                    }
                });
                onCompletion.run();
            }
        });
    }

    public void postTask(Runnable task){
        handler.post(task);
    }

    public void close() {
        while (handler == null);
        handler.post(new Runnable() {
            @Override
            public void run() {
                realm.close();
                RealmHandlerThread.this.quit();
            }
        });
    }
}

package com.simprints.libdata.databaseResolvers;

import android.content.ContentResolver;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.simprints.libcommon.Fingerprint;
import com.simprints.libcommon.Person;
import com.simprints.libdata.BufferedPersonSaver;
import com.simprints.libdata.DataCallback;
import com.simprints.libdata.models.Key;
import com.simprints.libdata.models.realm.rl_Person;
import com.simprints.libdata.tools.Utils;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Semaphore;

public abstract class DatabaseResolver {


    /**
     * <o>Queries an implementation dependent content provider (e.g. CommCare)
     * to update the specified realm</p>
     * <p>
     * <p>The update follows the following rules:
     * <ul>
     * <li>Persons are uniquely identified by their guid.</li>
     * <p>
     * <li>If in the content provider data set a person has two fingerprints
     * for the same finger, then only the highest quality one is considered.</li>
     * <p>
     * <li>A person present in the content provider data set but not in the Simprints'
     * database context is added to the latter with its fingerprints.</li>
     * <p>
     * <li>A person present in the content provider data set and in the Simprint's
     * database context is updated, by merging the two sets of fingerprints, keeping
     * only the highest quality fingerprint for each finger.</li>
     * </ul>
     * </p>
     *
     * @param resolver      Content resolver to use to query the content provider
     * @param providerGuids When not null, this set gets filled with the guids of the persons
     *                      contained in the content provider data set
     * @param callback      The {@link DataCallback#onSuccess()} onSuccess}
     *                      method is called on finish.
     */
    public void resolve(@NonNull final ContentResolver resolver,
                        @NonNull final Key key,
                        @Nullable final Set<String> providerGuids,
                        @NonNull final DataCallback callback) {

        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... voids) {
                List<String> guids = getGuids(key.userId, resolver);

                final BufferedPersonSaver saver = new BufferedPersonSaver(key.apiKey);

                for (String guid : guids) {
                    Utils.log(String.format("guid -> %s", guid));
                    List<Fingerprint> prints = getFingerprints(guid, resolver);

                    saver.save(new rl_Person(new Person(guid, prints), key));

                    if (providerGuids != null)
                        providerGuids.add(guid);
                }

                final Semaphore waitForFlush = new Semaphore(0);

                saver.flush(new Runnable() {
                    @Override
                    public void run() {
                        callback.onSuccess();
                        waitForFlush.release();
                    }
                });

                waitForFlush.acquireUninterruptibly();

                return null;
            }
        }.execute();
    }


    /**
     * @return The read permission to check before querying the provider
     */
    @NonNull
    public abstract String getReadPermission();


    /**
     * @param resolver Resolver to use to query the content provider
     * @return A list of the guids of all the person of the content provider data set
     */
    @NonNull
    protected abstract List<String> getGuids(@NonNull String userId, @NonNull ContentResolver resolver);


    /**
     * @param resolver Resolver to use to query the content provider
     * @param guid     Guid of the fingerprints owner
     * @return A list of the fingerprints of a given person of the content provider data set
     */
    @NonNull
    protected abstract List<Fingerprint> getFingerprints(@NonNull String guid,
                                                         @NonNull ContentResolver resolver);


}

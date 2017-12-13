package com.simprints.libdata;

import android.support.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.simprints.libdata.models.firebase.fb_Person;
import com.simprints.libdata.models.realm.rl_Person;
import com.simprints.libdata.tools.Routes;

import java.util.ArrayList;
import java.util.List;

public class BufferedPersonSaver {

    private final static int BUFFER_SIZE = 50;

    private RealmHandlerThread thread;
    private List<rl_Person> buffer;
    private Runnable completionCallback;

    private int pendingDownloads;
    private int pendingSaves;

    public BufferedPersonSaver(@NonNull String apiKey) {
        thread = new RealmHandlerThread(apiKey);
        thread.start();
        thread.getLooper(); // this blocks until it is safe to use the thread (initialization time)
        buffer = new ArrayList<>(BUFFER_SIZE);
        completionCallback = null;
        pendingDownloads = 0;
        pendingSaves = 0;
    }

    public synchronized void saveFromFirebase(@NonNull DatabaseReference ref, @NonNull String guid) {
        pendingDownloads++;
        ref.child(Routes.patientNode(guid)).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(final DataSnapshot dataSnapshot) {
                        final fb_Person person = dataSnapshot.getValue(fb_Person.class);
                        handleDownloaded(person);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                }
        );
    }

    private synchronized void handleDownloaded(@NonNull fb_Person person) {
        pendingDownloads--;
        save(new rl_Person(person));
    }

    public synchronized void save(@NonNull rl_Person person) {
        buffer.add(person);
        // The buffer is flushed either when it is full, or when flush() has been called and
        // there are no download left
        if (buffer.size() == BUFFER_SIZE || (completionCallback != null && pendingDownloads == 0))
            saveNow();
    }

    public synchronized void flush(@NonNull Runnable callback) {
        if (pendingDownloads == 0 && pendingSaves == 0)
            finish(callback);
        else
            completionCallback = callback;
    }

    private synchronized void confirmSave() {
        pendingSaves--;

        if (completionCallback != null && pendingDownloads == 0 && pendingSaves == 0)
            finish(completionCallback);
    }

    private synchronized void saveNow() {
        pendingSaves++;

        // Swap the buffer reference with a new empty buffer
        final List<rl_Person> toSave = buffer;
        buffer = new ArrayList<>(BUFFER_SIZE);

        // Call saveAsync on the handler thread
        thread.savePersonAsync(toSave, new Runnable() {
            @Override
            public void run() {
                BufferedPersonSaver.this.confirmSave();
            }
        });
    }

    private synchronized void finish(@NonNull Runnable callback) {
        thread.close();
        thread = null;
        buffer = null;
        completionCallback = null;
        callback.run();
    }

}

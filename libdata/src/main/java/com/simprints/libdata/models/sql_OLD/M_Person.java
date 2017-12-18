package com.simprints.libdata.models.sql_OLD;

import android.support.annotation.NonNull;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;
import com.simprints.libcommon.Fingerprint;
import com.simprints.libcommon.Person;

import java.util.ArrayList;
import java.util.List;

@Table(name = "People")
public class M_Person extends Model {

    @Column(name = "RemoteId")
    private int remoteId;

    @Column(name = "Guid", unique = true, notNull = true)
    public String guid;

    @Column(name = "CreatedAt")
    private Long createdAt;

    @Column(name = "IsProbe")
    private boolean isProbe;

    @Column(name = "IsSynced")
    private boolean isSynced;


    @Column(name = "ApiKey",
            onUpdate = Column.ForeignKeyAction.CASCADE,
            onDelete = Column.ForeignKeyAction.CASCADE,
            notNull = true)
    public M_ApiKey mApiKey;

    public M_Person() {
        super();
    }

    /**
     * @param mApiKey An api key
     * @return A newly allocated list of the persons linked to the specified api key
     * (excepted probes)
     */
    @NonNull
    public static List<M_Person> getAll(@NonNull M_ApiKey mApiKey) {
        List<M_Person> mPersons = new Select()
                .from(M_Person.class)
                .where("ApiKey = ?", mApiKey.getId())
                .where("IsProbe = ?", false)
                .execute();

        return mPersons != null ? mPersons : new ArrayList<M_Person>();
    }

    /**
     * @param mApiKey An api key
     * @return A newly allocated list of the unsynced persons linked to the specified api key
     */
    @NonNull
    public static List<M_Person> getAllUnsynced(@NonNull M_ApiKey mApiKey) {
        List<M_Person> mPersons = new Select()
                .from(M_Person.class)
                .where("ApiKey = ?", mApiKey.getId())
                .where("IsSynced = ?", false)
                .where("IsProbe = ?", false)
                .execute();

        return mPersons != null ? mPersons : new ArrayList<M_Person>();
    }

    /**
     * @param mApiKey An api key
     * @param guid    The global unique Id
     * @return A person of the local database if existing, null else
     */
    public static M_Person get(@NonNull M_ApiKey mApiKey, @NonNull String guid) {
        return new Select()
                .from(M_Person.class)
                .where("ApiKey = ?", mApiKey.getId())
                .where("Guid = ?", guid)
                .executeSingle();
    }

    public Person load() {
        List<Fingerprint> prints = new ArrayList<>();
        for (M_Fingerprint mPrint : M_Fingerprint.get(this)) {
            prints.add(mPrint.load());
        }
        return new Person(guid, prints);
    }

    @NonNull
    public String getGuid() {
        return guid;
    }
}
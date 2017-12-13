package com.simprints.libdata.models.sql_OLD;

import android.support.annotation.NonNull;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import java.util.ArrayList;
import java.util.List;

@Table(name = "IdentificationEvents")
public class M_IdEvent extends Model {

    @Column(name = "Probe")
    private M_Person mProbe;

    @Column(name = "SelectedMatchGuid")
    private String selectedMatchGuid;

    @Column(name = "Date")
    private Long date;

    @Column(name = "Key",
            onUpdate = Column.ForeignKeyAction.CASCADE,
            onDelete = Column.ForeignKeyAction.CASCADE)
    private M_ApiKey mApiKey;

    // Db accessors

    public M_IdEvent() {
        super();
    }

    public static
    @NonNull
    List<M_IdEvent> getAll(@NonNull M_ApiKey mApiKey) {
        List<M_IdEvent> mIdEvents = new Select()
                .from(M_IdEvent.class)
                .where("Key = ?", mApiKey.getId())
                .execute();
        return mIdEvents != null ? mIdEvents : new ArrayList<M_IdEvent>();
    }
}


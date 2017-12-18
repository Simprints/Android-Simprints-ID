package com.simprints.libdata.models.sql_OLD;

import android.support.annotation.NonNull;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import java.util.ArrayList;
import java.util.List;

@Table(name = "Matches")
public class M_Match extends Model {

    @Column(name = "Guid")
    private String guid;

    @Column(name = "Score")
    private int score;

    @Column(name = "IdentificationEvent")
    private M_IdEvent mIdEvent;

    // DB accessors
    public M_Match() {
        super();
    }

    public static
    @NonNull
    List<M_Match> get(@NonNull M_IdEvent mIdEvent) {
        List<M_Match> matches = new Select()
                .from(M_Match.class)
                .where("IdentificationEvent = ?", mIdEvent.getId())
                .execute();
        return matches != null ? matches : new ArrayList<M_Match>();
    }
}

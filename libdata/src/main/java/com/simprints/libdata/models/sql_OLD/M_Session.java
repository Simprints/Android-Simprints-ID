package com.simprints.libdata.models.sql_OLD;

import android.support.annotation.NonNull;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import java.util.ArrayList;
import java.util.List;

@Table(name = "Sessions")
public class M_Session extends Model {

    @Column(name = "MacAddress")
    private String macAddress;

    @Column(name = "DeviceId")
    private String deviceId;

    @Column(name = "UserId")
    private String userId;

    @Column(name = "Enrol")
    private boolean enrol;

    @Column(name = "StartTime")
    private Long startTime;

    @Column(name = "EndTime")
    private Long endTime;

    @Column(name = "Latitude")
    private String latitude;

    @Column(name = "Longitude")
    private String longitude;

    @Column(name = "PersonGuid")
    private String personGuid;

    @Column(name = "HardwareVersion")
    private int hardwareVersion;

    @Column(name = "Key",
            onUpdate = Column.ForeignKeyAction.CASCADE,
            onDelete = Column.ForeignKeyAction.CASCADE)
    private M_ApiKey mApiKey;

    // DB accessor

    /**
     * @param mApiKey An api key
     * @return A newly allocated list of the sessions linked to the specified api key
     */
    public static List<M_Session> getAll(@NonNull M_ApiKey mApiKey) {
        List<M_Session> mSessions = new Select()
                .from(M_Session.class)
                .where("Key = ?", mApiKey.getId())
                .execute();

        return mSessions != null ? mSessions : new ArrayList<M_Session>();
    }

    // Instance methods

    public M_Session() {
        super();
    }

}

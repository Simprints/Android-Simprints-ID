package com.simprints.id.data.db.remote.adapters

import com.google.firebase.database.ServerValue
import com.simprints.id.data.models.Session
import com.simprints.libdata.models.firebase.fb_Session

fun Session.toFirebaseSession(): fb_Session =
    with(this) {
        fb_Session(sessionId,
            androidSdkVersion,
            deviceModel,
            deviceId,
            appVersionName,
            libVersionName,
            calloutAction,
            projectId,
            moduleId,
            userId,
            patientId,
            callingPackage,
            metadata,
            resultFormat,
            macAddress,
            scannerId,
            hardwareVersion,
            latitude,
            longitude,
            msSinceBootOnLoadEnd - msSinceBootOnSessionStart,
            msSinceBootOnMainStart - msSinceBootOnSessionStart,
            msSinceBootOnMatchStart - msSinceBootOnSessionStart,
            msSinceBootOnSessionEnd - msSinceBootOnSessionStart,
            ServerValue.TIMESTAMP)
    }

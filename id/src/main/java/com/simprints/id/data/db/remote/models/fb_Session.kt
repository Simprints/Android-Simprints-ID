package com.simprints.id.data.db.remote.models


data class fb_Session(

    val sessionId: String = "",

    val androidSdkVersion: Int = -1,
    val deviceModel: String = "",
    val deviceId: String = "",
    val appVersionName: String = "",
    val libVersionName: String = "",

    val calloutAction: String = "",
    val projectId: String = "",
    val moduleId: String = "",
    val userId: String = "",
    val patientId: String = "",
    val callingPackage: String = "",
    val metadata: String = "",
    val resultFormat: String = "",

    val macAddress: String = "",
    val scannerId: String = "",
    val hardwareVersion: Int = -1,

    val latitude: String = "",
    val longitude: String = "",

    val sessionStartToLoadEndMs: Long = -1,
    val sessionStartToMainStart: Long = -1,
    val sessionStartToMatchStart: Long = -1,
    val sessionStartToSessionEnd: Long = -1,
    val serverSessionEndTime: Map<String, String>? = null

)

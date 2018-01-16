package com.simprints.id.data.models

data class Session(

    val sessionId: String,

    val androidSdkVersion: Int,
    val deviceModel: String,
    val deviceId: String,
    val appVersionName: String,
    val libVersionName: String,

    val calloutAction: String,
    val apiKey: String,
    val moduleId: String,
    val userId: String,
    val patientId: String,
    val callingPackage: String,
    val metadata: String,
    val resultFormat: String,

    val macAddress: String,
    val scannerId: String,
    val hardwareVersion: Int,

    val latitude: String,
    val longitude: String,

    var msSinceBootOnSessionStart: Long,
    var msSinceBootOnLoadEnd: Long,
    var msSinceBootOnMainStart: Long,
    var msSinceBootOnMatchStart: Long,
    var msSinceBootOnSessionEnd: Long

)

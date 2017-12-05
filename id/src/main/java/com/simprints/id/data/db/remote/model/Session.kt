package com.simprints.id.data.db.remote.model

import com.simprints.id.data.model.CalloutType
import java.util.*


data class Session(val sessionId: String = UUID.randomUUID().toString(),
                   val callout: String = CalloutType.INVALID_OR_MISSING.intentAction,
                   val apiKey: String = "",
                   val moduleId: String = "",
                   val userId: String = "",
                   val personGuid: String = "",
                   val metadata: String = "",
                   val deviceId: String = "",
                   val callingPackage: String = "",
                   val appVersion: String = "",
                   val phoneModel: String = "",
                   val macAddress: String = "",
                   val scannerId: String = "",
                   val hardwareVersion: Int = 0,
                   val latitude: String = "",
                   val longitude: String = "",
                   val loadEndOffsetMs: Long = 0,
                   val mainStartOffsetMs: Long = 0,
                   val matchStartOffsetMs: Long = 0,
                   val sessionEndOffsetMs: Long = 0,
                   val serverSessionEndTime: Map<String, String>)
package com.simprints.id.data.prefs.settings

import com.google.gson.JsonSyntaxException
import com.simprints.id.data.db.person.domain.FingerIdentifier
import com.simprints.id.data.prefs.settings.fingerprint.models.CaptureFingerprintStrategy
import com.simprints.id.data.prefs.settings.fingerprint.models.SaveFingerprintImagesStrategy
import com.simprints.id.data.prefs.settings.fingerprint.models.ScannerGeneration
import com.simprints.id.domain.GROUP
import com.simprints.id.domain.modality.Modality
import com.simprints.id.secure.models.SecurityState
import com.simprints.id.services.scheduledSync.people.master.models.PeopleDownSyncSetting
import com.simprints.id.tools.serializers.Serializer


interface SettingsPreferencesManager {

    var returnIdCount: Int
    var language: String
    var projectLanguages: Array<String>
    var moduleIdOptions: Set<String>
    var selectedModules: Set<String>
    var maxNumberOfModules: Int
    var syncGroup: GROUP
    var matchGroup: GROUP
    /** @throws JsonSyntaxException */
    var fingerStatus: Map<FingerIdentifier, Boolean>

    var programName: String
    var organizationName: String

    var logoExists: Boolean
    var consentRequired: Boolean
    var locationPermissionRequired: Boolean

    var modalities: List<Modality>
    var peopleDownSyncSetting: PeopleDownSyncSetting

    var fingerImagesExist: Boolean
    var captureFingerprintStrategy: CaptureFingerprintStrategy
    var saveFingerprintImagesStrategy: SaveFingerprintImagesStrategy
    var scannerGenerations: List<ScannerGeneration>
    var apiBaseUrl: String
    var securityStatus: SecurityState.Status

    var faceMaxRetries: Int
    var faceQualityThreshold: Float
    var faceNbOfFramesCaptured: Int
    var faceMatchThreshold: Float

    fun getRemoteConfigStringPreference(key: String): String
    fun <T: Any>getRemoteConfigComplexPreference(key: String, serializer: Serializer<T>): T
    fun getRemoteConfigFingerStatus(): Map<FingerIdentifier, Boolean>

}

package com.simprints.id.data.prefs.settings

import com.google.gson.JsonSyntaxException
import com.simprints.id.data.db.subject.domain.FingerIdentifier
import com.simprints.id.data.prefs.settings.fingerprint.models.CaptureFingerprintStrategy
import com.simprints.id.data.prefs.settings.fingerprint.models.SaveFingerprintImagesStrategy
import com.simprints.id.data.prefs.settings.fingerprint.models.ScannerGeneration
import com.simprints.id.domain.GROUP
import com.simprints.id.domain.modality.Modality
import com.simprints.id.services.scheduledSync.subjects.master.models.SubjectsDownSyncSetting
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
    var parentalConsentExists: Boolean
    var generalConsentOptionsJson: String
    var parentalConsentOptionsJson: String

    var logoExists: Boolean
    var consentRequired: Boolean
    var locationPermissionRequired: Boolean

    var enrolmentPlus: Boolean

    var modalities: List<Modality>
    var subjectsDownSyncSetting: SubjectsDownSyncSetting

    var fingerImagesExist: Boolean
    var captureFingerprintStrategy: CaptureFingerprintStrategy
    var saveFingerprintImagesStrategy: SaveFingerprintImagesStrategy
    var scannerGenerations: List<ScannerGeneration>
    var apiBaseUrl: String

    var faceMaxRetries: Int
    var faceQualityThreshold: Float
    var faceNbOfFramesCaptured: Int
    var faceMatchThreshold: Float

    fun getRemoteConfigStringPreference(key: String): String
    fun <T: Any>getRemoteConfigComplexPreference(key: String, serializer: Serializer<T>): T
    fun getRemoteConfigFingerStatus(): Map<FingerIdentifier, Boolean>

}

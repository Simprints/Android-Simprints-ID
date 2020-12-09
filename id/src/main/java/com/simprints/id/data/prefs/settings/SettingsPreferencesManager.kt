package com.simprints.id.data.prefs.settings

import com.simprints.id.data.db.subject.domain.FingerIdentifier
import com.simprints.id.data.prefs.settings.fingerprint.models.CaptureFingerprintStrategy
import com.simprints.id.data.prefs.settings.fingerprint.models.SaveFingerprintImagesStrategy
import com.simprints.id.data.prefs.settings.fingerprint.models.ScannerGeneration
import com.simprints.id.domain.GROUP
import com.simprints.id.domain.SyncLocationSetting
import com.simprints.id.domain.modality.Modality
import com.simprints.id.orchestrator.responsebuilders.FaceConfidenceThresholds
import com.simprints.id.orchestrator.responsebuilders.FingerprintConfidenceThresholds
import com.simprints.id.services.sync.events.master.models.EventDownSyncSetting
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

    var programName: String
    var organizationName: String
    var parentalConsentExists: Boolean
    var generalConsentOptionsJson: String
    var parentalConsentOptionsJson: String

    var logoExists: Boolean
    var consentRequired: Boolean
    var locationPermissionRequired: Boolean

    var isEnrolmentPlus: Boolean

    var modalities: List<Modality>
    var eventDownSyncSetting: EventDownSyncSetting
    var syncLocationSetting: SyncLocationSetting

    var fingerprintsToCollect: List<FingerIdentifier>
    var fingerImagesExist: Boolean
    var captureFingerprintStrategy: CaptureFingerprintStrategy
    var saveFingerprintImagesStrategy: SaveFingerprintImagesStrategy
    var scannerGenerations: List<ScannerGeneration>
    var fingerprintLiveFeedbackOn: Boolean
    var fingerprintQualityThreshold: Int
    var apiBaseUrl: String

    var faceMaxRetries: Int
    var faceQualityThreshold: Float
    var faceNbOfFramesCaptured: Int
    var faceMatchThreshold: Float

    var fingerprintConfidenceThresholds: Map<FingerprintConfidenceThresholds, Int>
    var faceConfidenceThresholds: Map<FaceConfidenceThresholds, Int>

    fun getRemoteConfigStringPreference(key: String): String
    fun <T: Any>getRemoteConfigComplexPreference(key: String, serializer: Serializer<T>): T
    fun getRemoteConfigFingerprintsToCollect(): List<FingerIdentifier>

}

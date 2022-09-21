package com.simprints.id.data.prefs.settings

import com.simprints.core.domain.common.GROUP
import com.simprints.core.domain.modality.Modality
import com.simprints.core.sharedpreferences.ImprovedSharedPreferences
import com.simprints.core.tools.json.JsonHelper
import com.simprints.core.tools.utils.LanguageHelper.SHARED_PREFS_LANGUAGE_DEFAULT
import com.simprints.core.tools.utils.LanguageHelper.SHARED_PREFS_LANGUAGE_KEY
import com.simprints.id.data.db.subject.domain.FingerIdentifier
import com.simprints.id.data.prefs.RemoteConfigWrapper
import com.simprints.id.data.prefs.preferenceType.ComplexPreference
import com.simprints.id.data.prefs.preferenceType.PrimitivePreference
import com.simprints.id.data.prefs.preferenceType.remoteConfig.RemoteConfigComplexPreference
import com.simprints.id.data.prefs.preferenceType.remoteConfig.RemoteConfigPrimitivePreference
import com.simprints.id.data.prefs.preferenceType.remoteConfig.overridable.OverridableRemoteConfigComplexPreference
import com.simprints.id.data.prefs.preferenceType.remoteConfig.overridable.OverridableRemoteConfigPrimitivePreference
import com.simprints.id.data.prefs.settings.fingerprint.models.CaptureFingerprintStrategy
import com.simprints.id.data.prefs.settings.fingerprint.models.FingerComparisonStrategy
import com.simprints.id.data.prefs.settings.fingerprint.models.SaveFingerprintImagesStrategy
import com.simprints.id.data.prefs.settings.fingerprint.models.ScannerGeneration
import com.simprints.id.domain.CosyncSetting
import com.simprints.id.domain.SimprintsSyncSetting
import com.simprints.id.exceptions.unexpected.preferences.NoSuchPreferenceError
import com.simprints.id.orchestrator.responsebuilders.FaceConfidenceThresholds
import com.simprints.id.orchestrator.responsebuilders.FingerprintConfidenceThresholds
import com.simprints.id.services.sync.events.master.models.EventDownSyncSetting
import com.simprints.id.tools.serializers.Serializer

open class SettingsPreferencesManagerImpl(
    prefs: ImprovedSharedPreferences,
    private val remoteConfigWrapper: RemoteConfigWrapper,
    groupSerializer: Serializer<GROUP>,
    modalitySerializer: Serializer<List<Modality>>,
    languagesStringArraySerializer: Serializer<Array<String>>,
    moduleNamesStringSetSerializer: Serializer<Set<String>>,
    eventDownSyncSettingSerializer: Serializer<EventDownSyncSetting>,
    captureFingerprintStrategySerializer: Serializer<CaptureFingerprintStrategy>,
    fingerComparisonStrategySerializer: Serializer<FingerComparisonStrategy>,
    saveFingerprintImagesStrategySerializer: Serializer<SaveFingerprintImagesStrategy>,
    scannerGenerationsSerializer: Serializer<List<ScannerGeneration>>,
    private val fingerprintsToCollectSerializer: Serializer<List<FingerIdentifier>>,
    fingerprintConfidenceThresholdsSerializer: Serializer<Map<FingerprintConfidenceThresholds, Int>>,
    faceConfidenceThresholdsSerializer: Serializer<Map<FaceConfidenceThresholds, Int>>,
    simprintsSyncSerializer: Serializer<SimprintsSyncSetting>,
    cosyncSyncSerializer: Serializer<CosyncSetting>
) : SettingsPreferencesManager {

    /**
     *  Number of GUIDs to be returned to the calling app as the result of an identification
     */
    override var returnIdCount: Int
        by RemoteConfigPrimitivePreference(prefs, remoteConfigWrapper, NB_IDS_KEY, NB_IDS_DEFAULT)

    /**
     *  What languages should be selectable for this project. Serialize as commas separated list. The empty list defaults to all languages
     */
    override var projectLanguages: Array<String>
        by RemoteConfigComplexPreference(
            prefs,
            remoteConfigWrapper,
            PROJECT_LANGUAGES_POSITION_KEY,
            PROJECT_LANGUAGES_POSITION_DEFAULT,
            languagesStringArraySerializer
        )

    /**
     * Selected language
     */
    override var language: String
        by OverridableRemoteConfigPrimitivePreference(
            prefs,
            remoteConfigWrapper,
            SHARED_PREFS_LANGUAGE_KEY,
            SHARED_PREFS_LANGUAGE_DEFAULT
        )

    /**
     * What modules will be available to sync by for this project. Serialize as pipe (|) separated list. Empty list indicates that module sync should not be possible.
     */
    override var moduleIdOptions: Set<String>
        by RemoteConfigComplexPreference(
            prefs,
            remoteConfigWrapper,
            MODULE_ID_OPTIONS_KEY,
            MODULE_ID_OPTIONS_DEFAULT,
            moduleNamesStringSetSerializer
        )

    /**
     * What modules were selected by the user
     */
    override var selectedModules: Set<String>
        by ComplexPreference(
            prefs,
            SELECTED_MODULES_KEY,
            SELECTED_MODULES_DEFAULT,
            moduleNamesStringSetSerializer
        )

    override var maxNumberOfModules: Int
        by RemoteConfigPrimitivePreference(
            prefs,
            remoteConfigWrapper,
            MAX_NUMBER_OF_MODULES_KEY,
            MAX_NUMBER_OF_MODULES_DEFAULT
        )

    /**
     * Sync group. Default is user
     */
    override var syncGroup: GROUP
        by RemoteConfigComplexPreference(
            prefs,
            remoteConfigWrapper,
            SYNC_GROUP_KEY,
            SYNC_GROUP_DEFAULT,
            groupSerializer
        )

    /**
     * Match group. Default is user
     */
    override var matchGroup: GROUP
        by RemoteConfigComplexPreference(
            prefs,
            remoteConfigWrapper,
            MATCH_GROUP_KEY,
            MATCH_GROUP_DEFAULT,
            groupSerializer
        )

    /**
     * Name of the partner's program
     */
    override var programName: String
        by RemoteConfigPrimitivePreference(
            prefs,
            remoteConfigWrapper,
            PROGRAM_NAME_KEY,
            PROGRAM_NAME_DEFAULT
        )

    /**
     * Name of the partner's organization
     */
    override var organizationName: String
        by RemoteConfigPrimitivePreference(
            prefs,
            remoteConfigWrapper,
            ORGANIZATION_NAME_KEY,
            ORGANIZATION_NAME_DEFAULT
        )

    /**
     * Whether the parental consent should be shown
     */
    override var parentalConsentExists: Boolean
        by RemoteConfigPrimitivePreference(
            prefs,
            remoteConfigWrapper,
            PARENTAL_CONSENT_EXISTS_KEY,
            PARENTAL_CONSENT_EXISTS_DEFAULT
        )

    /**
     * The options of the general consent as a JSON string of booleans
     */
    override var generalConsentOptionsJson: String
        by RemoteConfigPrimitivePreference(
            prefs,
            remoteConfigWrapper,
            GENERAL_CONSENT_OPTIONS_JSON_KEY,
            GENERAL_CONSENT_OPTIONS_JSON_DEFAULT
        )

    /**
     * The options of the parental consent as a JSON string of booleans
     */
    override var parentalConsentOptionsJson: String
        by RemoteConfigPrimitivePreference(
            prefs,
            remoteConfigWrapper,
            PARENTAL_CONSENT_OPTIONS_JSON_KEY, PARENTAL_CONSENT_OPTIONS_JSON_DEFAULT
        )

    /**
     * Whether to show the Simprints logo at the top of the launch activity
     */
    override var logoExists: Boolean
        by RemoteConfigPrimitivePreference(
            prefs,
            remoteConfigWrapper,
            LOGO_EXISTS_KEY,
            LOGO_EXISTS_DEFAULT
        )

    /**
     * Whether consent screen should show or not
     */
    override var consentRequired: Boolean
        by RemoteConfigPrimitivePreference(
            prefs,
            remoteConfigWrapper,
            CONSENT_REQUIRED_KEY,
            CONSENT_REQUIRED_DEFAULT
        )

    override var locationPermissionRequired: Boolean
        by RemoteConfigPrimitivePreference(
            prefs,
            remoteConfigWrapper,
            LOCATION_REQUIRED_KEY,
            LOCATION_REQUIRED_DEFAULT
        )

    /**
     * Flag for v1 of Enrolment+
     */
    override var isEnrolmentPlus: Boolean
        by RemoteConfigPrimitivePreference(
            prefs,
            remoteConfigWrapper,
            ENROLMENT_PLUS_KEY,
            ENROLMENT_PLUS_DEFAULT
        )

    /**
     * List of modalities to use
     */
    override var modalities: List<Modality>
        by RemoteConfigComplexPreference(
            prefs,
            remoteConfigWrapper,
            MODALITY_KEY,
            MODALITY_DEFAULT,
            modalitySerializer
        )

    override var eventDownSyncSetting: EventDownSyncSetting
        by RemoteConfigComplexPreference(
            prefs,
            remoteConfigWrapper,
            PEOPLE_DOWN_SYNC_SETTING_KEY,
            PEOPLE_DOWN_SYNC_SETTING_DEFAULT,
            eventDownSyncSettingSerializer
        )

    override var simprintsSyncSetting: SimprintsSyncSetting
        by RemoteConfigComplexPreference(
            prefs,
            remoteConfigWrapper,
            SIMPRINTS_SYNC_KEY,
            SIMPRINTS_SYNC_SETTINGS_DEFAULT,
            simprintsSyncSerializer
        )

    override var cosyncSyncSetting: CosyncSetting
        by RemoteConfigComplexPreference(
            prefs,
            remoteConfigWrapper,
            COSYNC_SYNC_KEY,
            COSYNC_SYNC_SETTINGS_DEFAULT,
            cosyncSyncSerializer
        )

    override var fingerprintsToCollect: List<FingerIdentifier>
        by OverridableRemoteConfigComplexPreference(
            prefs,
            remoteConfigWrapper,
            FINGERPRINTS_TO_COLLECT_KEY,
            FINGERPRINTS_TO_COLLECT_DEFAULT,
            fingerprintsToCollectSerializer
        )

    /**
     * The Vero2FirmwareVersions as a JSON string
     */

    override var firmwareDownloadableVersionsJson: String
        by RemoteConfigPrimitivePreference(
            prefs,
            remoteConfigWrapper,
            VERO2_FIRMWARE_VERSIONS_KEY,
            VERO2_FIRMWARE_VERSIONS_DEFAULT
        )

    override var fingerImagesExist: Boolean
        by RemoteConfigPrimitivePreference(
            prefs,
            remoteConfigWrapper,
            FINGER_IMAGES_EXIST_KEY,
            FINGER_IMAGES_EXIST_DEFAULT
        )

    override var captureFingerprintStrategy: CaptureFingerprintStrategy
        by RemoteConfigComplexPreference(
            prefs,
            remoteConfigWrapper,
            CAPTURE_FINGERPRINT_STRATEGY_KEY,
            CAPTURE_FINGERPRINT_STRATEGY_DEFAULT,
            captureFingerprintStrategySerializer
        )
    override var fingerComparisonStrategy: FingerComparisonStrategy
        by RemoteConfigComplexPreference(
            prefs,
            remoteConfigWrapper,
            FINGERPRINT_MATCHING_STRATEGY_FOR_VERIFICATION_KEY,
            FINGERPRINT_COMPARISON_STRATEGY_FOR_VERIFICATION_DEFAULT,
            fingerComparisonStrategySerializer
        )
    override var saveFingerprintImagesStrategy: SaveFingerprintImagesStrategy
        by RemoteConfigComplexPreference(
            prefs,
            remoteConfigWrapper,
            SAVE_FINGERPRINT_IMAGES_STRATEGY_KEY,
            SAVE_FINGERPRINT_IMAGES_STRATEGY_DEFAULT,
            saveFingerprintImagesStrategySerializer
        )

    override var scannerGenerations: List<ScannerGeneration>
        by RemoteConfigComplexPreference(
            prefs,
            remoteConfigWrapper,
            SCANNER_GENERATIONS_KEY,
            SCANNER_GENERATIONS_DEFAULT,
            scannerGenerationsSerializer
        )

    override var fingerprintLiveFeedbackOn: Boolean
        by RemoteConfigPrimitivePreference(
            prefs,
            remoteConfigWrapper,
            FINGERPRINT_LIVE_FEEDBACK_ON_KEY,
            FINGERPRINT_LIVE_FEEDBACK_ON_DEFAULT
        )

    override var fingerprintQualityThreshold: Int
        by RemoteConfigPrimitivePreference(
            prefs,
            remoteConfigWrapper,
            FINGERPRINT_QUALITY_THRESHOLD_KEY,
            FINGERPRINT_QUALITY_THRESHOLD_DEFAULT
        )

    override var faceQualityThreshold: Float
        by RemoteConfigPrimitivePreference(
            prefs,
            remoteConfigWrapper,
            FACE_QUALITY_THRESHOLD,
            FACE_QUALITY_THRESHOLD_DEFAULT
        )

    override var faceNbOfFramesCaptured: Int
        by RemoteConfigPrimitivePreference(
            prefs,
            remoteConfigWrapper,
            FACE_NB_OF_FRAMES_CAPTURED,
            FACE_NB_OF_FRAMES_CAPTURED_DEFAULT
        )

    override var shouldSaveFaceImages: Boolean
        by RemoteConfigPrimitivePreference(
            prefs,
            remoteConfigWrapper,
            SHOULD_SAVE_FACE_IMAGES_KEY,
            SHOULD_SAVE_FACE_IMAGES_DEFAULT
        )

    override var fingerprintConfidenceThresholds: Map<FingerprintConfidenceThresholds, Int>
        by RemoteConfigComplexPreference(
            prefs,
            remoteConfigWrapper,
            FINGERPRINT_CONFIDENCE_THRESHOLDS,
            FINGERPRINT_CONFIDENCE_THRESHOLDS_DEFAULT,
            fingerprintConfidenceThresholdsSerializer
        )

    override var faceConfidenceThresholds: Map<FaceConfidenceThresholds, Int>
        by RemoteConfigComplexPreference(
            prefs,
            remoteConfigWrapper,
            FACE_CONFIDENCE_THRESHOLDS,
            FACE_CONFIDENCE_THRESHOLDS_DEFAULT,
            faceConfidenceThresholdsSerializer
        )

    override var lastInstructionId: String by PrimitivePreference(
        prefs,
        LAST_INSTRUCTION_ID_KEY,
        LAST_INSTRUCTION_ID_DEFAULT
    )

    override fun getRemoteConfigStringPreference(key: String) = remoteConfigWrapper.getString(key)
        ?: throw NoSuchPreferenceError.forKey(key)

    override fun <T : Any> getRemoteConfigComplexPreference(
        key: String,
        serializer: Serializer<T>
    ): T = serializer.deserialize(getRemoteConfigStringPreference(key))

    override fun getRemoteConfigFingerprintsToCollect() =
        getRemoteConfigComplexPreference(
            FINGERPRINTS_TO_COLLECT_KEY,
            fingerprintsToCollectSerializer
        )

    companion object {
        const val NB_IDS_KEY = "NbOfIdsInt"
        const val NB_IDS_DEFAULT = 10

        const val PROJECT_LANGUAGES_POSITION_KEY = "ProjectLanguages"
        val PROJECT_LANGUAGES_POSITION_DEFAULT = arrayOf<String>()

        const val MODULE_ID_OPTIONS_KEY = "ModuleIdOptions"
        val MODULE_ID_OPTIONS_DEFAULT = setOf<String>()

        const val SELECTED_MODULES_KEY = "SelectedModules"
        val SELECTED_MODULES_DEFAULT = setOf<String>()

        const val MAX_NUMBER_OF_MODULES_KEY = "MaxNbOfModules"
        const val MAX_NUMBER_OF_MODULES_DEFAULT = 6

        const val SYNC_GROUP_KEY = "SyncGroup"
        val SYNC_GROUP_DEFAULT = GROUP.USER

        const val MATCH_GROUP_KEY = "MatchGroup"
        val MATCH_GROUP_DEFAULT = GROUP.USER

        const val PROGRAM_NAME_KEY = "ProgramName"
        const val PROGRAM_NAME_DEFAULT = "this program"

        const val ORGANIZATION_NAME_KEY = "OrganizationName"
        const val ORGANIZATION_NAME_DEFAULT = "This organization"

        const val PARENTAL_CONSENT_EXISTS_KEY = "ConsentParentalExists"
        const val PARENTAL_CONSENT_EXISTS_DEFAULT = false

        const val GENERAL_CONSENT_OPTIONS_JSON_KEY = "ConsentGeneralOptions"
        val GENERAL_CONSENT_OPTIONS_JSON_DEFAULT: String =
            JsonHelper.toJson(com.simprints.id.data.consent.shortconsent.GeneralConsentOptions())

        const val PARENTAL_CONSENT_OPTIONS_JSON_KEY = "ConsentParentalOptions"
        val PARENTAL_CONSENT_OPTIONS_JSON_DEFAULT: String =
            JsonHelper.toJson(com.simprints.id.data.consent.shortconsent.ParentalConsentOptions())

        const val LOGO_EXISTS_KEY = "LogoExists"
        const val LOGO_EXISTS_DEFAULT = true

        const val CONSENT_REQUIRED_KEY = "ConsentRequired"
        const val CONSENT_REQUIRED_DEFAULT = true

        const val LOCATION_REQUIRED_KEY = "LocationRequired"
        const val LOCATION_REQUIRED_DEFAULT = true

        const val ENROLMENT_PLUS_KEY = "EnrolmentPlus"
        const val ENROLMENT_PLUS_DEFAULT = false

        const val PEOPLE_DOWN_SYNC_SETTING_KEY = "DownSyncSetting"
        val PEOPLE_DOWN_SYNC_SETTING_DEFAULT = EventDownSyncSetting.ON

        const val SIMPRINTS_SYNC_KEY = "SimprintsSync"
        val SIMPRINTS_SYNC_SETTINGS_DEFAULT = SimprintsSyncSetting.SIM_SYNC_NONE

        const val COSYNC_SYNC_KEY = "CoSync"
        val COSYNC_SYNC_SETTINGS_DEFAULT = CosyncSetting.COSYNC_NONE

        val MODALITY_DEFAULT = listOf(Modality.FINGER)
        const val MODALITY_KEY = "Modality"

        const val FINGERPRINTS_TO_COLLECT_KEY = "FingerprintsToCollect"
        val FINGERPRINTS_TO_COLLECT_DEFAULT =
            listOf(FingerIdentifier.LEFT_THUMB, FingerIdentifier.LEFT_INDEX_FINGER)

        const val FINGER_IMAGES_EXIST_KEY = "FingerImagesExist"
        const val FINGER_IMAGES_EXIST_DEFAULT = true

        val CAPTURE_FINGERPRINT_STRATEGY_DEFAULT = CaptureFingerprintStrategy.SECUGEN_ISO_1700_DPI
        const val CAPTURE_FINGERPRINT_STRATEGY_KEY = "CaptureFingerprintStrategy"

        val FINGERPRINT_COMPARISON_STRATEGY_FOR_VERIFICATION_DEFAULT =
            FingerComparisonStrategy.SAME_FINGER
        const val FINGERPRINT_MATCHING_STRATEGY_FOR_VERIFICATION_KEY =
            "FingerComparisonStrategyForVerification"

        val SAVE_FINGERPRINT_IMAGES_STRATEGY_DEFAULT = SaveFingerprintImagesStrategy.NEVER
        const val SAVE_FINGERPRINT_IMAGES_STRATEGY_KEY = "SaveFingerprintImagesStrategy"

        val SCANNER_GENERATIONS_DEFAULT = listOf(ScannerGeneration.VERO_1)
        const val SCANNER_GENERATIONS_KEY = "ScannerGenerations"

        const val FINGERPRINT_LIVE_FEEDBACK_ON_DEFAULT = false
        const val FINGERPRINT_LIVE_FEEDBACK_ON_KEY = "FingerprintLiveFeedbackOn"

        const val SHOULD_SAVE_FACE_IMAGES_DEFAULT = true
        const val SHOULD_SAVE_FACE_IMAGES_KEY = "SaveFaceImages"

        const val FINGERPRINT_QUALITY_THRESHOLD_DEFAULT = 60
        const val FINGERPRINT_QUALITY_THRESHOLD_KEY = "FingerprintQualityThreshold"

        const val FACE_QUALITY_THRESHOLD = "FaceQualityThreshold"
        const val FACE_QUALITY_THRESHOLD_DEFAULT = -1f

        const val FACE_NB_OF_FRAMES_CAPTURED = "FaceNbOfFramesCaptured"
        const val FACE_NB_OF_FRAMES_CAPTURED_DEFAULT = 2

        const val FINGERPRINT_CONFIDENCE_THRESHOLDS = "FingerprintConfidenceThresholds"
        val FINGERPRINT_CONFIDENCE_THRESHOLDS_DEFAULT = mapOf(
            FingerprintConfidenceThresholds.LOW to 0,
            FingerprintConfidenceThresholds.MEDIUM to 0,
            FingerprintConfidenceThresholds.HIGH to 700
        )

        const val FACE_CONFIDENCE_THRESHOLDS = "FaceConfidenceThresholds"
        val FACE_CONFIDENCE_THRESHOLDS_DEFAULT = mapOf(
            FaceConfidenceThresholds.LOW to 0,
            FaceConfidenceThresholds.MEDIUM to 0,
            FaceConfidenceThresholds.HIGH to 700
        )

        const val VERO2_FIRMWARE_VERSIONS_KEY = "Vero2FirmwareVersions"
        const val VERO2_FIRMWARE_VERSIONS_DEFAULT = ""

        const val LAST_INSTRUCTION_ID_KEY = "LastInstructionId"
        const val LAST_INSTRUCTION_ID_DEFAULT = ""

    }

}

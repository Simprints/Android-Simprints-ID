package com.simprints.id.data.prefs.settings

import com.google.gson.JsonSyntaxException
import com.simprints.id.data.db.person.domain.FingerIdentifier
import com.simprints.id.data.prefs.RemoteConfigWrapper
import com.simprints.id.data.prefs.improvedSharedPreferences.ImprovedSharedPreferences
import com.simprints.id.data.prefs.preferenceType.ComplexPreference
import com.simprints.id.data.prefs.preferenceType.remoteConfig.RemoteConfigComplexPreference
import com.simprints.id.data.prefs.preferenceType.remoteConfig.RemoteConfigPrimitivePreference
import com.simprints.id.data.prefs.preferenceType.remoteConfig.overridable.OverridableRemoteConfigComplexPreference
import com.simprints.id.data.prefs.preferenceType.remoteConfig.overridable.OverridableRemoteConfigPrimitivePreference
import com.simprints.id.domain.GROUP
import com.simprints.id.domain.modality.Modality
import com.simprints.id.exceptions.unexpected.preferences.NoSuchPreferenceError
import com.simprints.id.services.scheduledSync.people.master.models.PeopleDownSyncTrigger
import com.simprints.id.tools.serializers.Serializer


open class SettingsPreferencesManagerImpl(prefs: ImprovedSharedPreferences,
                                          private val remoteConfigWrapper: RemoteConfigWrapper,
                                          private val fingerIdToBooleanSerializer: Serializer<Map<FingerIdentifier, Boolean>>,
                                          groupSerializer: Serializer<GROUP>,
                                          modalitySerializer: Serializer<List<Modality>>,
                                          languagesStringArraySerializer: Serializer<Array<String>>,
                                          moduleIdOptionsStringSetSerializer: Serializer<Set<String>>,
                                          peopleDownSyncTriggerToSerializer: Serializer<Map<PeopleDownSyncTrigger, Boolean>>,
                                          saveFingerprintImagesSerializer: Serializer<SaveFingerprintImagesStrategy>)
    : SettingsPreferencesManager {

    companion object {

        const val NB_IDS_KEY = "NbOfIdsInt"
        const val NB_IDS_DEFAULT = 10

        const val PROJECT_LANGUAGES_POSITION_KEY = "ProjectLanguages"
        val PROJECT_LANGUAGES_POSITION_DEFAULT = arrayOf<String>()

        const val LANGUAGE_KEY = "SelectedLanguage"
        const val LANGUAGE_DEFAULT = "en"

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

        const val FINGER_STATUS_KEY = "FingerStatus"
        val FINGER_STATUS_DEFAULT = mapOf(
            FingerIdentifier.RIGHT_THUMB to false,
            FingerIdentifier.RIGHT_INDEX_FINGER to false,
            FingerIdentifier.RIGHT_3RD_FINGER to false,
            FingerIdentifier.RIGHT_4TH_FINGER to false,
            FingerIdentifier.RIGHT_5TH_FINGER to false,
            FingerIdentifier.LEFT_THUMB to true,
            FingerIdentifier.LEFT_INDEX_FINGER to true,
            FingerIdentifier.LEFT_3RD_FINGER to false,
            FingerIdentifier.LEFT_4TH_FINGER to false,
            FingerIdentifier.LEFT_5TH_FINGER to false
        )

        const val PROGRAM_NAME_KEY = "ProgramName"
        const val PROGRAM_NAME_DEFAULT = "this program"

        const val ORGANIZATION_NAME_KEY = "OrganizationName"
        const val ORGANIZATION_NAME_DEFAULT = "This organization"

        const val LOGO_EXISTS_KEY = "LogoExists"
        const val LOGO_EXISTS_DEFAULT = true

        const val CONSENT_REQUIRED_KEY = "ConsentRequired"
        const val CONSENT_REQUIRED_DEFAULT = true

        const val PEOPLE_DOWN_SYNC_TRIGGERS_KEY = "PeopleDownSyncTriggers"
        val PEOPLE_DOWN_SYNC_TRIGGERS_DEFAULT = mapOf(
            PeopleDownSyncTrigger.MANUAL to true,
            PeopleDownSyncTrigger.PERIODIC_BACKGROUND to true,
            PeopleDownSyncTrigger.ON_LAUNCH_CALLOUT to false
        )

        val MODALITY_DEFAULT = listOf(Modality.FINGER)
        const val MODALITY_KEY = "Modality"

        const val FINGER_IMAGES_EXIST_KEY = "FingerImagesExist"
        const val FINGER_IMAGES_EXIST_DEFAULT = true

        val SAVE_FINGERPRINT_IMAGES_DEFAULT = SaveFingerprintImagesStrategy.NEVER
        const val SAVE_FINGERPRINT_IMAGES_KEY = "SaveFingerprintImages"
    }

    // Number of GUIDs to be returned to the calling app as the result of an identification
    override var returnIdCount: Int
        by RemoteConfigPrimitivePreference(prefs, remoteConfigWrapper, NB_IDS_KEY, NB_IDS_DEFAULT)

    // What languages should be selectable for this project. Serialize as commas separated list. The empty list defaults to all languages
    override var projectLanguages: Array<String>
        by RemoteConfigComplexPreference(prefs, remoteConfigWrapper, PROJECT_LANGUAGES_POSITION_KEY, PROJECT_LANGUAGES_POSITION_DEFAULT, languagesStringArraySerializer)

    // Selected language
    override var language: String
        by OverridableRemoteConfigPrimitivePreference(prefs, remoteConfigWrapper, LANGUAGE_KEY, LANGUAGE_DEFAULT)

    // What modules will be available to sync by for this project. Serialize as pipe (|) separated list. Empty list indicates that module sync should not be possible.
    override var moduleIdOptions: Set<String>
        by RemoteConfigComplexPreference(prefs, remoteConfigWrapper, MODULE_ID_OPTIONS_KEY, MODULE_ID_OPTIONS_DEFAULT, moduleIdOptionsStringSetSerializer)

    // What modules were selected by the user
    override var selectedModules: Set<String>
        by ComplexPreference(prefs, SELECTED_MODULES_KEY, SELECTED_MODULES_DEFAULT, moduleIdOptionsStringSetSerializer)

    override var maxNumberOfModules: Int
        by RemoteConfigPrimitivePreference(prefs, remoteConfigWrapper, MAX_NUMBER_OF_MODULES_KEY, MAX_NUMBER_OF_MODULES_DEFAULT)

    // Sync group. Default is user
    override var syncGroup: GROUP
        by RemoteConfigComplexPreference(prefs, remoteConfigWrapper, SYNC_GROUP_KEY, SYNC_GROUP_DEFAULT, groupSerializer)

    // Match group. Default is user
    override var matchGroup: GROUP
        by RemoteConfigComplexPreference(prefs, remoteConfigWrapper, MATCH_GROUP_KEY, MATCH_GROUP_DEFAULT, groupSerializer)

    // The map of default fingers
    /** @throws JsonSyntaxException */
    override var fingerStatus: Map<FingerIdentifier, Boolean>
        by OverridableRemoteConfigComplexPreference(prefs, remoteConfigWrapper, FINGER_STATUS_KEY, FINGER_STATUS_DEFAULT, fingerIdToBooleanSerializer)

    // Name of the partner's program
    override var programName: String
        by RemoteConfigPrimitivePreference(prefs, remoteConfigWrapper, PROGRAM_NAME_KEY, PROGRAM_NAME_DEFAULT)
    // Name of the partner's organization
    override var organizationName: String
        by RemoteConfigPrimitivePreference(prefs, remoteConfigWrapper, ORGANIZATION_NAME_KEY, ORGANIZATION_NAME_DEFAULT)

    // Whether to show the Simprints logo at the top of the launch activity
    override var logoExists: Boolean
        by RemoteConfigPrimitivePreference(prefs, remoteConfigWrapper, LOGO_EXISTS_KEY, LOGO_EXISTS_DEFAULT)

    override var consentRequired: Boolean
        by RemoteConfigPrimitivePreference(prefs, remoteConfigWrapper, CONSENT_REQUIRED_KEY, CONSENT_REQUIRED_DEFAULT)

    override var modalities: List<Modality>
        by RemoteConfigComplexPreference(prefs, remoteConfigWrapper, MODALITY_KEY, MODALITY_DEFAULT, modalitySerializer)


    override var peopleDownSyncTriggers: Map<PeopleDownSyncTrigger, Boolean>
        by RemoteConfigComplexPreference(prefs, remoteConfigWrapper, PEOPLE_DOWN_SYNC_TRIGGERS_KEY, PEOPLE_DOWN_SYNC_TRIGGERS_DEFAULT, peopleDownSyncTriggerToSerializer)

    override var fingerImagesExist: Boolean
        by RemoteConfigPrimitivePreference(prefs, remoteConfigWrapper, FINGER_IMAGES_EXIST_KEY, FINGER_IMAGES_EXIST_DEFAULT)

    override var saveFingerprintImages: SaveFingerprintImagesStrategy
        by RemoteConfigComplexPreference(prefs, remoteConfigWrapper, SAVE_FINGERPRINT_IMAGES_KEY, SAVE_FINGERPRINT_IMAGES_DEFAULT, saveFingerprintImagesSerializer)

    init {
        remoteConfigWrapper.registerAllPreparedDefaultValues()
    }

    override fun getRemoteConfigStringPreference(key: String) = remoteConfigWrapper.getString(key)
        ?: throw NoSuchPreferenceError.forKey(key)

    override fun <T : Any> getRemoteConfigComplexPreference(key: String, serializer: Serializer<T>): T = serializer.deserialize(getRemoteConfigStringPreference(key))

    override fun getRemoteConfigFingerStatus() = getRemoteConfigComplexPreference(FINGER_STATUS_KEY, fingerIdToBooleanSerializer)
}

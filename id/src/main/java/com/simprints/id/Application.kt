package com.simprints.id

import android.content.SharedPreferences
import android.support.multidex.MultiDexApplication
import com.crashlytics.android.Crashlytics
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.gson.Gson
import com.simprints.id.controllers.Setup
import com.simprints.id.data.DataManager
import com.simprints.id.data.DataManagerImpl
import com.simprints.id.data.db.analytics.AnalyticsManager
import com.simprints.id.data.db.analytics.FirebaseAnalyticsManager
import com.simprints.id.data.db.local.LocalDbManager
import com.simprints.id.data.db.local.RealmDbManager
import com.simprints.id.data.db.remote.FirebaseRtdbManager
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.data.network.ApiManager
import com.simprints.id.data.network.ApiManagerImpl
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.data.prefs.PreferencesManagerImpl
import com.simprints.id.data.prefs.improvedSharedPreferences.ImprovedSharedPreferences
import com.simprints.id.data.prefs.improvedSharedPreferences.ImprovedSharedPreferencesImpl
import com.simprints.id.data.prefs.sessionState.SessionStatePreferencesManager
import com.simprints.id.data.prefs.sessionState.SessionStatePreferencesManagerImpl
import com.simprints.id.data.prefs.sessionState.scannerAttributes.ScannerAttributesPreferencesManager
import com.simprints.id.data.prefs.sessionState.scannerAttributes.ScannerAttributesPreferencesManagerImpl
import com.simprints.id.data.prefs.sessionState.sessionParameters.SessionParametersPreferencesManager
import com.simprints.id.data.prefs.sessionState.sessionParameters.SessionParametersPreferencesManagerImpl
import com.simprints.id.data.prefs.sessionState.sessionTimestamps.SessionTimestampsPreferencesManager
import com.simprints.id.data.prefs.sessionState.sessionTimestamps.SessionTimestampsPreferencesManagerImpl
import com.simprints.id.data.prefs.settings.SettingsPreferencesManager
import com.simprints.id.data.prefs.settings.SettingsPreferencesManagerImpl
import com.simprints.id.data.secure.SecureDataManager
import com.simprints.id.data.secure.SecureDataManagerImpl
import com.simprints.id.domain.Location
import com.simprints.id.domain.callout.CalloutAction
import com.simprints.id.domain.callout.CalloutParameter
import com.simprints.id.domain.sessionParameters.SessionParameters
import com.simprints.id.domain.sessionParameters.extractors.ActionDependentExtractor
import com.simprints.id.domain.sessionParameters.extractors.Extractor
import com.simprints.id.domain.sessionParameters.extractors.ParameterExtractor
import com.simprints.id.domain.sessionParameters.extractors.SessionParametersExtractor
import com.simprints.id.domain.sessionParameters.readers.*
import com.simprints.id.domain.sessionParameters.validators.*
import com.simprints.id.exceptions.unsafe.InvalidCalloutError
import com.simprints.id.model.ALERT_TYPE
import com.simprints.id.tools.AppState
import com.simprints.id.tools.NotificationFactory
import com.simprints.id.tools.TimeHelper
import com.simprints.id.tools.TimeHelperImpl
import com.simprints.id.tools.serializers.*
import com.simprints.libdata.tools.Constants
import com.simprints.libsimprints.Constants.*
import com.simprints.libsimprints.FingerIdentifier
import io.fabric.sdk.android.Fabric
import timber.log.Timber
import java.util.*

class Application : MultiDexApplication() {

    // TODO: dependency injection with Dagger 2!

    private val gson: Gson by lazy {
        Gson()
    }

    private val booleanSerializer: Serializer<Boolean> by lazy {
        BooleanSerializer()
    }

    private val fingerIdentifierSerializer: Serializer<FingerIdentifier> by lazy {
        EnumSerializer(FingerIdentifier::class.java)
    }

    private val calloutActionSerializer: Serializer<CalloutAction> by lazy {
        EnumSerializer(CalloutAction::class.java)
    }

    private val groupSerializer: Serializer<Constants.GROUP> by lazy {
        EnumSerializer(Constants.GROUP::class.java)
    }

    private val fingerIdToBooleanSerializer: Serializer<Map<FingerIdentifier, Boolean>> by lazy {
        MapSerializer(fingerIdentifierSerializer, booleanSerializer, gson)
    }

    private val locationSerializer: Serializer<Location> by lazy {
        LocationSerializer()
    }

    private val basePrefs: SharedPreferences by lazy {
        this.getSharedPreferences(PreferencesManagerImpl.PREF_FILE_NAME, PreferencesManagerImpl.PREF_MODE)
    }

    public val prefs: ImprovedSharedPreferences by lazy {
        ImprovedSharedPreferencesImpl(basePrefs)
    }

    private val scannerAttributesPreferencesManager: ScannerAttributesPreferencesManager by lazy {
        ScannerAttributesPreferencesManagerImpl(prefs)
    }

    private val sessionParametersPreferencesManager: SessionParametersPreferencesManager by lazy {
        SessionParametersPreferencesManagerImpl(prefs, calloutActionSerializer)
    }

    private val sessionTimestampsPreferencesManager: SessionTimestampsPreferencesManager by lazy {
        SessionTimestampsPreferencesManagerImpl(prefs)
    }

    private val sessionStatePreferencesManager: SessionStatePreferencesManager by lazy {
        SessionStatePreferencesManagerImpl(prefs,
            scannerAttributesPreferencesManager,
            sessionParametersPreferencesManager,
            sessionTimestampsPreferencesManager,
            locationSerializer)
    }

    private val settingsPreferencesManager: SettingsPreferencesManager by lazy {
        SettingsPreferencesManagerImpl(prefs, fingerIdToBooleanSerializer, groupSerializer)
    }

    private val preferencesManager: PreferencesManager by lazy {
        PreferencesManagerImpl(sessionStatePreferencesManager, settingsPreferencesManager)
    }

    private val localDbManager: LocalDbManager by lazy {
        RealmDbManager()
    }

    private val remoteDbManager: RemoteDbManager by lazy {
        FirebaseRtdbManager()
    }

    private val apiManager: ApiManager by lazy {
        ApiManagerImpl()
    }

    private val fabric: Fabric by lazy {
        Fabric.Builder(this).kits(Crashlytics()).debuggable(BuildConfig.DEBUG).build()
    }

    private val firebaseAnalytics: FirebaseAnalytics by lazy {
        FirebaseAnalytics.getInstance(this).apply {
            setAnalyticsCollectionEnabled(true)
            setMinimumSessionDuration(0)
        }
    }

    var analyticsManager: AnalyticsManager by lazyVar {
        FirebaseAnalyticsManager(firebaseAnalytics)
    }

    val secureDataManager: SecureDataManager by lazy {
        SecureDataManagerImpl(prefs)
    }

    val dataManager: DataManager by lazy {
        DataManagerImpl(this, preferencesManager, localDbManager, remoteDbManager,
                apiManager, analyticsManager, secureDataManager)
    }

    val notificationFactory: NotificationFactory by lazy {
        val factory = NotificationFactory(this)
        factory.initSyncNotificationChannel()
        factory
    }

    private val actionReader: Reader<CalloutAction> by lazy {
        ActionReader()
    }

    private val invalidActionError: Error by lazy {
        InvalidCalloutError(ALERT_TYPE.INVALID_INTENT_ACTION)
    }

    private val actionValidator: Validator<CalloutAction> by lazy {
        ValueValidator(CalloutAction.validValues, invalidActionError)
    }

    private val actionExtractor: Extractor<CalloutAction> by lazy {
        ParameterExtractor(actionReader, actionValidator)
    }

    private val invalidApiKeyError: Error by lazy {
        InvalidCalloutError(ALERT_TYPE.INVALID_API_KEY)
    }

    private val missingApiKeyError: Error by lazy {
        InvalidCalloutError(ALERT_TYPE.MISSING_API_KEY)
    }

    private val apiKeyReader: Reader<String> by lazy {
        MandatoryParameterReader(SIMPRINTS_API_KEY, String::class,
            missingApiKeyError, invalidApiKeyError)
    }

    private val apiKeyValidator: Validator<String> by lazy {
        GuidValidator(invalidApiKeyError)
    }

    private val apiKeyExtractor: Extractor<String> by lazy {
        ParameterExtractor(apiKeyReader, apiKeyValidator)
    }

    private val invalidModuleIdError: Error by lazy {
        InvalidCalloutError(ALERT_TYPE.INVALID_MODULE_ID)
    }

    private val missingModuleIdError: Error by lazy {
        InvalidCalloutError(ALERT_TYPE.MISSING_MODULE_ID)
    }

    private val moduleIdReader: Reader<String> by lazy {
        MandatoryParameterReader(SIMPRINTS_MODULE_ID, String::class,
            missingModuleIdError, invalidModuleIdError)
    }

    private val moduleIdValidator: Validator<String> by lazy {
        NoOpValidator<String>()
    }

    private val moduleIdExtractor: Extractor<String> by lazy {
        ParameterExtractor(moduleIdReader, moduleIdValidator)
    }

    private val invalidUserIdError: Error by lazy {
        InvalidCalloutError(ALERT_TYPE.INVALID_USER_ID)
    }

    private val missingUserIdError: Error by lazy {
        InvalidCalloutError(ALERT_TYPE.MISSING_USER_ID)
    }

    private val userIdReader: Reader<String> by lazy {
        MandatoryParameterReader(SIMPRINTS_USER_ID, String::class,
            missingUserIdError, invalidUserIdError)
    }

    private val userIdValidator: Validator<String> by lazy {
        NoOpValidator<String>()
    }

    private val userIdExtractor: Extractor<String> by lazy {
        ParameterExtractor(userIdReader, userIdValidator)
    }

    private val missingVerifyIdError: Error by lazy {
        InvalidCalloutError(ALERT_TYPE.MISSING_VERIFY_GUID)
    }

    private val invalidVerifyIdError: Error by lazy {
        InvalidCalloutError(ALERT_TYPE.INVALID_VERIFY_GUID)
    }

    private val verifyIdReader: Reader<String> by lazy {
        MandatoryParameterReader(SIMPRINTS_VERIFY_GUID, String::class,
            missingVerifyIdError, invalidVerifyIdError)
    }

    private val verifyIdValidator: Validator<String> by lazy {
        GuidValidator(invalidVerifyIdError)
    }

    private val verifyIdExtractor: Extractor<String> by lazy {
        ParameterExtractor(verifyIdReader, verifyIdValidator)
    }

    private val missingUpdateIdError: Error by lazy {
        InvalidCalloutError(ALERT_TYPE.MISSING_UPDATE_GUID)
    }

    private val invalidUpdateIdError: Error by lazy {
        InvalidCalloutError(ALERT_TYPE.INVALID_UPDATE_GUID)
    }

    private val updateIdReader: Reader<String> by lazy {
        MandatoryParameterReader(SIMPRINTS_UPDATE_GUID, String::class,
            missingUpdateIdError, invalidUpdateIdError)
    }

    private val updateIdValidator: Validator<String> by lazy {
        GuidValidator(invalidVerifyIdError)
    }

    private val updateIdExtractor: Extractor<String> by lazy {
        ParameterExtractor(updateIdReader, updateIdValidator)
    }

    private val guidGenerator: Extractor<String> by lazy {
        ParameterExtractor(GeneratorReader({ UUID.randomUUID().toString() }),
            NoOpValidator())
    }

    private val patientIdExtractor: Extractor<String> by lazy {
        val patientIdSwitch = mapOf(
            CalloutAction.UPDATE to updateIdExtractor,
            CalloutAction.VERIFY to verifyIdExtractor,
            CalloutAction.REGISTER to guidGenerator)
        ActionDependentExtractor(patientIdSwitch, "")
    }

    private val invalidCallingPackageError: Error by lazy {
        InvalidCalloutError(ALERT_TYPE.INVALID_CALLING_PACKAGE)
    }

    private val callingPackageReader: Reader<String> by lazy {
        OptionalParameterReader(SIMPRINTS_CALLING_PACKAGE, "", invalidCallingPackageError)
    }

    private val callingPackageValidator: Validator<String> by lazy {
        NoOpValidator<String>()
    }

    private val callingPackageExtractor: Extractor<String> by lazy {
        ParameterExtractor(callingPackageReader, callingPackageValidator)
    }

    private val metadataReader: Reader<String> by lazy {
        OptionalParameterReader(SIMPRINTS_METADATA, "", invalidCallingPackageError)
    }

    private val invalidMetadataError: Error by lazy {
        InvalidCalloutError(ALERT_TYPE.INVALID_METADATA)
    }

    private val metadataValidator: Validator<String> by lazy {
        MetadataValidator(invalidMetadataError, gson)
    }

    private val metadataExtractor: Extractor<String> by lazy {
        ParameterExtractor(metadataReader, metadataValidator)
    }

    private val invalidResultFormatError: Error by lazy {
        InvalidCalloutError(ALERT_TYPE.INVALID_RESULT_FORMAT)
    }

    private val resultFormatReader: Reader<String> by lazy {
        OptionalParameterReader(SIMPRINTS_RESULT_FORMAT, "", invalidResultFormatError)
    }

    private val resultFormatValidator: Validator<String> by lazy {
        val validResultFormats = listOf(SIMPRINTS_ODK_RESULT_FORMAT_V01, "")
        ValueValidator(validResultFormats, invalidResultFormatError)
    }

    private val resultFormatExtractor: Extractor<String> by lazy {
        ParameterExtractor(resultFormatReader, resultFormatValidator)
    }

    private val expectedParametersLister: ExpectedParametersLister by lazy {
        ExpectedParametersListerImpl()
    }

    private val unexpectedParametersReader: Reader<Set<CalloutParameter>> by lazy {
        UnexpectedParametersReader(expectedParametersLister)
    }

    private val invalidUnexpectedParametersError: Error by lazy {
        InvalidCalloutError(ALERT_TYPE.UNEXPECTED_PARAMETER)
    }

    private val unexpectedParametersValidator: Validator<Set<CalloutParameter>> by lazy {
        val validUnexpectedParametersValues = listOf(emptySet<CalloutParameter>())
        ValueValidator(validUnexpectedParametersValues, invalidUnexpectedParametersError)
    }

    private val unexpectedParametersExtractor: Extractor<Set<CalloutParameter>> by lazy {
        ParameterExtractor(unexpectedParametersReader, unexpectedParametersValidator)
    }

    val sessionParametersExtractor: Extractor<SessionParameters> by lazy {
        SessionParametersExtractor(actionExtractor, apiKeyExtractor, moduleIdExtractor,
            userIdExtractor, patientIdExtractor, callingPackageExtractor, metadataExtractor,
            resultFormatExtractor, unexpectedParametersExtractor)
    }

    val timeHelper: TimeHelper by lazy {
        TimeHelperImpl()
    }

    // TODO: These are all the singletons that are used in Simprints ID right now. This is temporary, until we get rid of all these singletons
    val appState: AppState by lazy {
        AppState.getInstance()
    }

    val setup: Setup by lazy {
        Setup.getInstance(dataManager, appState)
    }

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        Fabric.with(fabric)
    }
}

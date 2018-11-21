package com.simprints.id.di

import com.google.gson.Gson
import com.simprints.id.domain.ALERT_TYPE
import com.simprints.id.domain.Constants
import com.simprints.id.domain.Location
import com.simprints.id.exceptions.unsafe.InvalidCalloutError
import com.simprints.id.session.callout.CalloutAction
import com.simprints.id.session.callout.CalloutParameter
import com.simprints.id.session.sessionParameters.SessionParameters
import com.simprints.id.session.sessionParameters.extractors.ActionDependentExtractor
import com.simprints.id.session.sessionParameters.extractors.Extractor
import com.simprints.id.session.sessionParameters.extractors.ParameterExtractor
import com.simprints.id.session.sessionParameters.extractors.SessionParametersExtractor
import com.simprints.id.session.sessionParameters.readers.*
import com.simprints.id.session.sessionParameters.readers.unexpectedParameters.ExpectedParametersLister
import com.simprints.id.session.sessionParameters.readers.unexpectedParameters.ExpectedParametersListerImpl
import com.simprints.id.session.sessionParameters.readers.unexpectedParameters.UnexpectedParametersReader
import com.simprints.id.session.sessionParameters.validators.*
import com.simprints.id.tools.serializers.*
import com.simprints.libsimprints.Constants.*
import com.simprints.libsimprints.FingerIdentifier
import dagger.Module
import dagger.Provides
import java.util.*
import javax.inject.Named
import javax.inject.Singleton


@Module
@JvmSuppressWildcards(false)
class SerializerModule {

    @Provides @Singleton @Named("BooleanSerializer") fun provideBooleanSerializer(): Serializer<Boolean> = BooleanSerializer()
    @Provides @Singleton @Named("FingerIdentifierSerializer") fun provideFingerIdentifierSerializer(): Serializer<FingerIdentifier> = EnumSerializer(FingerIdentifier::class.java)
    @Provides @Singleton @Named("CalloutActionSerializer") fun provideCalloutActionSerializer(): Serializer<CalloutAction> = EnumSerializer(CalloutAction::class.java)
    @Provides @Singleton @Named("GroupSerializer") fun provideGroupSerializer(): Serializer<Constants.GROUP> = EnumSerializer(Constants.GROUP::class.java)
    @Provides @Singleton fun provideGson(): Gson = Gson()
    @Provides @Singleton @Named("LocationSerializer") fun provideLocationSerializer(): Serializer<Location> = LocationSerializer()

    @Provides @Singleton @Named("FingerIdToBooleanSerializer") fun provideFingerIdToBooleanSerializer(@Named("FingerIdentifierSerializer") fingerIdentifierSerializer: Serializer<FingerIdentifier>,
                                                                @Named("BooleanSerializer") booleanSerializer: Serializer<Boolean>,
                                                                gson: Gson): Serializer<Map<FingerIdentifier, Boolean>> = MapSerializer(fingerIdentifierSerializer, booleanSerializer, gson)

    @Provides @Singleton @Named("LanguagesStringArraySerializer") fun provideLanguagesStringArraySerializer(): Serializer<Array<String>> = LanguagesStringArraySerializer()
    @Provides @Singleton @Named("ModuleIdOptionsStringSetSerializer") fun provideModuleIdOptionsStringSetSerializer(): Serializer<Set<String>> = ModuleIdOptionsStringSetSerializer()

    //Action
    @Provides @Singleton @Named("ActionReader") fun provideActionReader(): Reader<CalloutAction> = ActionReader()
    @Provides @Singleton @Named("InvalidActionError") fun provideInvalidActionError(): Error = InvalidCalloutError(ALERT_TYPE.INVALID_INTENT_ACTION)
    @Provides @Singleton @Named("ActionValidator") fun provideActionValidator(@Named("InvalidActionError") invalidActionError: Error): Validator<CalloutAction> = ValueValidator(CalloutAction.validValues, invalidActionError)
    @Provides @Singleton @Named("ActionExtractor") fun provideActionExtractor(@Named("ActionReader") actionReader: Reader<CalloutAction>, @Named("ActionValidator") actionValidator: Validator<CalloutAction>): Extractor<CalloutAction> = ParameterExtractor(actionReader, actionValidator)

    //ModuleId
    @Provides @Singleton @Named("InvalidModuleIdError") fun provideInvalidModuleIdError(): Error = InvalidCalloutError(ALERT_TYPE.INVALID_MODULE_ID)
    @Provides @Singleton @Named("MissingModuleIdError") fun provideMissingModuleIdError(): Error = InvalidCalloutError(ALERT_TYPE.MISSING_MODULE_ID)
    @Provides @Singleton @Named("ModuleIdValidator") fun provideModuleIdValidator(): Validator<String> = NoOpValidator<String>()
    @Provides @Singleton @Named("ModuleIdExtractor")fun provideModuleIdExtractor(@Named("ModuleIdReader") moduleIdReader: Reader<String>, @Named("ModuleIdValidator") moduleIdValidator: Validator<String>): Extractor<String> = ParameterExtractor(moduleIdReader, moduleIdValidator)
    @Provides @Singleton @Named("ModuleIdReader") fun provideModuleIdReader(@Named("MissingModuleIdError") missingModuleIdError: Error, @Named("InvalidModuleIdError") invalidModuleIdError: Error): Reader<String> =
        MandatoryParameterReader(SIMPRINTS_MODULE_ID, String::class, missingModuleIdError, invalidModuleIdError)

    //APIKey
    @Provides @Singleton @Named("InvalidApiKeyError") fun provideInvalidApiKeyError(): Error = InvalidCalloutError(ALERT_TYPE.INVALID_API_KEY)
    @Provides @Singleton @Named("ApiKeyValidator") fun provideApiKeyValidator(@Named("InvalidApiKeyError") invalidApiKeyError: Error): Validator<String> = GuidValidator(invalidApiKeyError)
    @Provides @Singleton @Named("ApiKeyExtractor") fun provideApiKeyExtractor(@Named("ApiKeyReader") apiKeyReader: Reader<String>, @Named("ApiKeyValidator") apiKeyValidator: Validator<String>): Extractor<String> = ParameterExtractor(apiKeyReader, apiKeyValidator)
    @Provides @Singleton @Named("ApiKeyReader") fun provideApiKeyReader(@Named("InvalidApiKeyError") invalidApiKeyError: Error): Reader<String> =
        OptionalParameterReader(SIMPRINTS_API_KEY, "", invalidApiKeyError)

    //ProjectId
    @Provides @Singleton @Named("InvalidProjectIdError") fun provideInvalidProjectIdError(): Error = InvalidCalloutError(ALERT_TYPE.INVALID_API_KEY)
    @Provides @Singleton @Named("ProjectIdValidator") fun provideProjectIdValidator(@Named("InvalidProjectIdError") invalidProjectIdError: Error): Validator<String> = NoOpValidator()
    @Provides @Singleton @Named("ProjectIdExtractor") fun provideProjectIdExtractor(@Named("ProjectIdReader") projectIdReader: Reader<String>, @Named("ProjectIdValidator") projectIdValidator: Validator<String>): Extractor<String> = ParameterExtractor(projectIdReader, projectIdValidator)
    @Provides @Singleton @Named("ProjectIdReader") fun provideProjectIdReader(@Named("InvalidProjectIdError") invalidProjectIdError: Error): Reader<String> =
        OptionalParameterReader(SIMPRINTS_PROJECT_ID, "", invalidProjectIdError)

    //UserId
    @Provides @Singleton @Named("InvalidUserIdError") fun provideInvalidUserIdError(): Error = InvalidCalloutError(ALERT_TYPE.INVALID_USER_ID)
    @Provides @Singleton @Named("MissingUserIdError") fun provideMissingUserIdError(): Error = InvalidCalloutError(ALERT_TYPE.MISSING_USER_ID)
    @Provides @Singleton @Named("UserIdValidator") fun provideUserIdValidator(): Validator<String> = NoOpValidator()
    @Provides @Singleton @Named("UserIdExtractor") fun provideUserIdExtractor(@Named("UserIdReader") userIdReader: Reader<String>, @Named("UserIdValidator") userIdValidator: Validator<String>): Extractor<String> = ParameterExtractor(userIdReader, userIdValidator)
    @Provides @Singleton @Named("UserIdReader") fun provideUserIdReader(@Named("MissingUserIdError") missingUserIdError: Error, @Named("InvalidUserIdError") invalidUserIdError: Error): Reader<String> =
        MandatoryParameterReader(SIMPRINTS_USER_ID, String::class, missingUserIdError, invalidUserIdError)

    //VerifyId
    @Provides @Singleton @Named("MissingVerifyIdError") fun provideMissingVerifyIdError(): Error = InvalidCalloutError(ALERT_TYPE.MISSING_VERIFY_GUID)
    @Provides @Singleton @Named("InvalidVerifyIdError") fun provideInvalidVerifyIdError(): Error = InvalidCalloutError(ALERT_TYPE.INVALID_VERIFY_GUID)
    @Provides @Singleton @Named("VerifyIdValidator") fun provideVerifyIdValidator(@Named("InvalidVerifyIdError") invalidVerifyIdError: Error): Validator<String> = GuidValidator(invalidVerifyIdError)
    @Provides @Singleton @Named("VerifyIdExtractor") fun provideVerifyIdExtractor(@Named("VerifyIdReader") verifyIdReader: Reader<String>, @Named("VerifyIdValidator") verifyIdValidator: Validator<String>): Extractor<String> = ParameterExtractor(verifyIdReader, verifyIdValidator)
    @Provides @Singleton @Named("VerifyIdReader") fun provideVerifyIdReader(@Named("MissingVerifyIdError") missingVerifyIdError: Error, @Named("InvalidVerifyIdError") invalidVerifyIdError: Error): Reader<String> =
        MandatoryParameterReader(SIMPRINTS_VERIFY_GUID, String::class, missingVerifyIdError, invalidVerifyIdError)

    //UpdateId
    @Provides @Singleton @Named("MissingUpdateIdError") fun provideMissingUpdateIdError(): Error = InvalidCalloutError(ALERT_TYPE.MISSING_UPDATE_GUID)
    @Provides @Singleton @Named("InvalidUpdateIdError") fun provideInvalidUpdateIdError(): Error = InvalidCalloutError(ALERT_TYPE.INVALID_UPDATE_GUID)
    @Provides @Singleton @Named("UpdateIdValidator") fun provideUpdateIdValidator(@Named("InvalidUpdateIdError") invalidUpdateIdError: Error): Validator<String> = GuidValidator(invalidUpdateIdError)
    @Provides @Singleton @Named("UpdateIdExtractor") fun provideUpdateIdExtractor(@Named("UpdateIdReader") updateIdReader: Reader<String> , @Named("UpdateIdValidator") updateIdValidator: Validator<String>): Extractor<String> = ParameterExtractor(updateIdReader, updateIdValidator)
    @Provides @Singleton @Named("UpdateIdReader") fun provideUpdateIdReader(@Named("MissingUpdateIdError") missingUpdateIdError: Error, @Named("InvalidUpdateIdError") invalidUpdateIdError: Error): Reader<String> =
        MandatoryParameterReader(SIMPRINTS_UPDATE_GUID, String::class, missingUpdateIdError, invalidUpdateIdError)

    //CallingPackage
    @Provides @Singleton @Named("InvalidCallingPackageError") fun provideInvalidCallingPackageError(): Error = InvalidCalloutError(ALERT_TYPE.INVALID_CALLING_PACKAGE)
    @Provides @Singleton @Named("CallingPackageReader") fun provideCallingPackageReader(@Named("InvalidCallingPackageError") invalidCallingPackageError: Error): Reader<String> = OptionalParameterReader(SIMPRINTS_CALLING_PACKAGE, "", invalidCallingPackageError)
    @Provides @Singleton @Named("CallingPackageValidator") fun provideCallingPackageValidator(): Validator<String> = NoOpValidator<String>()
    @Provides @Singleton @Named("CallingPackageExtractor") fun provideCallingPackageExtractor(@Named("CallingPackageReader") callingPackageReader: Reader<String>, @Named("CallingPackageValidator") callingPackageValidator: Validator<String>): Extractor<String> = ParameterExtractor(callingPackageReader, callingPackageValidator)

    //MetadataReader
    @Provides @Singleton @Named("MetadataReader") fun provideMetadataReader(@Named("InvalidCallingPackageError") invalidCallingPackageError: Error): Reader<String> = OptionalParameterReader(SIMPRINTS_METADATA, "", invalidCallingPackageError)
    @Provides @Singleton @Named("InvalidMetadataError") fun provideInvalidMetadataError(): Error = InvalidCalloutError(ALERT_TYPE.INVALID_METADATA)
    @Provides @Singleton @Named("MetadataValidator") fun provideMetadataValidator(@Named("InvalidMetadataError") invalidMetadataError: Error, gson: Gson): Validator<String> = MetadataValidator(invalidMetadataError, gson)
    @Provides @Singleton @Named("MetadataExtractor") fun provideMetadataExtractor(@Named("MetadataReader") metadataReader: Reader<String>, @Named("MetadataValidator") metadataValidator: Validator<String>): Extractor<String> = ParameterExtractor(metadataReader, metadataValidator)

    //ResultFormat
    @Provides @Singleton @Named("InvalidResultFormatError") fun provideInvalidResultFormatError(): Error = InvalidCalloutError(ALERT_TYPE.INVALID_RESULT_FORMAT)
    @Provides @Singleton @Named("ResultFormatReader") fun provideResultFormatReader(@Named("InvalidResultFormatError") invalidResultFormatError: Error): Reader<String> = OptionalParameterReader(SIMPRINTS_RESULT_FORMAT, "", invalidResultFormatError)
    @Provides @Singleton @Named("ResultFormatValidator") fun provideResultFormatValidator(@Named("InvalidResultFormatError") invalidResultFormatError: Error): Validator<String> {
        val validResultFormats = listOf(SIMPRINTS_ODK_RESULT_FORMAT_V01, "")
        return ValueValidator(validResultFormats, invalidResultFormatError)
    }
    @Provides @Singleton @Named("ResultFormatExtractor") fun provideResultFormatExtractor(@Named("ResultFormatReader") resultFormatReader: Reader<String>, @Named("ResultFormatValidator") resultFormatValidator: Validator<String>): Extractor<String> = ParameterExtractor(resultFormatReader, resultFormatValidator)

    @Provides @Singleton fun provideParametersLister(): ExpectedParametersLister = ExpectedParametersListerImpl()

    @Provides @Singleton @Named("ParametersReader") fun provideParametersReader(@Named("ExpectedParametersLister") expectedParametersLister: ExpectedParametersLister): Reader<Set<CalloutParameter>> = UnexpectedParametersReader(expectedParametersLister)

    @Provides @Singleton @Named("GuidGenerator") fun provideGuidGenerator(): Extractor<String> = ParameterExtractor(GeneratorReader({ UUID.randomUUID().toString() }), NoOpValidator())
    @Provides @Singleton @Named("PatientIdExtractor") fun providePatientIdExtractor(@Named("UpdateIdExtractor") updateIdExtractor: Extractor<String>, @Named("VerifyIdExtractor") verifyIdExtractor: Extractor<String>, @Named("GuidGenerator") guidGenerator: Extractor<String>): Extractor<String> {
        val patientIdSwitch = mapOf(
            CalloutAction.UPDATE to updateIdExtractor,
            CalloutAction.VERIFY to verifyIdExtractor,
            CalloutAction.REGISTER to guidGenerator)
        return ActionDependentExtractor(patientIdSwitch, "")
    }

    @Provides @Singleton @Named("MissingApiKeyOrProjectIdError") fun provideMissingApiKeyOrProjectIdError(): Error = InvalidCalloutError(ALERT_TYPE.MISSING_PROJECT_ID_OR_API_KEY)
    @Provides @Singleton @Named("SessionParametersValidator") fun provideSessionParametersValidator(@Named("MissingApiKeyOrProjectIdError") missingApiKeyOrProjectIdError: Error): Set<Validator<SessionParameters>> = setOf(ProjectIdOrApiKeyValidator(missingApiKeyOrProjectIdError))

    @Provides @Singleton fun provideSessionParametersExtractor(@Named("ActionExtractor") actionExtractor: Extractor<CalloutAction>,
                                                               @Named("ApiKeyExtractor") apiKeyExtractor: Extractor<String>,
                                                               @Named("ProjectIdExtractor") projectIdExtractor: Extractor<String>,
                                                               @Named("ModuleIdExtractor") moduleIdExtractor: Extractor<String>,
                                                               @Named("UserIdExtractor") userIdExtractor: Extractor<String>,
                                                               @Named("PatientIdExtractor") patientIdExtractor: Extractor<String>,
                                                               @Named("CallingPackageExtractor") callingPackageExtractor: Extractor<String>,
                                                               @Named("MetadataExtractor") metadataExtractor: Extractor<String>,
                                                               @Named("ResultFormatExtractor") resultFormatExtractor: Extractor<String>,
                                                               @Named("SessionParametersValidator") sessionParametersValidator: Set<Validator<SessionParameters>>): SessionParametersExtractor =
    SessionParametersExtractor(actionExtractor, apiKeyExtractor, projectIdExtractor, moduleIdExtractor,
        userIdExtractor, patientIdExtractor, callingPackageExtractor, metadataExtractor,
        resultFormatExtractor, sessionParametersValidator)
}

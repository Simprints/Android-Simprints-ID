package com.simprints.id.domain.calloutValidation.calloutParameters

import com.simprints.id.domain.calloutValidation.calloutParameter.concrete.*


class MainCalloutParameters(val typeParameter: TypeParameter,
                            val apiKeyParameter: ApiKeyParameter,
                            val moduleIdParameter: ModuleIdParameter,
                            val userIdParameter: UserIdParameter,
                            val updateIdParameter: UpdateIdParameter,
                            val verifyIdParameter: VerifyIdParameter,
                            val patientIdParameter: PatientIdParameter,
                            val callingPackageParameter: CallingPackageParameter,
                            val metadataParameter: MetadataParameter,
                            val resultFormatParameter: ResultFormatParameter,
                            calloutParameters: CalloutParameters)
    : CalloutParameters by calloutParameters

package com.simprints.fingerprint.infra.biosdkimpl.initialization

import com.simprints.fingerprint.infra.basebiosdk.initialization.SdkInitializer
import javax.inject.Inject

internal class SdkInitializerImpl @Inject constructor() : SdkInitializer<Unit> {
    override suspend fun initialize(initializationParams: Unit?) {
        // No need to initialize anything for the Simprints Bio SDK
    }
}

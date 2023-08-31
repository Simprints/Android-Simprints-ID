package com.simprints.fingerprint.infra.biosdkimpl.initialization

import com.simprints.fingerprint.infra.basebiosdk.initialization.SdkInitializer

internal class SdkInitializerImpl: SdkInitializer<Unit> {
    override suspend fun initialize(initializationParams: Unit?) {
        // No need to initialize anything for the Simprints Bio SDK
    }
}

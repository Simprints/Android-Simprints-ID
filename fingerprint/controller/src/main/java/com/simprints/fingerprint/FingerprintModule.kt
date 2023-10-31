package com.simprints.fingerprint

import com.simprints.fingerprint.controllers.core.eventData.FingerprintSessionEventsManager
import com.simprints.fingerprint.controllers.core.eventData.FingerprintSessionEventsManagerImpl
import com.simprints.fingerprint.controllers.core.flow.MasterFlowManager
import com.simprints.fingerprint.controllers.core.flow.MasterFlowManagerImpl
import com.simprints.fingerprint.controllers.core.image.FingerprintImageManager
import com.simprints.fingerprint.controllers.core.image.FingerprintImageManagerImpl
import com.simprints.fingerprint.controllers.core.repository.FingerprintDbManager
import com.simprints.fingerprint.controllers.core.repository.FingerprintDbManagerImpl
import com.simprints.fingerprint.controllers.core.timehelper.FingerprintTimeHelper
import com.simprints.fingerprint.controllers.core.timehelper.FingerprintTimeHelperImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class FingerprintModule {

    @Binds
    abstract fun provideMasterFlowManager(impl: MasterFlowManagerImpl): MasterFlowManager

    @Binds
    abstract fun provideFingerprintImageManager(impl: FingerprintImageManagerImpl): FingerprintImageManager

    @Binds
    abstract fun provideFingerprintTimeHelper(impl: FingerprintTimeHelperImpl): FingerprintTimeHelper

    @Binds
    abstract fun provideFingerprintSessionEventsManager(impl: FingerprintSessionEventsManagerImpl): FingerprintSessionEventsManager

    @Binds
    abstract fun provideFingerprintDbManager(impl: FingerprintDbManagerImpl): FingerprintDbManager

}

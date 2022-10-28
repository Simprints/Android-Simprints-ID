package com.simprints.face

import com.simprints.face.controllers.core.events.FaceSessionEventsManager
import com.simprints.face.controllers.core.events.FaceSessionEventsManagerImpl
import com.simprints.face.controllers.core.flow.MasterFlowManager
import com.simprints.face.controllers.core.flow.MasterFlowManagerImpl
import com.simprints.face.controllers.core.image.FaceImageManager
import com.simprints.face.controllers.core.image.FaceImageManagerImpl
import com.simprints.face.controllers.core.repository.FaceDbManager
import com.simprints.face.controllers.core.repository.FaceDbManagerImpl
import com.simprints.face.controllers.core.timehelper.FaceTimeHelper
import com.simprints.face.controllers.core.timehelper.FaceTimeHelperImpl
import com.simprints.face.detection.FaceDetector
import com.simprints.face.detection.rankone.RankOneFaceDetector
import com.simprints.face.initializers.RankOneInitializer
import com.simprints.face.initializers.SdkInitializer
import com.simprints.face.match.FaceMatcher
import com.simprints.face.match.rankone.RankOneFaceMatcher
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class FaceModule {

    @Binds
    abstract fun provideFaceImageManager(impl: FaceImageManagerImpl): FaceImageManager

    @Binds
    abstract fun provideFaceSessionEventsManager(impl: FaceSessionEventsManagerImpl): FaceSessionEventsManager

    @Binds
    abstract fun provideFaceTimeHelper(impl: FaceTimeHelperImpl): FaceTimeHelper

    @Binds
    abstract fun provideFaceDetector(impl: RankOneFaceDetector): FaceDetector

    @Binds
    abstract fun provideSdkInitializer(impl: RankOneInitializer): SdkInitializer

    @Binds
    abstract fun provideFaceDbManager(impl: FaceDbManagerImpl): FaceDbManager

    @Binds
    abstract fun provideMasterFlowManager(impl: MasterFlowManagerImpl): MasterFlowManager

    @Binds
    abstract fun provideFaceMatcher(impl: RankOneFaceMatcher): FaceMatcher
}

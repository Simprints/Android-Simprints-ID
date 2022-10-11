package com.simprints.clientapi

import com.simprints.clientapi.activities.commcare.CommCareAction
import com.simprints.clientapi.activities.commcare.CommCareContract
import com.simprints.clientapi.activities.commcare.CommCarePresenter
import com.simprints.clientapi.activities.errors.ErrorContract
import com.simprints.clientapi.activities.errors.ErrorPresenter
import com.simprints.clientapi.activities.libsimprints.LibSimprintsAction
import com.simprints.clientapi.activities.libsimprints.LibSimprintsContract
import com.simprints.clientapi.activities.libsimprints.LibSimprintsPresenter
import com.simprints.clientapi.activities.odk.OdkAction
import com.simprints.clientapi.activities.odk.OdkContract
import com.simprints.clientapi.activities.odk.OdkPresenter
import com.simprints.clientapi.controllers.core.eventData.ClientApiSessionEventsManager
import com.simprints.clientapi.controllers.core.eventData.ClientApiSessionEventsManagerImpl
import com.simprints.clientapi.data.sharedpreferences.SharedPreferencesManager
import com.simprints.clientapi.data.sharedpreferences.SharedPreferencesManagerImpl
import com.simprints.clientapi.tools.ClientApiTimeHelper
import com.simprints.clientapi.tools.ClientApiTimeHelperImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.assisted.AssistedFactory
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton

@Module
@InstallIn(ActivityComponent::class)
interface ClientApiModule {

    @AssistedFactory
    interface CommCarePresenterFactory {
        fun create(
            view: CommCareContract.View,
            action: CommCareAction,
            coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Default)
        ): CommCarePresenter
    }

    @AssistedFactory
    interface LibSimprintsPresenterFactory {
        fun create(
            view: LibSimprintsContract.View,
            action: LibSimprintsAction
        ): LibSimprintsPresenter
    }

    @AssistedFactory
    interface OdkPresenterFactory {
        fun create(
            view: OdkContract.View,
            action: OdkAction
        ): OdkPresenter
    }

    @AssistedFactory
    interface ErrorPresenterFactory {
        fun create(
            view: ErrorContract.View
        ): ErrorPresenter
    }


    @Binds
    fun bindClientApiSessionEventsManager(impl: ClientApiSessionEventsManagerImpl): ClientApiSessionEventsManager

    @Binds
    fun bindSharedPreferencesManager(impl: SharedPreferencesManagerImpl): SharedPreferencesManager

    @Binds
    fun bindClientApiTimeHelper(impl: ClientApiTimeHelperImpl): ClientApiTimeHelper

}

@Module
@InstallIn(ActivityComponent::class)
class ClientApiDispatcherModule {

    @Provides
    @Singleton
    fun provideIODispatcher(): CoroutineDispatcher = Dispatchers.IO

}

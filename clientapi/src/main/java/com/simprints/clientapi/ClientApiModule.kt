package com.simprints.clientapi

import com.simprints.clientapi.activities.commcare.CommCareAction
import com.simprints.clientapi.activities.commcare.CommCareActivity
import com.simprints.clientapi.activities.commcare.CommCareContract
import com.simprints.clientapi.activities.commcare.CommCarePresenter
import com.simprints.clientapi.activities.libsimprints.LibSimprintsAction
import com.simprints.clientapi.activities.libsimprints.LibSimprintsActivity
import com.simprints.clientapi.activities.libsimprints.LibSimprintsContract
import com.simprints.clientapi.activities.libsimprints.LibSimprintsPresenter
import com.simprints.clientapi.controllers.core.eventData.ClientApiSessionEventsManager
import com.simprints.clientapi.controllers.core.eventData.ClientApiSessionEventsManagerImpl
import com.simprints.clientapi.data.sharedpreferences.SharedPreferencesManager
import com.simprints.clientapi.data.sharedpreferences.SharedPreferencesManagerImpl
import com.simprints.clientapi.tools.ClientApiTimeHelper
import com.simprints.clientapi.tools.ClientApiTimeHelperImpl
import com.simprints.id.di.*
import com.simprints.infra.login.LoginManagerModule
import com.simprints.infra.login.SafetyNetModule
import com.simprints.infra.network.NetworkModule
import com.simprints.infra.realm.RealmModule
import com.simprints.infra.security.SecurityModule
import dagger.Binds
import dagger.Component
import dagger.Module
import dagger.assisted.AssistedFactory
import dagger.hilt.migration.DisableInstallInCheck
import javax.inject.Singleton

@Component(
    dependencies = [EntryPointForDFMs::class],
    modules = [
        ClientApiModule::class,
        SerializerModule::class,
        SecurityModule::class,
        PreferencesModule::class,
        AppModule::class,
        DataModule::class,
        RealmModule::class,
        LoginManagerModule::class,
        SafetyNetModule::class,
        NetworkModule::class
    ]
)
@Singleton
interface ClientApiComponent {

    fun inject(activity: CommCareActivity)
    fun inject(action: LibSimprintsActivity)

    @Component.Builder
    interface Builder {
        fun appDependencies(moduleDependencies: EntryPointForDFMs): Builder
        fun build(): ClientApiComponent
    }

    @AssistedFactory
    interface CommCarePresenterFactory {
        fun create(
            view: CommCareContract.View,
            action: CommCareAction
        ): CommCarePresenter
    }

    @AssistedFactory
    interface LibSimprintsPresenterFactory {
        fun create(
            view: LibSimprintsContract.View,
            action: LibSimprintsAction
        ): LibSimprintsPresenter
    }

}

@DisableInstallInCheck // This is a dagger module
@Module
interface ClientApiModule {
    @Binds
    fun bindClientApiSessionEventsManager(impl: ClientApiSessionEventsManagerImpl): ClientApiSessionEventsManager

    @Binds
    fun bindSharedPreferencesManager(impl: SharedPreferencesManagerImpl): SharedPreferencesManager

    @Binds
    fun bindClientApiTimeHelper(impl: ClientApiTimeHelperImpl): ClientApiTimeHelper
}

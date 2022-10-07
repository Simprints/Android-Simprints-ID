package com.simprints.clientapi

import com.simprints.clientapi.activities.commcare.CommCareAction
import com.simprints.clientapi.activities.commcare.CommCareActivity
import com.simprints.clientapi.activities.commcare.CommCareContract
import com.simprints.clientapi.activities.commcare.CommCarePresenter
import com.simprints.clientapi.activities.errors.ErrorContract
import com.simprints.clientapi.activities.errors.ErrorPresenter
import com.simprints.clientapi.activities.libsimprints.LibSimprintsAction
import com.simprints.clientapi.activities.libsimprints.LibSimprintsActivity
import com.simprints.clientapi.activities.libsimprints.LibSimprintsContract
import com.simprints.clientapi.activities.libsimprints.LibSimprintsPresenter
import com.simprints.clientapi.activities.odk.OdkAction
import com.simprints.clientapi.activities.odk.OdkActivity
import com.simprints.clientapi.activities.odk.OdkContract
import com.simprints.clientapi.activities.odk.OdkPresenter
import com.simprints.clientapi.controllers.core.eventData.ClientApiSessionEventsManager
import com.simprints.clientapi.controllers.core.eventData.ClientApiSessionEventsManagerImpl
import com.simprints.clientapi.data.sharedpreferences.SharedPreferencesManager
import com.simprints.clientapi.data.sharedpreferences.SharedPreferencesManagerImpl
import com.simprints.clientapi.tools.ClientApiTimeHelper
import com.simprints.clientapi.tools.ClientApiTimeHelperImpl
import com.simprints.id.Application
import com.simprints.id.di.*
import com.simprints.infra.config.ConfigManagerModule
import com.simprints.infra.config.DataStoreModule
import com.simprints.infra.enrolment.records.EnrolmentRecordsModule
import com.simprints.infra.login.LoginManagerModule
import com.simprints.infra.login.SafetyNetModule
import com.simprints.infra.network.NetworkModule
import com.simprints.infra.realm.RealmModule
import com.simprints.infra.security.SecurityModule
import dagger.*
import dagger.assisted.AssistedFactory
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.migration.DisableInstallInCheck
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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
        NetworkModule::class,
        ClientApiDispatcherModule::class,
        ConfigManagerModule::class,
        DataStoreModule::class,
        EnrolmentRecordsModule::class,
    ]
)
@Singleton
interface ClientApiComponent {

    companion object {
        fun getComponent(app: Application) =
            DaggerClientApiComponent.builder().application(app).appDependencies(
                EntryPointAccessors.fromApplication(
                    app,
                    EntryPointForDFMs::class.java
                )
            ).build()
    }

    fun inject(activity: CommCareActivity)
    fun inject(activity: LibSimprintsActivity)
    fun inject(activity: OdkActivity)

    @Component.Builder
    interface Builder {
        fun application(@BindsInstance context: Application): Builder
        fun appDependencies(moduleDependencies: EntryPointForDFMs): Builder
        fun build(): ClientApiComponent
    }

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

@DisableInstallInCheck // This is a dagger module
@Module
class ClientApiDispatcherModule {

    @Provides
    @Singleton
    fun provideIODispatcher(): CoroutineDispatcher = Dispatchers.IO

}

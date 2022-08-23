package com.simprints.clientapi

import android.content.Context
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
import com.simprints.id.data.db.subject.SubjectRepository
import com.simprints.id.data.db.subject.SubjectRepositoryImpl
import com.simprints.id.di.EntryPointForDFMs
import com.simprints.id.di.SerializerModule
import com.simprints.infra.security.SecurityManager
import dagger.Binds
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.assisted.AssistedFactory
import dagger.hilt.migration.DisableInstallInCheck
import javax.inject.Singleton

@Component(
    dependencies = [EntryPointForDFMs::class],
    modules = [ClientApiModule::class, SerializerModule::class]
)
@Singleton
interface ClientApiComponent {

    fun inject(activity: CommCareActivity)
    fun inject(action: LibSimprintsActivity)

    @Component.Builder
    interface Builder {
        fun context(@BindsInstance context: Context): Builder
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
    fun bindSubjectRepository(impl: SubjectRepositoryImpl): SubjectRepository

    @Binds
    fun bindClientApiTimeHelper(impl: ClientApiTimeHelperImpl): ClientApiTimeHelper

    @Binds
    fun bindSecurityManager(impl: SecurityManager): SecurityManager
}

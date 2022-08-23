package com.simprints.id.testtools.di

import android.content.Context
import com.simprints.eventsystem.event.remote.EventRemoteDataSource
import com.simprints.id.data.consent.longconsent.LongConsentRepository
import com.simprints.id.data.consent.longconsent.local.LongConsentLocalDataSource
import com.simprints.id.data.consent.longconsent.remote.LongConsentRemoteDataSource
import com.simprints.id.data.db.subject.SubjectRepository
import com.simprints.id.data.db.subject.local.SubjectLocalDataSource
import com.simprints.id.data.images.repository.ImageRepository
import com.simprints.id.di.DataModule
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.login.LoginManager
import com.simprints.infra.realm.RealmWrapper
import com.simprints.testtools.common.di.DependencyRule
import io.mockk.mockk

class TestDataModule(
    private val personLocalDataSourceRule: DependencyRule = DependencyRule.RealRule,
    private val longConsentRepositoryRule: DependencyRule = DependencyRule.RealRule,
    private val longConsentLocalDataSourceRule: DependencyRule = DependencyRule.RealRule,
    private val personRepositoryRule: DependencyRule = DependencyRule.RealRule,
    private val imageRepositoryRule: DependencyRule = DependencyRule.RealRule
) : DataModule() {

    override fun provideSubjectRepository(
        subjectLocalDataSource: SubjectLocalDataSource,
        eventRemoteDataSource: EventRemoteDataSource
    ): SubjectRepository = personRepositoryRule.resolveDependency {
        super.provideSubjectRepository(
            subjectLocalDataSource,
            eventRemoteDataSource
        )
    }

    override fun provideImageRepository(
        context: Context,
        configManager: ConfigManager,
        loginManager: LoginManager
    ): ImageRepository = imageRepositoryRule.resolveDependency {
        super.provideImageRepository(context, configManager, loginManager)
    }

    override fun provideLongConsentLocalDataSource(
        context: Context,
        loginManager: LoginManager
    ): LongConsentLocalDataSource =
        longConsentLocalDataSourceRule.resolveDependency {
            super.provideLongConsentLocalDataSource(
                context,
                loginManager
            )
        }

    override fun provideLongConsentRepository(
        longConsentLocalDataSource: LongConsentLocalDataSource,
        longConsentRemoteDataSource: LongConsentRemoteDataSource
    ): LongConsentRepository =
        longConsentRepositoryRule.resolveDependency {
            super.provideLongConsentRepository(
                longConsentLocalDataSource,
                longConsentRemoteDataSource
            )
        }

    override fun providePersonLocalDataSource(
        realmWrapper: RealmWrapper
    ): SubjectLocalDataSource =
        personLocalDataSourceRule.resolveDependency {
            super.providePersonLocalDataSource(
                mockk()
            )
        }
}


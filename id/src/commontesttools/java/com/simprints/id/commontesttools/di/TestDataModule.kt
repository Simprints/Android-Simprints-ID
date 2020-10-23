package com.simprints.id.commontesttools.di

import android.content.Context
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.data.consent.longconsent.LongConsentLocalDataSource
import com.simprints.id.data.consent.longconsent.LongConsentRemoteDataSource
import com.simprints.id.data.consent.longconsent.LongConsentRepository
import com.simprints.id.data.db.event.remote.EventRemoteDataSource
import com.simprints.id.data.db.project.ProjectRepository
import com.simprints.id.data.db.project.local.ProjectLocalDataSource
import com.simprints.id.data.db.project.remote.ProjectRemoteDataSource
import com.simprints.id.data.db.subject.SubjectRepository
import com.simprints.id.data.db.subject.local.SubjectLocalDataSource
import com.simprints.id.data.images.repository.ImageRepository
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.secure.SecureLocalDbKeyProvider
import com.simprints.id.di.DataModule
import com.simprints.id.network.BaseUrlProvider
import com.simprints.id.network.SimApiClientFactory
import com.simprints.testtools.common.di.DependencyRule
import kotlinx.coroutines.FlowPreview

class TestDataModule(
    private val projectLocalDataSourceRule: DependencyRule = DependencyRule.RealRule,
    private val projectRemoteDataSourceRule: DependencyRule = DependencyRule.RealRule,
    private val projectRepositoryRule: DependencyRule = DependencyRule.RealRule,
    private val personLocalDataSourceRule: DependencyRule = DependencyRule.RealRule,
    private val eventRemoteDataSourceRule: DependencyRule = DependencyRule.RealRule,
    private val longConsentRepositoryRule: DependencyRule = DependencyRule.RealRule,
    private val longConsentLocalDataSourceRule: DependencyRule = DependencyRule.RealRule,
    private val personRepositoryUpSyncHelperRule: DependencyRule = DependencyRule.RealRule,
    private val personRepositoryDownSyncHelperRule: DependencyRule = DependencyRule.RealRule,
    private val personRepositoryRule: DependencyRule = DependencyRule.RealRule,
    private val imageRepositoryRule: DependencyRule = DependencyRule.RealRule
) : DataModule() {

    @FlowPreview
    override fun provideProjectLocalDataSource(
        ctx: Context,
        secureLocalDbKeyProvider: SecureLocalDbKeyProvider,
        loginInfoManager: LoginInfoManager
    ): ProjectLocalDataSource =
        projectLocalDataSourceRule.resolveDependency {
            super.provideProjectLocalDataSource(
                ctx,
                secureLocalDbKeyProvider,
                loginInfoManager
            )
        }

    override fun provideProjectRemoteDataSource(simApiClientFactory: SimApiClientFactory): ProjectRemoteDataSource =
        projectRemoteDataSourceRule.resolveDependency {
            super.provideProjectRemoteDataSource(simApiClientFactory)
        }

    override fun provideProjectRepository(
        projectLocalDataSource: ProjectLocalDataSource,
        projectRemoteDataSource: ProjectRemoteDataSource
    ): ProjectRepository = projectRepositoryRule.resolveDependency {
        super.provideProjectRepository(
            projectLocalDataSource,
            projectRemoteDataSource
        )
    }


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
        baseUrlProvider: BaseUrlProvider
    ): ImageRepository = imageRepositoryRule.resolveDependency {
        super.provideImageRepository(context, baseUrlProvider)
    }

    override fun provideLongConsentLocalDataSource(
        context: Context,
        loginInfoManager: LoginInfoManager
    ): LongConsentLocalDataSource =
        longConsentLocalDataSourceRule.resolveDependency {
            super.provideLongConsentLocalDataSource(
                context,
                loginInfoManager
            )
        }

    override fun provideLongConsentRepository(
        longConsentLocalDataSource: LongConsentLocalDataSource,
        longConsentRemoteDataSource: LongConsentRemoteDataSource,
        crashReportManager: CrashReportManager
    ): LongConsentRepository =
        longConsentRepositoryRule.resolveDependency {
            super.provideLongConsentRepository(
                longConsentLocalDataSource,
                longConsentRemoteDataSource,
                crashReportManager
            )
        }

    @FlowPreview
    override fun providePersonLocalDataSource(
        ctx: Context,
        secureLocalDbKeyProvider: SecureLocalDbKeyProvider,
        loginInfoManager: LoginInfoManager
    ): SubjectLocalDataSource =
        personLocalDataSourceRule.resolveDependency {
            super.providePersonLocalDataSource(
                ctx,
                secureLocalDbKeyProvider,
                loginInfoManager
            )
        }

}

package com.simprints.infra.enrolment.records.repository.commcare

import com.simprints.core.DispatcherIO
import com.simprints.infra.enrolment.records.repository.commcare.domain.CommCareCase
import com.simprints.infra.enrolment.records.room.store.CommCareCaseDao
import com.simprints.infra.enrolment.records.room.store.SubjectsDatabase
import com.simprints.infra.enrolment.records.room.store.SubjectsDatabaseFactory
import com.simprints.infra.enrolment.records.room.store.models.DbCommCareCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

internal class CommCareCaseRepositoryImpl @Inject constructor(
    private val databaseFactory: SubjectsDatabaseFactory,
    @DispatcherIO private val dispatcher: CoroutineDispatcher,
) : CommCareCaseRepository {

    private suspend fun <T> withDatabase(action: suspend (CommCareCaseDao) -> T): T = 
        withContext(dispatcher) {
            databaseFactory.build().commCareCaseDao.let { dao ->
                action(dao)
            }
        }

    override suspend fun saveCase(case: CommCareCase) {
        withDatabase { dao ->
            dao.insertOrUpdateCase(case.toDbModel())
        }
    }

    override suspend fun saveCases(cases: List<CommCareCase>) {
        withDatabase { dao ->
            dao.insertOrUpdateCases(cases.map { it.toDbModel() })
        }
    }

    override suspend fun getCase(caseId: String): CommCareCase? {
        return withDatabase { dao ->
            dao.getCase(caseId)?.toDomain()
        }
    }

    override suspend fun getCaseBySubjectId(subjectId: String): CommCareCase? {
        return withDatabase { dao ->
            dao.getCaseBySubjectId(subjectId)?.toDomain()
        }
    }

    override suspend fun getAllCases(): List<CommCareCase> {
        return withDatabase { dao ->
            dao.getAllCases().map { it.toDomain() }
        }
    }

    override suspend fun deleteCase(caseId: String) {
        withDatabase { dao ->
            dao.deleteCase(caseId)
        }
    }

    override suspend fun deleteCaseBySubjectId(subjectId: String) {
        withDatabase { dao ->
            dao.deleteCaseBySubjectId(subjectId)
        }
    }

    override suspend fun deleteAllCases() {
        withDatabase { dao ->
            dao.deleteAllCases()
        }
    }

    // Extension functions for mapping between domain and database models
    private fun CommCareCase.toDbModel() = DbCommCareCase(
        caseId = caseId,
        subjectId = subjectId,
        lastModified = lastModified,
    )

    private fun DbCommCareCase.toDomain() = CommCareCase(
        caseId = caseId,
        subjectId = subjectId,
        lastModified = lastModified,
    )
}
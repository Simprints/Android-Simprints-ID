package com.simprints.id.data.db.project.local

import com.google.common.truth.Truth
import com.simprints.id.data.db.project.domain.Project
import com.simprints.id.data.db.project.local.models.DbProject
import com.simprints.id.data.db.subject.local.RealmWrapper
import io.mockk.*
import io.realm.Realm
import io.realm.RealmQuery
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class ProjectLocalDataSourceImplTest {
    private lateinit var realm: Realm
    private lateinit var realmWrapperMock: RealmWrapper
    private lateinit var blockCapture: CapturingSlot<(Realm) -> Any>
    private lateinit var localProjects: MutableList<DbProject>
    private lateinit var projectLocalDataSource: ProjectLocalDataSource

    @Before
    fun setUp() {
        localProjects = mutableListOf()
        realmWrapperMock = mockk() {
            blockCapture = slot()
            coEvery {
                useRealmInstance(capture(blockCapture))
            } answers { blockCapture.captured.invoke(realm) }
        }

        realm = mockk() {
            val transaction = slot<Realm.Transaction>()
            every { executeTransaction(capture(transaction)) } answers {
                transaction.captured.execute(realm)
            }
            val insertedProject = slot<DbProject>()
            every { insertOrUpdate(capture(insertedProject)) } answers {
                localProjects.add(insertedProject.captured)
            }
        }

        val captureProjectId = slot<String>()
        val query: RealmQuery<DbProject> = mockk() {
            every {
                equalTo(eq(ProjectLocalDataSourceImpl.PROJECT_ID_FIELD), capture(captureProjectId))
            } answers {
                this@mockk
            }
            every { findFirst() } answers {
                localProjects.firstOrNull()
            }
        }
        every { realm.where(DbProject::class.java) } returns query

        projectLocalDataSource = ProjectLocalDataSourceImpl(realmWrapperMock)
    }

    @Test
    fun `test load success`() = runBlocking {
        val projectId = "PROJECT_ID"
        val project: Project = mockk() { every { id } returns projectId }
        projectLocalDataSource.save(project)
        val dbProject = projectLocalDataSource.load(projectId)
        Truth.assertThat(dbProject).isNotNull()
        Truth.assertThat(dbProject?.id).isEqualTo(projectId)

    }
    @Test
    fun `test load returns null if DB is empty`() = runBlocking {
        val projectId = "PROJECT_ID"
        val dbProject = projectLocalDataSource.load(projectId)
        Truth.assertThat(dbProject).isNull()

    }

    @Test
    fun `test save project`() = runBlocking {
        val projectId = "PROJECT_ID"
        val project: Project = mockk() { every { id } returns projectId }
        projectLocalDataSource.save(project)

        Truth.assertThat(localProjects.size).isEqualTo(1)
        Truth.assertThat(localProjects.first().id).isEqualTo(projectId)
    }
}

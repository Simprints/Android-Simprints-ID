package com.simprints.id.data.db.project.domain

import com.google.common.truth.Truth.assertThat
import com.simprints.id.data.db.project.local.models.DbProject
import com.simprints.id.data.db.project.local.models.fromDbToDomain
import com.simprints.id.data.db.project.local.models.fromDomainToDb
import org.junit.Test

class ProjectTest {

    private val domainProject = Project("id", "name", "description", "creator", "now")
    private val dbProject = DbProject().apply {
        id = "id"
        name = "name"
        description = "description"
        creator = "creator"
        updatedAt = "now"
    }

    @Test
    fun fromDomainToDb() {
        assertThat(dbProject.fromDbToDomain()).isEqualTo(domainProject)
    }

    @Test
    fun fromDbToDomain() {
        val dbProjectFromDomain = domainProject.fromDomainToDb()
        assertThat(dbProjectFromDomain.id).isEqualTo(dbProject.id)
        assertThat(dbProjectFromDomain.name).isEqualTo(dbProject.name)
        assertThat(dbProjectFromDomain.description).isEqualTo(dbProject.description)
        assertThat(dbProjectFromDomain.creator).isEqualTo(dbProject.creator)
        assertThat(dbProjectFromDomain.updatedAt).isEqualTo(dbProject.updatedAt)
    }
}

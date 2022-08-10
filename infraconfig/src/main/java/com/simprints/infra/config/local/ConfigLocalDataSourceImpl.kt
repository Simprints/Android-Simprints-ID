package com.simprints.infra.config.local

import androidx.datastore.core.DataStore
import com.simprints.infra.config.domain.models.Project
import com.simprints.infra.config.local.models.ProtoProject
import com.simprints.infra.config.local.models.toDomain
import com.simprints.infra.config.local.models.toProto
import kotlinx.coroutines.flow.first
import javax.inject.Inject

internal class ConfigLocalDataSourceImpl @Inject constructor(private val projectDataStore: DataStore<ProtoProject>) :
    ConfigLocalDataSource {

    override suspend fun saveProject(project: Project) {
        projectDataStore.updateData { project.toProto() }
    }

    override suspend fun getProject(): Project =
        projectDataStore.data.first().toDomain().let {
            if (it.id == "") {
                throw NoSuchElementException()
            }
            it
        }

}

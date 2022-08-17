package com.simprints.id.data.db.subject.local.models

import com.simprints.core.domain.face.FaceSample
import com.simprints.infra.realm.models.DbFaceSample
import com.simprints.moduleapi.face.responses.entities.IFaceTemplateFormat

fun DbFaceSample.fromDbToDomain(): FaceSample =
    FaceSample(template = template, format = IFaceTemplateFormat.valueOf(format))

fun FaceSample.fromDomainToDb(): DbFaceSample =
    DbFaceSample(
        id = id,
        template = template,
        format = format.name
    )

package com.simprints.id.data.db.remote.people.models

import com.simprints.id.data.db.remote.models.fb_Person

data class RemotePeopleToPost(
    val patients: List<RemotePerson>
)

fun List<fb_Person>.toRemotePeopleToPost(): RemotePeopleToPost =
    RemotePeopleToPost(this.map(fb_Person::toRemotePerson))

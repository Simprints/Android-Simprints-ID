package com.simprints.id.data.db.project.domain

import androidx.annotation.Keep

@Keep
open class Project {

    lateinit var id: String

    lateinit var name: String

    lateinit var description: String

    lateinit var creator: String

    var updatedAt: String? = null

}

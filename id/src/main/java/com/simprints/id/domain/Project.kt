package com.simprints.id.domain


open class Project {

    lateinit var id: String

    var legacyId: String? = null

    lateinit var name: String

    lateinit var description: String

    lateinit var creator: String

    var updatedAt: String? = null

}

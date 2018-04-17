package com.simprints.id.data.db.remote.tools

import com.google.firebase.FirebaseApp
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

object Routes {

    private const val sessionRef = "sessions"
    private const val idEventRef = "id-events"
    private const val idUpdateRef = "id-events-update"
    private const val vfEventRef = "vf-events"
    private const val refusalRef = "refusal-forms"
    private const val junkRef = "junk"

    fun sessionRef(app: FirebaseApp): DatabaseReference =
        Utils.getDatabase(app).reference.child(sessionRef)

    fun idEventRef(app: FirebaseApp, projectId: String): DatabaseReference =
        Utils.getDatabase(app).reference.child(idEventRef).child(projectId)

    fun idUpdateRef(projectId: String): DatabaseReference =
        Utils.getDatabase(null).reference.child(idUpdateRef).child(projectId)

    fun vfEventRef(app: FirebaseApp, projectId: String): DatabaseReference =
        FirebaseDatabase.getInstance(app).reference.child(vfEventRef).child(projectId)

    fun refusalRef(app: FirebaseApp): DatabaseReference =
        Utils.getDatabase(app).reference.child(refusalRef)

    fun junkRef(app: FirebaseApp): DatabaseReference =
        Utils.getDatabase(app).reference.child(junkRef)
}

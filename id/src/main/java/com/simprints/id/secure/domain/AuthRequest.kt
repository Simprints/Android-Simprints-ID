package com.simprints.id.secure.domain

import org.json.JSONObject

data class AuthRequest(var encryptedProjectSecret: String = "", var projectId: String = "", var attestation: JSONObject = JSONObject())

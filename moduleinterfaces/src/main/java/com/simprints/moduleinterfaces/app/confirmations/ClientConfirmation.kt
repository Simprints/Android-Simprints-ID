package com.simprints.moduleinterfaces.app.confirmations


interface ClientConfirmation {

    companion object {
        const val BUNDLE_KEY = "clientConfirmationBundleKey"
    }

    val projectId: String

}

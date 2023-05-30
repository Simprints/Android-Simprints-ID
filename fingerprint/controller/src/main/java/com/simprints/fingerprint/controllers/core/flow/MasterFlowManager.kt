package com.simprints.fingerprint.controllers.core.flow

/**
 * This interface represents a Master flow manager that determines which flow is currently being
 * executed.
 *
 * @see Action
 */
interface MasterFlowManager {

    /**
     * Will be deprecated. Use cases to revamp:
     * 1. Changing the toolbar title in CollectFingerprintsActivity to something generic.
     * 2. Replacing OneToOneMatchEvent and OneToManyMatchEvent with MatchEvent.
     * 3. Handling the UI pause while matching in MatchingActivity.
     */

    fun getCurrentAction(): Action
}

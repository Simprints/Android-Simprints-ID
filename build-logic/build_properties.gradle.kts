extra.apply {

    /**
     * Debug Version code. The version code for staging and production builds are taken from the CI.
     * Read more about our versioning here:
     * https://simprints.atlassian.net/wiki/spaces/KB/pages/1761378305/Releasing+Simprints+ID
     */
    set("VERSION_CODE", 1)

    /**
     * Current release version. This base version name corresponds with the latest release being worked
     * on, and the version of the BFSID API being used. The version name is automatically set by the
     * CI. Read more about our versioning here:
     * https://simprints.atlassian.net/wiki/spaces/KB/pages/1761378305/Releasing+Simprints+ID
     *
     * Dev version >= 2024.2.1 is required for receiving biometric sdk age restrictions
     * Dev version >= 2024.2.2 is required for float quality thresholds
     * Dev version >= 2024.3.0 is required to receive configuration ID
     * Dev version >= 2025.2.0 is required to support enrolment record updates and SimFace configuration
     * Dev version >= 2025.3.0 is required to receive smaples and structured down sync configuration
     */
    set("VERSION_NAME", "2025.3.0")

    /**
     * Build type. The version code describes which build type was used for the build.
     * Read more about our versioning here:
     * https://simprints.atlassian.net/wiki/spaces/KB/pages/1761378305/Releasing+Simprints+ID
     */
    set("VERSION_SUFFIX", "dev")

    /**
     * Build Information. The build information comes from the github actions run info and allows
     * us to track down where an exact build comes from. Read more about our versioning here:
     * https://simprints.atlassian.net/wiki/spaces/KB/pages/1761378305/Releasing+Simprints+ID
     */
    set("VERSION_BUILD", "local")

    /**
     * Set debuggable, default it true so local staging and release builds can be debugged. NOTE: THE
     * CD WILL OVERWRITE TO FALSE AS THE PLAYSTORE DOES NOT ACCEPT DEBUGGABLE TRUE APPS
     */
    set("DEBUGGABLE", true)
    set("DB_ENCRYPTION", true)
}

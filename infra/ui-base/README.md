# UI Base

This module serves as a common foundation for all modules with any kind on UI.

Things that should be stored here:

* Common UI dependencies that are used in all/most feature modules shared via `api()` declaration in modules build file
* Generic utilities that simplify work with UI-related Android API

Things that must not be in this module:

* Dependencies required by one/couple specific modules
  * Add those dependencies to modules in question directly
  * Consider creating convention plugin, if dependency requires copy-pasting configuration
* Any kind business logic

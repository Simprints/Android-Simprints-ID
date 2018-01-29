import platform

HERE = '.'

# Modify to suit local set up.
# Relative paths should be relative to instrumented_test.py

SIMPRINTS_ID_DIR_PATH = f'{HERE}'
CERBERUS_DIR_PATH = f'/tmp/Android-Cerberus'
LOG_DIR_BASE_NAME = f'{SIMPRINTS_ID_DIR_PATH}/testing/logs'

ADB = 'adb'

GRADLEW = 'gradlew'
SIMPRINTS_ID_MODULE_NAME = 'id'
CERBERUS_APP_MODULE_NAME = 'cerberus-app'

SIMPRINTS_ID_PACKAGE_NAME = 'com.simprints.id'
CERBERUS_APP_PACKAGE_NAME = 'com.simprints.cerberusapp'
CERBERUS_MAIN_ACTIVITY_NAME = 'activities.main.MainActivity'

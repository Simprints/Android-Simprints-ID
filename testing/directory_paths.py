import platform

HERE = '' if platform.system() == 'Windows' else '.'

# Modify to suit local set up.
# Relative paths should be relative to instrumented_test.py

SIMPRINTS_ID_DIR_PATH = f'{HERE}'
CERBERUS_DIR_PATH = f'/tmp/Android-Cerberus'
LOG_DIR_BASE_NAME = f'{SIMPRINTS_ID_DIR_PATH}/testing/logs'

# Some devices are known to use the same serial number.
# With default ADB, you cannot run tests with several devices connected if any devices share a serial number.
# Use this custom version of adb that configures devices by their usb port rather than serial number.
# This version of adb only works on Unix systems.
ADB = 'adb' if platform.system() == 'Windows' else f'{SIMPRINTS_ID_DIR_PATH}/testing/adb'
ADB = 'adb'

GRADLEW = 'gradlew.bat' if platform.system() == 'Windows' else 'gradlew'
SIMPRINTS_ID_MODULE_NAME = 'id'
CERBERUS_APP_MODULE_NAME = 'cerberus-app'

SIMPRINTS_ID_PACKAGE_NAME = 'com.simprints.id'
CERBERUS_APP_PACKAGE_NAME = 'com.simprints.cerberusapp'
CERBERUS_MAIN_ACTIVITY_NAME = f'{CERBERUS_APP_PACKAGE_NAME}/{CERBERUS_APP_PACKAGE_NAME}.activities.main.MainActivity'

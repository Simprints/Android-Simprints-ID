from testing.models import Scanner, Device, WifiNetwork
from testing.directory_paths import *


# Generic commands


def gradlew_command(dir_path: str, module_name: str, command: str):
    return f'{dir_path}/{GRADLEW} -p {dir_path}/{module_name} {command}'


def uninstall_apk_command(package_name: str, device: Device):
    return f'{ADB} -s {device.device_id} uninstall {package_name}'


def uninstall_android_test_apk_command(package_name: str, device: Device):
    return f'{ADB} -s {device.device_id} uninstall {package_name}.test'


def install_apk_command(dir_path: str, module_name: str, build_type: str, device: Device):
    return f'{ADB} -s {device.device_id} install -t -d -r ' \
           f'{dir_path}/{module_name}/build/outputs/apk/{build_type}/{module_name}-{build_type}.apk'

def open_apk_command(package_name: str, main_activity: str, device: Device):
    return f'{ADB} -s {device.device_id} shell am start -n {package_name}/{package_name}.{main_activity}'

def install_android_test_apk_command(dir_path: str, module_name: str, build_type: str, device: Device):
    return f'{ADB} -s {device.device_id} install -t -d -r ' \
           f'{dir_path}/' \
           f'{module_name}/build/outputs/apk/androidTest/{build_type}/{module_name}-{build_type}-androidTest.apk'

def run_instrumented_tests_command(dir_path: str, device: Device):
    return f'{dir_path}/{GRADLEW} connectedAndroidTest mergeAndroidReports --continue -Pdevices={device.device_id}'

def query_devices():
    return f'{ADB} devices -l'


# Specific implementations


def simprints_id_test_command(device: Device, test: str, scanner: Scanner = None, wifi_network: WifiNetwork = None):
    return f'{ADB} -s {device.device_id} shell am instrument -w ' + \
           (f'' if scanner is None else f'-e scanner_mac_address \"\'{scanner.mac_address}\'\" ') + \
           (f'' if wifi_network is None else f'-e wifi_network_ssid \"\'{wifi_network.ssid}\'\" ') + \
           (f'' if wifi_network is None else f'-e wifi_network_password \"\'{wifi_network.password}\'\" ') + \
           f'-e class {test} com.simprints.id.test/android.support.test.runner.AndroidJUnitRunner '


def cerberus_app_gradlew_command(command: str):
    return gradlew_command(CERBERUS_DIR_PATH, CERBERUS_APP_MODULE_NAME, command)


def cerberus_app_uninstall_apk_command(device: Device):
    return uninstall_apk_command(CERBERUS_APP_PACKAGE_NAME, device)


def cerberus_app_install_apk_command(build_type: str, device: Device):
    return install_apk_command(CERBERUS_DIR_PATH, CERBERUS_APP_MODULE_NAME, build_type, device)

def cerberus_app_open_apk_command(device: Device):
    return open_apk_command(CERBERUS_APP_PACKAGE_NAME, CERBERUS_MAIN_ACTIVITY_NAME, device)

def simprints_id_gradlew_command(command: str):
    return gradlew_command(SIMPRINTS_ID_DIR_PATH, SIMPRINTS_ID_MODULE_NAME, command)


def simprints_id_uninstall_apk_command(device: Device):
    return uninstall_apk_command(SIMPRINTS_ID_PACKAGE_NAME, device)


def simprints_id_uninstall_android_test_apk_command(device: Device):
    return uninstall_android_test_apk_command(SIMPRINTS_ID_PACKAGE_NAME, device)


def simprints_id_install_apk_command(build_type: str, device: Device):
    return install_apk_command(SIMPRINTS_ID_DIR_PATH, SIMPRINTS_ID_MODULE_NAME, build_type, device)


def simprints_id_install_android_test_apk_command(build_type: str, device: Device):
    return install_android_test_apk_command(SIMPRINTS_ID_DIR_PATH, SIMPRINTS_ID_MODULE_NAME, build_type, device)

def simprints_id_run_instrumented_tests(device: Device):
    return run_instrumented_tests_command(SIMPRINTS_ID_DIR_PATH, device)

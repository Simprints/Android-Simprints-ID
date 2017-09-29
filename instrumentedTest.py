import datetime
import os
import platform
import queue
import subprocess
import sys
import threading
import time

from logging import Formatter, Logger, getLogger, DEBUG, StreamHandler, FileHandler, INFO, WARN, ERROR, CRITICAL
from typing import List

GRADLEW_PATH = 'gradlew.bat' if platform.system() == 'Windows' else './gradlew'

tests = {
    'happy_path_enrol': 'com.simprints.id.happypath.HappyPathEnrolTest',
    'happy_path_identify': 'com.simprints.id.happypath.HappyPathIdentifyTest'
}

commands = {
    'clean builds': (GRADLEW_PATH + ' clean'),
    'assemble endToEndTesting apk': (GRADLEW_PATH + ' assembleEndToEndTesting'),
    'assemble endToEndTesting test apk': (GRADLEW_PATH + ' assembleEndToEndTestingAndroidTest'),
    'devices query': 'adb devices -l',
    'bluetooth on': 'adb -s {0} shell am startservice -a com.simprints.testutilities.bluetooth.action.ON',
    'bluetooth off': 'adb -s {0} shell am startservice -a com.simprints.testutilities.bluetooth.action.OFF',
    'bluetooth pair': 'adb -s {0} shell am startservice --user 0 -a com.simprints.testutilities.bluetooth.action.PAIR '
                      '-e "com.simprints.testutilities.bluetooth.extra.MAC_ADDRESS" "{1}"',
    'bluetooth unpair': 'adb -s {0} shell am startservice --user 0 -a '
                        'com.simprints.testutilities.bluetooth.action.UNPAIR -e '
                        '"com.simprints.testutilities.bluetooth.extra.MAC_ADDRESS" "{1}"',
    'wifi on': 'adb -s {0} shell am startservice -a com.simprints.testutilities.wifi.action.ON',
    'wifi off': 'adb -s {0} shell am startservice -a com.simprints.testutilities.wifi.action.OFF',
    'install endToEndTesting apk': 'adb -s {0} install -t -d -r '
                                   'id/build/outputs/apk/id-endToEndTesting.apk',
    'install endToEndTesting test apk': 'adb -s {0} install -t -d -r '
                                        'id/build/outputs/apk/id-endToEndTesting-androidTest.apk',
    'run test': 'adb -s {0} shell am instrument -w -e coverage true '
                '-e class {1} com.simprints.id.test/android.support.test.runner.AndroidJUnitRunner ',
    'acquire coverage file': 'adb -d -s {0} shell "run-as com.simprints.id cat '
                             '/data/user/0/com.simprints.id/files/coverage.ec" > {1}/coverage.ec',
    'parse coverage file': 'java -jar ./testing/JacocoReportParser.jar -p . -f ./{0}/coverage.ec -c '
                           './id/build/intermediates/classes -s ./id/src/main/java -r ./{0}/coverage_report'
}


class Scanner:
    def __init__(self, scanner_id: str, mac_address: str, hardware_version: int, description: str = ''):
        self.scanner_id: str = scanner_id
        self.mac_address: str = mac_address
        self.hardware_version: int = hardware_version
        self.description: str = description


scanners = {
    'SP576290': Scanner('SP576290', 'F0:AC:D7:C8:CB:22', 6),
    'SP337428': Scanner('SP337428', 'F0:AC:D7:C5:26:14', 6),
    'SP443761': Scanner('SP443761', 'F0:AC:D7:C6:C5:71', 6),
    'SP898185': Scanner('SP898185', 'F0:AC:D7:CD:B4:89', 4)
}


class Device:
    def __init__(self, device_id: str, model: str):
        self.device_id: str = device_id
        self.model: str = model
        self.is_bluetooth_on: bool = None
        self.is_wifi_on: bool = None
        self.bluetooth_paired_list: List[Scanner] = []


class LogState:
    """
    These methods return a LogState to be passed to Run.updateLogFormat()
    They represent different beginning strings for each of the log coressponding to what state the program is in
    Here are the states and when they should be used:

    default()
    [time] name :
    This is the base line for the whole log. All other methods call this one. This is for commands and log output that
    is not directed at any particular device.

    device(arg1: Device)
    [time] name : phone model :
    This is for when commands are directed at a particular device, usually to change the state of the device.

    test(arg1: Device)
    [time] name : phone model : device state :
    This is for executing a test with a preset, known, constant device state.


    """

    @staticmethod
    def default(extra=''):
        fmt = '[%(asctime)s] :{0} %(message)s'.format(extra)
        datefmt = '%Y/%m/%d %H:%M:%S'

        return Formatter(fmt=fmt, datefmt=datefmt)

    @staticmethod
    def device(device: Device, extra=''):
        return LogState.default(' {0:12s} :{1}'.format(device.model, extra))

    @staticmethod
    def test(device: Device):
        bool_to_char = {None: '?', True: '1', False: '0'}
        w = bool_to_char[device.is_wifi_on]
        b = bool_to_char[device.is_bluetooth_on]

        paired_count = len(device.bluetooth_paired_list)
        p = str(paired_count) if 0 <= paired_count <= 9 else '+'

        if paired_count == 0:
            s = 'NONE    '
        elif paired_count == 1:
            s = device.bluetooth_paired_list[0].scanner_id
        else:
            s = 'MULTIPLE'

        return LogState.device(device, ' W{0:1s}-B{1:1s}-P{2:1s}-{3:8s} :'.format(w, b, p, s))


class Run:
    LOG_DIR_BASE_NAME = 'testing/logs'

    if not os.path.exists(LOG_DIR_BASE_NAME):
        os.makedirs(LOG_DIR_BASE_NAME)

    def __init__(self, logger_name, log_commands=True):

        self.log_dir_name = Run.LOG_DIR_BASE_NAME + '/'\
                            + logger_name + '_' + datetime.datetime.now().strftime('%Y-%m-%d_%H-%M-%S')

        if not os.path.exists(self.log_dir_name):
            os.makedirs(self.log_dir_name)

        self.logger: Logger = getLogger(logger_name)
        self.logger.setLevel(DEBUG)

        self.console_handler: StreamHandler = StreamHandler(sys.stdout)
        self.file_handler: FileHandler = FileHandler(self.log_dir_name + '/' + logger_name + '.log', mode='w')

        self.logger.addHandler(self.console_handler)
        self.logger.addHandler(self.file_handler)

        self.update_log_format(LogState.default())

        self.log_commands = log_commands

    @staticmethod
    def reformat_process_output(output: bytes):
        #  The output onto the command line contains a lot of \r and \n characters which add a lot of blank spaces
        return output.decode('utf-8').replace(u'\r\r\n', '').replace(u'\r\n', '').replace(u'\n', '')

    def update_log_format(self, log_state: Formatter):
        self.console_handler.setFormatter(log_state)
        self.file_handler.setFormatter(log_state)

    def log(self, line: str, flag=INFO):
        if flag is DEBUG:
            self.logger.debug(line)
        elif flag is INFO:
            self.logger.info(line)
        elif flag is WARN:
            self.logger.warning(line)
        elif flag is ERROR:
            self.logger.error(line)
        elif flag is CRITICAL:
            self.logger.critical(line)
        else:
            self.logger.info(line)

    def run_and_log(self, command):
        if self.log_commands:
            self.log('>>> ' + command, DEBUG)
        lines = []
        if platform.system() == 'Windows':
            process = subprocess.Popen(command, stdout=subprocess.PIPE, stderr=subprocess.PIPE, bufsize=1)
        else:
            process = subprocess.Popen(command, stdout=subprocess.PIPE, stderr=subprocess.PIPE, bufsize=1, shell=True)

        def enqueue_output(out, this_queue: queue.Queue):
            for thisLine in iter(out.readline, b''):
                this_queue.put(thisLine)
            out.close()
            this_queue.put(None)

        message_queue = queue.Queue()
        thread = threading.Thread(target=enqueue_output, args=(process.stdout, message_queue))
        thread.daemon = True
        thread.start()

        while True:
            while not message_queue.empty():
                line = message_queue.get()
                message_queue.task_done()
                if line is None:
                    return lines
                formatted_line = self.reformat_process_output(line)

                lines.append(formatted_line)
                self.log(formatted_line, INFO)

    ##############
    #  Command methods
    #
    #  Template:
    #
    #  commandMethodName(self, ...):
    #      self.updateLogFormat(LogSate.appropriateLogState())
    #      lines = self.runAndLog(command['theCommand'].format(the, command, arguments))
    #
    #      processedLines = processTheRawOutputtedLines(lines)
    #      updateStateOrLogsIfNecessary(processedLines)
    #
    #      return nothingOrSomeInformationIfNecessary
    #
    ##############

    def clean_builds(self):
        self.update_log_format(LogState.default())
        self.run_and_log(commands['clean builds'])

    def assemble_apk(self):
        self.update_log_format(LogState.default())
        self.run_and_log(commands['assemble endToEndTesting apk'])

    def assemble_test_apk(self):
        self.update_log_format(LogState.default())
        self.run_and_log(commands['assemble endToEndTesting test apk'])

    def devices_query(self):
        self.update_log_format(LogState.default())
        lines = self.run_and_log(commands['devices query'])

        # The first line is "List of devices attached". The last line is blank. Lines in between contain a device.
        # If ADB isn't started, the adb daemon starting log has '*'s
        relevant_lines = []
        for line in lines[1:-1]:
            if line[0] != '*':
                relevant_lines.append(line)
        devices_strs = []
        for line in relevant_lines:
            devices_strs.append(line.split())
        devices = []
        for deviceStr in devices_strs:
            # The 0th element is the device Id
            # The 3rd element is the model name, the first 6 characters are 'model:'
            for deviceStrSection in deviceStr:
                if deviceStrSection[0:6] == 'model:':
                    devices.append(Device(deviceStr[0], deviceStrSection[6:]))
        return devices

    def bluetooth_on(self, device: Device):
        self.update_log_format(LogState.device(device))
        self.run_and_log(commands['bluetooth on'].format(device.device_id))
        time.sleep(5)
        device.is_bluetooth_on = True

    def bluetooth_off(self, device: Device):
        self.update_log_format(LogState.device(device))
        self.run_and_log(commands['bluetooth off'].format(device.device_id))
        time.sleep(5)
        device.is_bluetooth_on = False

    def bluetooth_pair(self, device: Device, scanner: Scanner):
        self.update_log_format(LogState.device(device))
        self.run_and_log(commands['bluetooth pair'].format(device.device_id, scanner.mac_address))
        time.sleep(10)
        device.bluetooth_paired_list.append(scanner)

    def bluetooth_unpair(self, device: Device, scanner: Scanner):
        self.update_log_format(LogState.device(device))
        self.run_and_log(commands['bluetooth unpair'].format(device.device_id, scanner.mac_address))
        time.sleep(5)
        device.bluetooth_paired_list.remove(scanner)

    def wifi_on(self, device: Device):
        self.update_log_format(LogState.device(device))
        self.run_and_log(commands['wifi on'].format(device.device_id))
        time.sleep(10)
        device.is_wifi_on = True

    def wifi_off(self, device: Device):
        self.update_log_format(LogState.device(device))
        self.run_and_log(commands['wifi off'].format(device.device_id))
        time.sleep(5)
        device.is_wifi_on = False

    def install_apk(self, device: Device):
        self.update_log_format(LogState.device(device))
        self.run_and_log(commands['install endToEndTesting apk'].format(device.device_id))

    def install_test_apk(self, device: Device):
        self.update_log_format(LogState.device(device))
        self.run_and_log(commands['install endToEndTesting test apk'].format(device.device_id))

    def run_test(self, device: Device, test_id: str):
        self.update_log_format(LogState.device(device))

        test_dir_name = self.log_dir_name + '/' + test_id

        if not os.path.exists(test_dir_name):
            os.makedirs(test_dir_name)

        test_file_handler: FileHandler = FileHandler(test_dir_name + '/' + test_id + '.log', mode='w')

        self.logger.addHandler(test_file_handler)

        self.run_and_log(commands['run test'].format(device.device_id, tests[test_id]))
        self.run_and_log(commands['acquire coverage file'].format(device.device_id, test_dir_name))
        self.run_and_log(commands['parse coverage file'].format(test_dir_name))

        self.logger.removeHandler(test_file_handler)


def main(scanner_id: str = 'SP443761'):
    start_time = time.perf_counter()
    run = Run('instrumented_test')
    run.log("Hello world!")

    run.clean_builds()
    run.assemble_apk()
    run.assemble_test_apk()

    devices = run.devices_query()

    for device in devices:
        run.update_log_format(LogState.device(device))
        run.install_apk(device)
        run.install_test_apk(device)
        # run.bluetooth_pair(device, scanners[scanner_id])
        run.run_test(device, 'happy_path_enrol')
        run.run_test(device, 'happy_path_identify')
        # run.bluetooth_unpair(device, scanners[scanner_id])
    run.update_log_format(LogState.default())
    run.log('TEST END')

    end_time = time.perf_counter()
    run.log('Total elapsed time: {0}'.format(end_time - start_time))


if __name__ == "__main__":
    main()

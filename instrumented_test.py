import datetime
import os
import queue
import shutil
import subprocess
import sys
import threading
import time
from logging import Formatter, Logger, getLogger, DEBUG, StreamHandler, FileHandler, INFO, WARN, ERROR, CRITICAL

from testing.commands import *
from testing.directory_paths import LOG_DIR_BASE_NAME
from testing.models import Device

buckets = {
    'bucket_01': 'com.simprints.id.bucket01.Bucket01Suite',
}


class LogState:
    """
    These methods return a LogState to be passed to Run.updateLogFormat()
    They represent different beginning strings for each of the logs corresponding to what state the program is in.
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
    def test(device: Device, bucket: str):
        return LogState.device(device, ' {0:9s} :'.format(bucket))


class Run:
    if not os.path.exists(LOG_DIR_BASE_NAME):
        os.makedirs(LOG_DIR_BASE_NAME)

    def __init__(self, logger_name, log_commands=True):

        self.log_dir_name = LOG_DIR_BASE_NAME + '/' \
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

    def update_log_format(self, log_state: Formatter, extra_file_handler: FileHandler = None):
        self.console_handler.setFormatter(log_state)
        self.file_handler.setFormatter(log_state)
        if extra_file_handler is not None:
            extra_file_handler.setFormatter(log_state)

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

    def clean_cerberus_build(self):
        self.run_and_log(cerberus_app_gradlew_command('clean'))

    def assemble_cerberus_apk(self):
        self.run_and_log(cerberus_app_gradlew_command('assembleDebug'))

    def clean_simprints_id_build(self):
        self.run_and_log(simprints_id_gradlew_command('clean'))

    def assemble_simprints_id_apk(self):
        self.run_and_log(simprints_id_gradlew_command('assembleEndToEndTesting'))

    def assemble_simprints_id_test_apk(self):
        self.run_and_log(simprints_id_gradlew_command('assembleEndToEndTestingAndroidTest'))

    def uninstall_cerberus_apk(self, device):
        self.run_and_log(cerberus_app_uninstall_apk_command(device))

    def uninstall_simprints_id_apk(self, device):
        self.run_and_log(simprints_id_uninstall_apk_command(device))

    def uninstall_simprints_id_test_apk(self, device):
        self.run_and_log(simprints_id_uninstall_android_test_apk_command(device))

    def run_cerberus_apk(self, device: Device):
        self.run_and_log(cerberus_app_open_apk_command(device))

    def install_cerberus_apk(self, device: Device):
        self.run_and_log(cerberus_app_install_apk_command('debug', device))

    def install_apk(self, device: Device):
        self.run_and_log(simprints_id_install_apk_command('endToEndTesting', device))

    def install_test_apk(self, device: Device):
        self.run_and_log(simprints_id_install_android_test_apk_command('endToEndTesting', device))

    def query_devices(self):
        lines = self.run_and_log(query_devices())

        # The first line is "List of devices attached". The last line is blank. Lines in between contain a device.
        # If the ADB daemon isn't started or is invalid, there is some preamble that can be ignored.
        relevant_lines = []
        for line in lines[1:-1]:
            if line[0] != '*':
                relevant_lines.append(line)
        devices_strs = [line.split() for line in relevant_lines]
        devices = []
        for device_str in devices_strs:
            # The 0th element is the device Id
            # The 3rd element is the model name, the first 6 characters are 'model:'
            for segment in device_str:
                if segment[0:6] == 'model:':
                    devices.append(Device(device_str[0], segment[6:]))
        return devices

    def run_test(self, device: Device, test_id: str):
        test_dir_name = f'{self.log_dir_name}/{device.model}/{test_id}'

        if not os.path.exists(test_dir_name):
            os.makedirs(test_dir_name)

        test_file_handler: FileHandler = FileHandler(f'{test_dir_name}/{test_id}.log', mode='w')

        self.logger.addHandler(test_file_handler)
        self.update_log_format(LogState.test(device, test_id), test_file_handler)

        self.run_and_log(simprints_id_run_instrumented_tests(device))

        self.logger.removeHandler(test_file_handler)

    def save_results(self, device: Device):
        dir_name_to_save_results_html = f'{self.log_dir_name}/{device.model}/html'
        dir_name_to_save_results_xml = f'{self.log_dir_name}/{device.model}/xml'

        if not os.path.exists(dir_name_to_save_results_html):
            os.makedirs(dir_name_to_save_results_html)

        if not os.path.exists(dir_name_to_save_results_xml):
            os.makedirs(dir_name_to_save_results_xml)

        if os.path.exists("build/reports/androidTests"):
            shutil.move("build/reports/androidTests", dir_name_to_save_results_html)

        if os.path.exists("id/build/outputs/androidTest-results/connected"):
            shutil.move("id/build/outputs/androidTest-results/connected", dir_name_to_save_results_xml)


def main():
    start_time = time.perf_counter()

    run = Run('instrumented_test')
    run.update_log_format(LogState.default())

    run.clean_cerberus_build()
    run.assemble_cerberus_apk()

    run.clean_simprints_id_build()
    # run.assemble_simprints_id_apk()
    # run.assemble_simprints_id_test_apk()

    devices = run.query_devices()

    for device in devices:
        run.update_log_format(LogState.device(device))

        run.uninstall_cerberus_apk(device)
        run.uninstall_simprints_id_apk(device)
        run.uninstall_simprints_id_test_apk(device)

        # run.install_test_apk(device)

        run.install_cerberus_apk(device)
        # Cerberus needs to be in foreground to start services in Android versions
        # https://developer.android.com/about/versions/oreo/android-8.0-changes.html#back-all
        run.run_cerberus_apk(device)
        run.run_test(device, 'instrumentedTests')
        run.save_results(device)

    # run.run_and_log(f'{SIMPRINTS_ID_DIR_PATH}/{GRADLEW} connectedAndroidTest mergeAndroidReports --continue')

    run.update_log_format(LogState.default())
    run.log('TEST END')

    end_time = time.perf_counter()
    run.log('Total time elapsed: {0}'.format(end_time - start_time))


if __name__ == "__main__":
    main()

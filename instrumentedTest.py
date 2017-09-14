import sys
import os
import subprocess
import time
import queue
import threading
import logging

import datetime

commands = {
    'assemble test apk': 'gradlew.bat assembleAndroidTest',
    'devices query': 'adb devices -l',
    'bluetooth on': 'adb -s {0} shell am startservice -a com.simprints.testutilities.bluetooth.action.ON',
    'bluetooth off': 'adb -s {0} shell am startservice -a com.simprints.testutilities.bluetooth.action.OFF',
    'bluetooth pair': 'adb -s {0} shell am startservice -a com.simprints.testutilities.bluetooth.action.PAIR -e '
                      '"com.simprints.testutilities.bluetooth.extra.MAC_ADDRESS" "{1}"',
    'bluetooth unpair': 'adb -s {0} shell am startservice -a com.simprints.testutilities.bluetooth.action.UNPAIR -e '
                        '"com.simprints.testutilities.bluetooth.extra.MAC_ADDRESS" "{1}"',
    'wifi on': 'adb -s {0} shell am startservice -a com.simprints.testutilities.wifi.action.ON',
    'wifi off': 'adb -s {0} shell am startservice -a com.simprints.testutilities.wifi.action.OFF',
    'install test apk': 'adb -s {0} install -t -d -r id/build/outputs/apk/id-debug-androidTest.apk',
    'run example tests': 'adb -s {0} shell am instrument -w com.simprints.testutilities.test/android.support.test'
                         '.runner.AndroidJUnitRunner ',
    'run tests': 'adb -s {0} shell am instrument -w '
                 '-e class com.simprints.id.HappyPathEnrolTest'
                 ' com.simprints.id.test/android.support.test.runner.AndroidJUnitRunner '
}


class Scanner:
    def __init__(self, scannerId, macAddress, hardwareVersion, description=''):
        self.scannerId = scannerId
        self.macAddress = macAddress
        self.hardwareVersion = hardwareVersion
        self.description = description


scanners = {
    'SP576290': Scanner('SP576290', 'F0:AC:D7:C8:CB:22', 6),
    'SP337428': Scanner('SP337428', 'F0:AC:D7:C5:26:14', 6),
    'SP443761': Scanner('SP443761', 'F0:AC:D7:C6:C5:71', 6),
    'SP898185': Scanner('SP898185', 'F0:AC:D7:CD:B4:89', 4)
}


class Device:
    def __init__(self, deviceId, model):
        self.deviceId = deviceId
        self.model = model
        self.isBluetoothOn = None
        self.isWifiOn = None
        self.bluetoothPairedList = []


class LogState:
    """
    These methods return a logState to be passed to Run.updateLogFormat()
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

        return logging.Formatter(fmt=fmt, datefmt=datefmt)

    @staticmethod
    def device(device: Device, extra=''):
        return LogState.default(' {0:12s} :{1}'.format(device.model, extra))

    @staticmethod
    def test(device: Device):
        W = '?'
        if device.isWifiOn is True:
            W = '1'
        if device.isWifiOn is False:
            W = '0'

        B = '?'
        if device.isWifiOn is True:
            B = '1'
        if device.isWifiOn is False:
            B = '0'

        p = len(device.bluetoothPairedList)
        P = '?'
        if 0 <= p <= 9:
            P = str(p)
        elif p > 9:
            P = '+'

        S = 'UNKNOWN '
        if p == 1:
            S = device.bluetoothPairedList[0].scannerId
        if p > 1:
            S = 'MULTIPLE'
        if p == 0:
            S = 'NONE    '

        return LogState.device(device, ' W{0:1s}-B{1:1s}-P{2:1s}-{3:8s} :'.format(W, B, P, S))


class Run:

    logDirName = 'testing/logs'

    if not os.path.exists(logDirName):
        os.makedirs(logDirName)

    def __init__(self, loggerName, logFileName=None, logCommands=True):

        if logFileName is None:
            logFileName = loggerName + '_' + datetime.datetime.now().strftime('%Y-%m-%d_%H-%M-%S')

        self.logger = logging.getLogger(loggerName)
        self.logger.setLevel(logging.DEBUG)

        self.consoleHandler = logging.StreamHandler(sys.stdout)
        self.fileHandler = logging.FileHandler(Run.logDirName + '/' + logFileName + '.log', mode='w')

        self.logger.addHandler(self.consoleHandler)
        self.logger.addHandler(self.fileHandler)

        self.updateLogFormat(LogState.default())

        self.logCommands = logCommands

    @staticmethod
    def reformatProcessOutput(output: bytes):
        #  The output onto the command line contains a lot of \r and \n characters which add a lot of blank spaces
        return output.decode('utf-8').replace(u'\r\r\n', '').replace(u'\r\n', '')

    def updateLogFormat(self, logState: logging.Formatter):
        self.consoleHandler.setFormatter(logState)
        self.fileHandler.setFormatter(logState)

    def log(self, line: str, flag=logging.INFO):
        if flag is logging.DEBUG:
            self.logger.debug(line)
        elif flag is logging.INFO:
            self.logger.info(line)
        elif flag is logging.WARN:
            self.logger.warning(line)
        elif flag is logging.ERROR:
            self.logger.error(line)
        elif flag is logging.CRITICAL:
            self.logger.critical(line)
        else:
            self.logger.info(line)

    def runAndLog(self, command):
        if self.logCommands:
            self.log('>>> ' + command, logging.DEBUG)
        lines = []
        process = subprocess.Popen(command, stdout=subprocess.PIPE, stderr=subprocess.PIPE, bufsize=1)

        def enqueueOutput(out, thisQueue: queue.Queue):
            for thisLine in iter(out.readline, b''):
                thisQueue.put(thisLine)
            out.close()
            thisQueue.put(None)

        messageQueue = queue.Queue()
        thread = threading.Thread(target=enqueueOutput, args=(process.stdout, messageQueue))
        thread.daemon = True
        thread.start()

        while True:
            while not messageQueue.empty():
                line = messageQueue.get()
                messageQueue.task_done()
                if line is None:
                    return lines
                formattedLine = self.reformatProcessOutput(line)

                lines.append(formattedLine)
                self.log(formattedLine, logging.INFO)

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

    def assembleTestApk(self):
        self.updateLogFormat(LogState.default())
        self.runAndLog(commands['assemble test apk'])

    def devicesQuery(self):
        self.updateLogFormat(LogState.default())
        lines = self.runAndLog(commands['devices query'])

        # The first line is "List of devices attached". The last line is blank. Lines in between contain a device.
        # If ADB isn't started, the adb daemon starting log has '*'s
        relevantLines = []
        for line in lines[1:-1]:
            if line[0] != '*':
                relevantLines.append(line)
        devicesStrs = []
        for line in relevantLines:
            devicesStrs.append(line.split())
        devices = []
        for deviceStr in devicesStrs:
            # The 0th element is the device Id
            # The 3rd element is the model name, the first 6 characters are 'model:'
            devices.append(Device(deviceStr[0], deviceStr[3][6:]))
        return devices

    def bluetoothOn(self, device: Device):
        self.updateLogFormat(LogState.device(device))
        self.runAndLog(commands['bluetooth on'].format(device.deviceId))
        time.sleep(5)
        device.isBluetoothOn = True

    def bluetoothOff(self, device: Device):
        self.updateLogFormat(LogState.device(device))
        self.runAndLog(commands['bluetooth off'].format(device.deviceId))
        time.sleep(5)
        device.isBluetoothOn = False

    def bluetoothPair(self, device: Device, scanner: Scanner):
        self.updateLogFormat(LogState.device(device))
        self.runAndLog(commands['bluetooth pair'].format(device.deviceId, scanner.macAddress))
        time.sleep(10)
        device.bluetoothPairedList.append(scanner)

    def bluetoothUnpair(self, device: Device, scanner: Scanner):
        self.updateLogFormat(LogState.device(device))
        self.runAndLog(commands['bluetooth unpair'].format(device.deviceId, scanner.macAddress))
        time.sleep(5)
        device.bluetoothPairedList.remove(scanner)

    def wifiOn(self, device: Device):
        self.updateLogFormat(LogState.device(device))
        self.runAndLog(commands['wifi on'].format(device.deviceId))
        time.sleep(10)
        device.isWifiOn = True

    def wifiOff(self, device: Device):
        self.updateLogFormat(LogState.device(device))
        self.runAndLog(commands['wifi off'].format(device.deviceId))
        time.sleep(5)
        device.isWifiOn = False

    def installTestApk(self, device: Device):
        self.updateLogFormat(LogState.device(device))
        self.runAndLog(commands['install test apk'].format(device.deviceId))

    def runExampleTests(self, device: Device):
        self.updateLogFormat(LogState.test(device))
        self.runAndLog(commands['run example tests'].format(device.deviceId))

    def runTests(self, device: Device):
        self.updateLogFormat(LogState.device(device))
        self.runAndLog(commands['run tests'].format(device.deviceId))


def main():
    run = Run('instrumented_test')
    run.log("Hello world!")
    run.assembleTestApk()
    devices = run.devicesQuery()
    for device in devices:
        run.updateLogFormat(LogState.device(device))
        run.log('Preparing device for instrumented test...')
        # run.wifiOn(device)
        # run.bluetoothOn(device)
        run.bluetoothPair(device, scanners['SP443761'])
        run.installTestApk(device)
        run.runTests(device)
    run.updateLogFormat(LogState.default())
    run.log('TEST END')


if __name__ == "__main__":
    main()

# Firmware Updates
The firmware updates invovle 3 main components, __STM__, __Cypress__ & __Un20__.


#### STM Over The Air Update

This is a process that involves updating the __STM__ firmware on the Vero 2 scanner, by transferring new version of the firmware bytes to the scanner.

__Note__: The Stm firmware is responsible for controling the Bluetooth module, the LEDs, powering the fingerprint scanner, and is like a bridge between the Bluetooth and Fingerprint modules. 


##### OTA Process 
There are a series of steps involved in updating the Stm firmware, and here is an ordered list of the steps and details of what occurs in each step.

Starting with the fingerprint scanner in __Root-Mode__, 
1. __Enter STM-Mode__: The first step is to run the command for the scanner to enter a mode (STM-Mode) to accept firmware updates for the __STM__ component.
2. __Reconnect After Entering Stm-Mode__: Next step is to reconnect to the scanner after entering STM-Mode because the scanner device is rstarted and the bluetooth connection is lost, when entering STM-Mode from Root-Mode.
3. __Re-Enter STM-Mode__: After reconnecting, we need to make the same call to enter STM mode again to ensure that, after the scanner has been restarted, it is now ready to receive the STM firmware (bytes) update.
4. __Transfer Firmware Bytes__: The new version of the firmware (bytes) are transferred, via bluetooth, to the scanner device. Once the transfer is complete, the scanner will restart and on the next start, the device will try to run the new version of the STM firmware.
5. __Reconnect After Transfer__: The scanner is now disconnect after file-transfer & restart, hence the next step is to reconnect the scanner.
6. __Validate Firmware Version__: The next step is to validate the current running version of STM firmware, and to do that, we need to enter the Main-Mode and run Main-Commands to validate the current running firmware version.
7. __Reconnect Scanner & Update Firmware Version__: Once the version of firmware has been validated as the new version, we finally need to set/update that new version info on the scanner, to do that we need to go back to the Root-Mode by reconnecting to the scanner, and then sending commands to set the new firmware verwsion info.





#### Un20 Over The Air Update

This is a process that involves updating the __Un20__ firmware on the Vero 2 scanner, by transferring new version of the firmware bytes to the scanner.

__Note__: The Un20 firmware is the Scanning module on the scanner, that captures the fingerprint image (i.e. the area where the user places their finger). 


##### OTA Process 
This is a series of ordered steps involved in updating the Un20 firmware, and details of what occurs in each step.

Starting with the fingerprint scanner in __Root-Mode__, 
1. __Enter Main-Mode__: The first step is to run the command for the scanner to enter Main-Mode, as the Un20 OTA commands are run in the main-mode (with main-commands).
2. __Turn on Un20 & Await StateChange Event__: As the Un20 is always off, to preserve battery life, we first turn it on the Un20 and wait for state change event (i.e. a feedback notification indicating that the Un20 is now on).
3. __Transfer Firmware Bytes__: The new version of the firmware (bytes) are transferred to the scanner device.
4. __Await Cache Commit__: Once the transfer is complete, some time is needed for the Un20 to write (commit) the new bytes to the system. so we wait for some time for the bytes to be commited to system.
5. __Turn off & Turn on Un20__: After the bytes are committed to system, we restart (turning off & on) the Un20, so it runs the new version of the Un20 firmware.
6. __Validate Firmware Version__: The next step is to validate the current running version of Un20 firmware, so we run commands to validate the current running firmware version.
7. __Reconnect Scanner & Update Firmware Version__: Once the version of firmware has been validated as the new version, we finally need to set/update that new Un20 firmware version info on the scanner, to do that we need to go back to the Root-Mode by reconnecting to the scanner, and then sending commands to set the new firmware verwsion info.




#### Cypress Over The Air Update

This is a process that involves updating the __Cypress__ firmware on the Vero 2 scanner, by transferring new version of the firmware bytes to the scanner.

__Note__: The Cypress firmware is the Bluetooth module on the scanner. 


##### OTA Process 
This is a series of ordered steps involved in updating the Cypress firmware, and details of what occurs in each step.

Starting with the fingerprint scanner in __Root-Mode__, 
1. __Enter Cypress-Mode__: The first step is to run the command for the scanner to enter Cypress-Mode to accept firmware updates for the __Cypress__ component.
2. __Transfer Firmware Bytes__: The new version of the firmware (bytes) are transferred, via bluetooth, to the scanner device. Once the transfer is complete, the scanner will restart and on the next start, the device will try to run the new version of the Cypress firmware.
3. __Reconnect After Transfer__: The scanner is now disconnect after file-transfer & restart, hence the next step is to reconnect the scanner.
4. __Validate Firmware Version__: The next step is to validate the current running version of Cypress firmware, and to do that, we need to enter the Main-Mode and run Main-Commands to validate the current running firmware version.
5. __Reconnect Scanner & Update Firmware Version__: Once the version of firmware has been validated as the new version, we finally need to set/update that new version info on the scanner, to do that we need to go back to the Root-Mode by reconnecting to the scanner, and then sending commands to set the new firmware verwsion info.











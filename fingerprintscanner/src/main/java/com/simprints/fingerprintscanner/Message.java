package com.simprints.fingerprintscanner;

import com.simprints.fingerprintscanner.enums.CRASH_LOG_STATUS;
import com.simprints.fingerprintscanner.enums.HARDWARE_CONFIG;
import com.simprints.fingerprintscanner.enums.LED;
import com.simprints.fingerprintscanner.enums.LED_STATE;
import com.simprints.fingerprintscanner.enums.MESSAGE_STATUS;
import com.simprints.fingerprintscanner.enums.MESSAGE_TYPE;
import com.simprints.fingerprintscanner.enums.SDK_ERROR;
import com.simprints.fingerprintscanner.enums.UN20_STATE;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Locale;

import androidx.annotation.NonNull;

import static com.simprints.fingerprintscanner.ScannerUtils.log;

@SuppressWarnings({"unused", "WeakerAccess"})
public class Message {

    // Request factory

    public static Message captureImage() {
        return new Message(0, MESSAGE_TYPE.CAPTURE_IMAGE);
    }

    public static Message getSensorInfo() {
        return new Message(0, MESSAGE_TYPE.GET_SENSOR_INFO);
    }

    public static Message pair(byte[] macAddress) throws IllegalArgumentException {
        if (macAddress.length != 6)
            throw new IllegalArgumentException("Invalid mac address array size");

        Message message = new Message(6, MESSAGE_TYPE.PAIR);
        for (int i = 0; i < macAddress.length; i++) {
            message.bytes.put(CONTENT_OFFSET + i * BYTE_SIZE, macAddress[i]);
        }
        return message;
    }

    public static Message setSensorConfig(short powerOffTimeoutSecs, short un20IdleTimeoutSecs)
    {
        Message message = new Message(14, MESSAGE_TYPE.SET_SENSOR_CONFIG);
        message.bytes.putShort(CONTENT_OFFSET, powerOffTimeoutSecs);
        message.bytes.putShort(CONTENT_OFFSET + SHORT_SIZE, un20IdleTimeoutSecs);
        return message;
    }

    public static Message setUI(boolean enableTrigger, boolean setLeds, boolean triggerVibrate,
                                LED_STATE[] leds, short vibrationDuration)
            throws IllegalArgumentException
    {
        if (leds.length != LED.COUNT)
            throw new IllegalArgumentException("Invalid led states array size");

        Message message = new Message(5 + LED.COUNT, MESSAGE_TYPE.SET_UI);
        message.bytes.put(CONTENT_OFFSET, (byte) (enableTrigger ? 1 : 0));
        message.bytes.put(CONTENT_OFFSET + BYTE_SIZE, (byte) (setLeds ? 1 : 0));
        message.bytes.put(CONTENT_OFFSET + 2 * BYTE_SIZE, (byte) (triggerVibrate ? 1 : 0));
        for (int i = 0; i < LED.COUNT; i++) {
            message.bytes.put(CONTENT_OFFSET + (3 + i) * BYTE_SIZE, (byte) (leds[i].ordinal()));
        }
        message.bytes.putShort(CONTENT_OFFSET + (3 + LED.COUNT) * BYTE_SIZE, vibrationDuration);
        return message;
    }

    public static Message getImageFragment(short fragmentNo) {
        Message message = new Message(2, MESSAGE_TYPE.GET_IMAGE_FRAGMENT);
        message.bytes.putShort(CONTENT_OFFSET, fragmentNo);
        return message;
    }

    public static Message imageQuality() {
        return new Message(0, MESSAGE_TYPE.IMAGE_QUALITY);
    }

    public static Message generateTemplate() {
        return new Message(0, MESSAGE_TYPE.GENERATE_TEMPLATE);
    }

    public static Message getTemplateFragment(short fragmentNo) {
        Message message = new Message(2, MESSAGE_TYPE.GET_TEMPLATE_FRAGMENT);
        message.bytes.putShort(CONTENT_OFFSET, fragmentNo);
        return message;
    }

    public static Message sendOtaPacket(int packet_number, int number_of_data_bytes, String data) {
        int FIXED_PACKET_DATA_SIZE = 800;
        int number_of_extra_padding_bytes = FIXED_PACKET_DATA_SIZE - number_of_data_bytes;
        assert(number_of_extra_padding_bytes >= 0);
        int message_size = FIXED_PACKET_DATA_SIZE + INT_SIZE + INT_SIZE;
        Message message = new Message(message_size, MESSAGE_TYPE.SEND_OTA_DATA_PACKET);
        message.bytes.putInt(CONTENT_OFFSET, packet_number);
        message.bytes.putInt(CONTENT_OFFSET + INT_SIZE, number_of_data_bytes);
        for (int i = 0; i < number_of_data_bytes; i++) {
            message.bytes.put(CONTENT_OFFSET + INT_SIZE + INT_SIZE + (i) * BYTE_SIZE, (data.getBytes()[i]));
        }
        return message;
    }

    public static Message metadataOfImage(int size, short crc) {
        int message_size = INT_SIZE + SHORT_SIZE;
        Message message = new Message(message_size, MESSAGE_TYPE.SET_OTA_META_DATA);
        message.bytes.putInt(CONTENT_OFFSET, size);
        message.bytes.putShort(CONTENT_OFFSET + INT_SIZE, crc);
        return message;
    }

    public static Message setBank(char bankId, char reset, char force_switch) {
        int message_size = BYTE_SIZE + BYTE_SIZE + BYTE_SIZE;
        Message message = new Message(message_size, MESSAGE_TYPE.SET_RUNNING_BANK);
        message.bytes.put(CONTENT_OFFSET, (byte) bankId);
        message.bytes.put(CONTENT_OFFSET + BYTE_SIZE, (byte) reset);
        message.bytes.put(CONTENT_OFFSET + BYTE_SIZE + BYTE_SIZE, (byte) force_switch);
        return message;
    }

    public static Message getBank() {
        return new Message(0, MESSAGE_TYPE.GET_RUNNING_BANK);
    }

    public static Message crashVero(char mode) {
        Message message = new Message(BYTE_SIZE, MESSAGE_TYPE.CRASH_FIRMWARE);
        message.bytes.put(CONTENT_OFFSET, (byte) mode);
        return message;
    }

    public static Message UN20Shutdown() {
        return new Message(0, MESSAGE_TYPE.UN20_SHUTDOWN);
    }

    public static Message UN20Wakeup() {
        return new Message(0, MESSAGE_TYPE.UN20_WAKEUP);
    }

    public static Message disableFingerCheck() {
        return new Message(0, MESSAGE_TYPE.DISABLE_FINGER_CHECK);
    }

    public static Message enableFingerCheck() {
        return new Message(0, MESSAGE_TYPE.ENABLE_FINGER_CHECK);
    }

    public static Message getCrashLog() {
        return new Message(0, MESSAGE_TYPE.GET_CRASH_LOG);
    }

    public static Message setHardwareConfig(HARDWARE_CONFIG hwConfig) {
        Message message = new Message(1, MESSAGE_TYPE.SET_HARDWARE_CONFIG);
        message.bytes.put(CONTENT_OFFSET, (byte) hwConfig.ordinal());
        return message;
    }

    public static Message timeout() {
        return new Message(0, MESSAGE_TYPE.NONE);
    }


    // Message reading helpers

    public MESSAGE_TYPE getMessageType() {
        return MESSAGE_TYPE.fromId(bytes.get(TYPE_OFFSET) & 0x7f);
    }

    public MESSAGE_STATUS getMessageStatus() {
        return MESSAGE_STATUS.fromId(bytes.get(STATUS_OFFSET));
    }

    public boolean isReply() {
        return bytes.get(TYPE_OFFSET) >= MSG_REPLY;
    }

    public SDK_ERROR getSDKErr() {
        if (!this.isReply() || this.getMessageStatus() != MESSAGE_STATUS.SDK_ERROR_CODE) {
            throw new IllegalStateException("This message is not a sensor info reply");
        }
        return SDK_ERROR.fromId(bytes.getShort(CONTENT_OFFSET));
    }

    public short getUcVersion() {
        if (!this.isReply() || this.getMessageType() != MESSAGE_TYPE.GET_SENSOR_INFO) {
            throw new IllegalStateException("This message is not a sensor info reply");
        }
        return bytes.getShort(CONTENT_OFFSET + ADDR_SIZE);
    }

    public byte getBankValue() {
        if (!this.isReply() || this.getMessageType() != MESSAGE_TYPE.GET_RUNNING_BANK) {
            throw new IllegalStateException("This message is not a sensor info reply");
        }
        return bytes.get(CONTENT_OFFSET);
    }

    public short getUn20Version() {
        if (!this.isReply() || this.getMessageType() != MESSAGE_TYPE.GET_SENSOR_INFO) {
            throw new IllegalStateException("This message is not a sensor info reply");
        }
        return bytes.getShort(CONTENT_OFFSET + ADDR_SIZE + SHORT_SIZE);
    }

    public short getBatteryLevel1() {
        if (!this.isReply() || this.getMessageType() != MESSAGE_TYPE.GET_SENSOR_INFO) {
            throw new IllegalStateException("This message is not a sensor info reply");
        }
        return bytes.getShort(CONTENT_OFFSET + ADDR_SIZE + 2 * SHORT_SIZE);
    }

    public short getBatteryLevel2() {
        if (!this.isReply() || this.getMessageType() != MESSAGE_TYPE.GET_SENSOR_INFO) {
            throw new IllegalStateException("This message is not a sensor info reply");
        }
        return bytes.getShort(CONTENT_OFFSET + ADDR_SIZE + 3 * SHORT_SIZE);
    }

    public boolean getCrashLogValid() {
        if (!this.isReply() || this.getMessageType() != MESSAGE_TYPE.GET_SENSOR_INFO) {
            throw new IllegalStateException("This message is not a sensor info reply");
        }
        return bytes.getShort(CONTENT_OFFSET + ADDR_SIZE + 4 * SHORT_SIZE) != 0;
    }

    public byte getHardwareVersion() {
        if (!this.isReply() || this.getMessageType() != MESSAGE_TYPE.GET_SENSOR_INFO) {
            throw new IllegalStateException("This message is not a sensor info reply");
        }
        return bytes.get(CONTENT_OFFSET + ADDR_SIZE + 4 * SHORT_SIZE + BYTE_SIZE);
    }

    public UN20_STATE getUn20State() {
        if (!this.isReply() || this.getMessageType() != MESSAGE_TYPE.GET_SENSOR_INFO) {
            throw new IllegalStateException("This message is not a sensor info reply");
        }
        return UN20_STATE.fromId(bytes.get(CONTENT_OFFSET + ADDR_SIZE + 4 * SHORT_SIZE + 2 * BYTE_SIZE));
    }

    public short getFragmentNumber() throws IllegalStateException {
        if (!this.isReply() || (this.getMessageType() != MESSAGE_TYPE.GET_TEMPLATE_FRAGMENT &&
                this.getMessageType() != MESSAGE_TYPE.GET_IMAGE_FRAGMENT)) {
            throw new IllegalStateException("This message is not a fragment reply");
        }
        return bytes.getShort(CONTENT_OFFSET);
    }

    public void writeFragmentBytesIn(ByteArrayOutputStream dest)
            throws IllegalStateException
    {
        if (!this.isReply() || (this.getMessageType() != MESSAGE_TYPE.GET_TEMPLATE_FRAGMENT &&
                this.getMessageType() != MESSAGE_TYPE.GET_IMAGE_FRAGMENT)) {
            throw new IllegalStateException("This message is not a fragment reply");
        }
        short fragmentLength = bytes.getShort(CONTENT_OFFSET + SHORT_SIZE);
        dest.write(bytes.array(), CONTENT_OFFSET + 2 * SHORT_SIZE + BYTE_SIZE, fragmentLength);
    }

    public boolean isLastFragment() throws IllegalStateException {
        if (!this.isReply() || (this.getMessageType() != MESSAGE_TYPE.GET_TEMPLATE_FRAGMENT &&
                this.getMessageType() != MESSAGE_TYPE.GET_IMAGE_FRAGMENT)) {
            throw new IllegalStateException("This message is not a fragment reply");
        }
        return bytes.get(CONTENT_OFFSET + 2 * SHORT_SIZE) != 0;
    }

    public short getImageQuality() throws IllegalStateException {
        if (!this.isReply() || this.getMessageType() != MESSAGE_TYPE.IMAGE_QUALITY) {
            throw new IllegalStateException("This message is not an image quality reply");
        }
        return bytes.getShort(CONTENT_OFFSET);
    }

    public CRASH_LOG_STATUS getCrashLogStatus() throws IllegalStateException {
        if (!this.isReply() || this.getMessageType() != MESSAGE_TYPE.GET_CRASH_LOG) {
            throw new IllegalStateException("This message is not a crash log reply");
        }
        int statusId = Math.min(bytes.get(CONTENT_OFFSET), CRASH_LOG_STATUS.UNKNOWN.ordinal());
        return CRASH_LOG_STATUS.fromId(statusId);
    }

    // Message sending and receiving helpers

    /**
     * Make a blocking read on the specified stream
     *
     * @param is Stream to read from
     * @return The message received
     * @throws IOException In particular if the end of the input stream is reached
     */
    public static Message blockingReceiveFrom(@NonNull InputStream is, boolean doLog) throws IOException {
        // Reads header
        short headerLen = Message.TYPE_OFFSET;
        ByteBuffer header = ByteBuffer.allocate(headerLen);
        header.order(ByteOrder.LITTLE_ENDIAN);
        int read;

        for (int totalRead = 0; totalRead < headerLen; totalRead += read) {
            read = is.read(header.array(), totalRead, headerLen - totalRead);
            if (read == -1)
                throw new IOException("End of stream reached");
        }
        if (doLog) log(String.format("blockingReceiveFrom(): received header %s.",
                hexString(header, 0, headerLen)));

        if (header.getInt(0) != HEADER_BYTES)
            throw new IOException("End of stream reached");

        // Reads body
        short msgLen = header.getShort(Message.INT_SIZE);
        Message msg = new Message(msgLen - Message.OVERHEAD);
        msg.bytes.put(header.array());
        int bodyLen = msgLen - headerLen;

        for (int totalRead = 0; totalRead < bodyLen; totalRead += read) {
            read = is.read(msg.bytes.array(), headerLen + totalRead, bodyLen - totalRead);
            if (read == -1)
                throw new IOException("End of stream reached");
        }
        if (doLog) log(String.format("blockingReceiveFrom(): received body %s.",
                hexString(msg.bytes, headerLen, bodyLen)));
        if (doLog) log(String.format("blockingReceiveFrom(): received %s, status %s.",
                msg.getMessageType(), msg.getMessageStatus().name()));
        return msg;
    }

    /**
     * Perform a blocking write on the specified stram
     *
     * @param os Stream to write to
     * @throws IOException In particular if the end of the input stream is reached
     */
    public void sendTo(OutputStream os) throws IOException {
        os.write(bytes.array(), 0, bytes.getShort(4));
        os.flush();
    }

    public void sendToWithLogging(OutputStream os) throws IOException {
        sendTo(os);
        log(String.format(Locale.UK,
                "sendTo(): sent %d bytes : %s.", bytes.getShort(4),
                hexString(bytes, 0, bytes.getShort(4))));
    }



    public void copy(Message message) {
        this.bytes = message.bytes;
    }

    public byte[] getByteArray() {
        return bytes.array();
    }

    // Private

    private final static int ADDR_SIZE = 6;
    private final static int INT_SIZE = 4;
    private final static int SHORT_SIZE = 2;
    private final static int BYTE_SIZE = 1;
    private final static int HEADER_BYTES = 0xFAFAFAFA;
    private final static int FOOTER_BYTES = 0xF5F5F5F5;
    private final static int OVERHEAD = (INT_SIZE + SHORT_SIZE + BYTE_SIZE + BYTE_SIZE + INT_SIZE);
    private final static int LENGTH_OFFSET = INT_SIZE;
    private final static int TYPE_OFFSET = (INT_SIZE + SHORT_SIZE);
    private final static int STATUS_OFFSET = (INT_SIZE + SHORT_SIZE + BYTE_SIZE);
    private final static int CONTENT_OFFSET = (INT_SIZE + SHORT_SIZE + BYTE_SIZE + BYTE_SIZE);
    private final static byte MSG_REPLY = (byte) 0x80;

    public static String hexString(ByteBuffer bytes, int start, int length) {
        StringBuilder hexString = new StringBuilder();
        for (int i = start; i < Math.min(bytes.array().length, start + length); i++) {
            hexString.append(String.format("%02x ", bytes.get(i)));
        }
        return hexString.toString();
    }

    private ByteBuffer bytes;

    private Message(int length) {
        bytes = ByteBuffer.allocate((short) (length + OVERHEAD));
        bytes.order(ByteOrder.LITTLE_ENDIAN);
    }

    private Message(int length, MESSAGE_TYPE type) {
        this(length);
        bytes.putInt(0, HEADER_BYTES);
        bytes.putShort(LENGTH_OFFSET, (short) bytes.capacity());
        bytes.put(TYPE_OFFSET, (byte) type.ordinal());
        bytes.put(STATUS_OFFSET, (byte) MESSAGE_STATUS.GOOD.ordinal());
        bytes.putInt(bytes.capacity() - INT_SIZE, FOOTER_BYTES);
    }
}

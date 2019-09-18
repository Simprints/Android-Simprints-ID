package com.simprints.fingerprintscanner.v2

import com.simprints.fingerprintscanner.v2.domain.message.IncomingMessage
import com.simprints.fingerprintscanner.v2.domain.message.vero.VeroMessageProtocol
import com.simprints.fingerprintscanner.v2.domain.packet.Channel
import com.simprints.fingerprintscanner.v2.domain.packet.Packet
import com.simprints.fingerprintscanner.v2.domain.packet.PacketProtocol
import com.simprints.fingerprintscanner.v2.incoming.message.accumulators.PacketToMessageAccumulator
import com.simprints.fingerprintscanner.v2.incoming.message.parsers.MessageParser
import com.simprints.fingerprintscanner.v2.incoming.message.toMessageStream
import com.simprints.fingerprintscanner.v2.incoming.packet.ByteArrayToPacketAccumulator
import com.simprints.fingerprintscanner.v2.incoming.packet.PacketParser
import com.simprints.fingerprintscanner.v2.incoming.packet.PacketRouter
import com.simprints.fingerprintscanner.v2.incoming.packet.toPacketStream
import com.simprints.fingerprintscanner.v2.tools.*
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.observers.TestObserver
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import io.reactivex.subscribers.TestSubscriber
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.InputStream
import java.io.OutputStream
import java.io.PipedInputStream
import java.io.PipedOutputStream

class RxJavaTest {

    @Test
    fun unsignedToIntTest() {
        assertEquals(10, 10.toShort().unsignedToInt())
        assertEquals(0, 0.toShort().unsignedToInt())
        assertEquals(65530, 65530.toShort().unsignedToInt())
        assertEquals(65532, (-4).toShort().unsignedToInt())
        assertEquals(32767, 32767.toShort().unsignedToInt())
        assertEquals(32768, 32768.toShort().unsignedToInt())
        assertEquals(32769, 32769.toShort().unsignedToInt())
        assertEquals(65535, (-1).toShort().unsignedToInt())
    }

    @Test
    fun shortToByteArrayTest() {
        assertHexStringsEqual("57 00", 87.toShort().toByteArray().toHexString())
        assertHexStringsEqual("00 00", 0.toShort().toByteArray().toHexString())
        assertHexStringsEqual("FF FF", 65535.toShort().toByteArray().toHexString())
        assertHexStringsEqual("FF 00", 255.toShort().toByteArray().toHexString())
        assertHexStringsEqual("00 01", 256.toShort().toByteArray().toHexString())
        assertHexStringsEqual("17 36", 13847.toShort().toByteArray().toHexString())
    }

    @Test
    fun intToByteArrayTest() {
        assertHexStringsEqual("57 00 00 00", 87.toByteArray().toHexString())
        assertHexStringsEqual("00 00 00 00", 0.toByteArray().toHexString())
        assertHexStringsEqual("FF FF FF 7F", (Int.MAX_VALUE).toByteArray().toHexString())
        assertHexStringsEqual("00 00 00 80", (Int.MIN_VALUE).toByteArray().toHexString())
        assertHexStringsEqual("BB 5A 69 36", 912874171.toByteArray().toHexString())
    }

    @Test
    fun packetBuilderTest() {
        assertHexStringsEqual(
            "10 A0 03 00 0F 1F 2F ",

            PacketParser().parse(PacketProtocol.buildPacketBytes(Channel.Remote.VeroServer, Channel.Local.AndroidDevice, byteArrayOf(0x0F, 0x1F, 0x2F))).bytes.toHexString()
        )
    }

    @Test
    fun packetTest() {

        val outputStream = PipedOutputStream()
        val inputStream = PipedInputStream()
        inputStream.connect(outputStream)

        val testSubscriber = TestSubscriber<Packet>()

        inputStream
            .toPacketStream()
            .doOnNext {
                print("On packet next : ${it.bytes.toHexString()}")
            }
            .take(1800)
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.trampoline())
            .subscribe(testSubscriber)

        val bytes = "10 A0 01 00 F0   10 A0 03 00 F0 F1 F2   10 A0 02 00 F0 F1 ".repeat(600)
        outputStream.writeBytes(bytes)

        testSubscriber.awaitTerminalEvent()
        print("Final messages: ${testSubscriber.values().map { it.bytes.toHexString() }}")
        assertHexStringsEqual(bytes, testSubscriber.values().map { it.bytes.toHexString() }.reduce { a, b -> a + b })
    }

    @Test
    fun messageAccumulation_messageSpreadOutOverMultiplePackets_succeeds() {
        val outputStream = PipedOutputStream()
        val inputStream = PipedInputStream()
        inputStream.connect(outputStream)

        val testSubscriber = TestSubscriber<TestMessage>()

        inputStream
            .toPacketStream()
            .doOnNext { packet ->
                print("On packet next : ${packet.bytes.toHexString()}")
            }
            .toMessageStream(TestMessageAccumulator(TestMessageParser()))
            .doOnNext { message ->
                print("On message next : ${message.bytes.toHexString()}")
            }
            .take(600)
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.trampoline())
            .subscribe(testSubscriber)

        val bytes =
            "10 A0 04 00 CC DD 08 00    10 A0 05 00 F0 F1 F2 F3 F4   10 A0 03 00 F5 F6 F7".repeat(600)
        val messageBytes = "CC DD 08 00 F0 F1 F2 F3 F4 F5 F6 F7 ".repeat(600)
        outputStream.writeBytes(bytes)

        testSubscriber.awaitTerminalEvent()
        print("Final messages: ${testSubscriber.values().map { message -> message.bytes.toHexString() }}")
        assertHexStringsEqual(
            messageBytes,
            testSubscriber.values().map { message -> message.bytes.toHexString() }.reduce { a, b -> a + b })
    }

    @Test
    fun messageAccumulation_packetContainsMultipleMessages_succeeds() {
        val outputStream = PipedOutputStream()
        val inputStream = PipedInputStream()
        inputStream.connect(outputStream)

        val testSubscriber = TestSubscriber<TestMessage>()

        inputStream
            .toPacketStream()
            .doOnNext { packet ->
                print("On packet next : ${packet.bytes.toHexString()}")
            }
            .toMessageStream(TestMessageAccumulator(TestMessageParser()))
            .doOnNext { message ->
                print("On message next : ${message.bytes.toHexString()}")
            }
            .take(3000)
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.trampoline())
            .subscribe(testSubscriber)

        val bytes =
            "10 A0 0F 00 CC DD 02 00 F0 F2 CC DD 00 00 CC DD 01 00 F0  10 A0 0B 00 CC DD 00 00 CC DD 03 00 F0 F1 F2".repeat(600)
        val messageBytes = "CC DD 02 00 F0 F2  CC DD 00 00  CC DD 01 00 F0  CC DD 00 00  CC DD 03 00 F0 F1 F2".repeat(600)
        outputStream.writeBytes(bytes)

        testSubscriber.awaitTerminalEvent()
        print("Final messages: ${testSubscriber.values().map { message -> message.bytes.toHexString() }}")
        assertHexStringsEqual(
            messageBytes,
            testSubscriber.values().map { message -> message.bytes.toHexString() }.reduce { a, b -> a + b })
    }

    @Test
    fun messageAccumulation_multipleMessagesBrokenOverMultiplePackets_succeeds() {
        val outputStream = PipedOutputStream()
        val inputStream = PipedInputStream()
        inputStream.connect(outputStream)

        val testSubscriber = TestSubscriber<TestMessage>()

        inputStream
            .toPacketStream()
            .doOnNext { packet ->
                print("On packet next : ${packet.bytes.toHexString()}")
            }
            .toMessageStream(TestMessageAccumulator(TestMessageParser()))
            .doOnNext { message ->
                print("On message next : ${message.bytes.toHexString()}")
            }
            .take(1200)
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.trampoline())
            .subscribe(testSubscriber)

        val bytes =
            "10 A0 04 00 CC DD 01 00  10 A0 04 00 F0 CC DD 03  10 A0 04 00 00 F0 F1 F2".repeat(600)
        val messageBytes = "CC DD 01 00 F0  CC DD 03 00 F0 F1 F2".repeat(600)
        outputStream.writeBytes(bytes)

        testSubscriber.awaitTerminalEvent()
        print("Final messages: ${testSubscriber.values().map { message -> message.bytes.toHexString() }}")
        assertHexStringsEqual(
            messageBytes,
            testSubscriber.values().map { message -> message.bytes.toHexString() }.reduce { a, b -> a + b })
    }

    @Test
    fun publishPacketTest() {
        val outputStream = PipedOutputStream()
        val inputStream = PipedInputStream()
        inputStream.connect(outputStream)

        val publisher = PublishSubject.create<Packet>()
        inputStream
            .toPacketStream()
            .toObservable()
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.trampoline())
            .subscribe(publisher)

        val testObserver = TestObserver<Packet>()

        fun sendMessageAndQueueReply() {
            Completable.fromAction {
                Thread.sleep(1000)
                val bytes = "10 A0 01 00 F0   10 A0 03 00 F0 F1 F2   10 A0 02 00 F0 F1 ".repeat(600)
                outputStream.writeBytes(bytes)
            }.subscribeOn(Schedulers.io()).observeOn(Schedulers.trampoline()).subscribe()
        }

        Completable.fromAction {
            sendMessageAndQueueReply()
        }.andThen(publisher)
            .take(3)
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.trampoline())
            .subscribeWith(testObserver)

        testObserver.awaitTerminalEvent()
        print("Final messages: ${testObserver.values().map { it.bytes.toHexString() }}")
        assertHexStringsEqual(
            "10 A0 01 00 F0   10 A0 03 00 F0 F1 F2   10 A0 02 00 F0 F1 ",
            testObserver.values().map { it.bytes.toHexString() }.reduce { a, b -> a + b })
    }

    @Test
    fun publishFlowablePacketTest() {
        val outputStream = PipedOutputStream()
        val inputStream = PipedInputStream()
        inputStream.connect(outputStream)

        val publishedFlowable = inputStream
            .toPacketStream()
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.trampoline())
            .publish()

        val testSubscriber = TestSubscriber<Packet>()

        val bytes = "10 A0 02 00 F0 F1 "

        Completable.fromAction {
            outputStream.writeBytes(bytes.repeat(600))
        }.andThen(publishedFlowable)
            .take(3)
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.trampoline())
            .subscribeWith(testSubscriber)

        publishedFlowable.connect()

        testSubscriber.awaitTerminalEvent()
        print("Final messages: ${testSubscriber.values().map { it.bytes.toHexString() }}")
        assertHexStringsEqual(
            testSubscriber.values().map { it.bytes.toHexString() }.reduce { a, b -> a + b },
            bytes.repeat(3)
        )
    }

    @Test
    fun publishFlowablePacketMultipleSubscribersShouldBothGetPackets() {
        val outputStream = PipedOutputStream()
        val inputStream = PipedInputStream()
        inputStream.connect(outputStream)

        val publishedFlowable = inputStream
            .toPacketStream()
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.trampoline())
            .publish()

        val testSubscriber1 = TestSubscriber<Packet>()
        val testSubscriber2 = TestSubscriber<Packet>()

        val bytes = "10 A0 02 00 F0 F1 "

        publishedFlowable
            .take(7)
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.trampoline())
            .subscribeWith(testSubscriber1)

        publishedFlowable
            .take(5)
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.trampoline())
            .subscribeWith(testSubscriber2)

        publishedFlowable.connect()

        outputStream.writeBytes(bytes.repeat(600))

        testSubscriber1.awaitTerminalEvent()
        testSubscriber2.awaitTerminalEvent()
        print("Final messages 1: ${testSubscriber1.values().map { it.bytes.toHexString() }}")
        print("Final messages 2: ${testSubscriber2.values().map { it.bytes.toHexString() }}")
        assertHexStringsEqual(
            testSubscriber1.values().map { it.bytes.toHexString() }.reduce { a, b -> a + b },
            bytes.repeat(7)
        )
        assertHexStringsEqual(
            testSubscriber2.values().map { it.bytes.toHexString() }.reduce { a, b -> a + b },
            bytes.repeat(5)
        )
    }

    @Test
    fun publishFlowablePacketMultipleSubscribersFilteredShouldGetDifferentPackets() {
        val outputStream = PipedOutputStream()
        val inputStream = PipedInputStream()
        inputStream.connect(outputStream)

        val publishedFlowable = inputStream
            .toPacketStream()
            .subscribeOn(Schedulers.io())
            .publish()

        val testSubscriber1 = TestSubscriber<Packet>()
        val testSubscriber2 = TestSubscriber<Packet>()

        val bytes1 = "10 A0 03 00 F0 F1 F2 "
        val bytes2 = "20 A0 02 00 F0 F1 "
        val bytes3 = "30 A0 01 00 F0 "

        val source1Flowable = publishedFlowable
            .filter { it.source == 0x10.toByte() }
            .subscribeOn(Schedulers.io())
            .publish()

        val source2Flowable = publishedFlowable
            .filter { it.source == 0x20.toByte() }
            .subscribeOn(Schedulers.io())
            .publish()

        source1Flowable
            .take(7)
            .subscribeOn(Schedulers.io())
            .subscribeWith(testSubscriber1)

        source2Flowable
            .take(5)
            .subscribeOn(Schedulers.io())
            .subscribeWith(testSubscriber2)

        source1Flowable.connect()
        source2Flowable.connect()
        publishedFlowable.connect()

        outputStream.writeBytes((bytes1 + bytes2 + bytes3).repeat(600))

        testSubscriber1.awaitTerminalEvent()
        testSubscriber2.awaitTerminalEvent()
        print("Final messages 1: ${testSubscriber1.values().map { it.bytes.toHexString() }}")
        print("Final messages 2: ${testSubscriber2.values().map { it.bytes.toHexString() }}")
        assertHexStringsEqual(
            testSubscriber1.values().map { it.bytes.toHexString() }.reduce { a, b -> a + b },
            bytes1.repeat(7)
        )
        assertHexStringsEqual(
            testSubscriber2.values().map { it.bytes.toHexString() }.reduce { a, b -> a + b },
            bytes2.repeat(5)
        )
    }

    @Test
    fun packetRouterTest() {
        val outputStream = PipedOutputStream()
        val inputStream = PipedInputStream()
        inputStream.connect(outputStream)

        val router = PacketRouter(ByteArrayToPacketAccumulator(PacketParser()))

        router.connect(inputStream)

        val testSubscriber1 = TestSubscriber<Packet>()
        val testSubscriber2 = TestSubscriber<Packet>()
        val testSubscriber3 = TestSubscriber<Packet>()

        router.incomingPacketChannels[Channel.Remote.VeroServer]
            ?.take(7)?.subscribeOn(Schedulers.io())?.subscribeWith(testSubscriber1)
        router.incomingPacketChannels[Channel.Remote.VeroEvent]
            ?.take(5)?.subscribeOn(Schedulers.io())?.subscribeWith(testSubscriber2)
        router.incomingPacketChannels[Channel.Remote.Un20Server]
            ?.take(11)?.subscribeOn(Schedulers.io())?.subscribeWith(testSubscriber3)

        val pb = PacketParser()
        val bytes1 = pb.parse(PacketProtocol.buildPacketBytes(Channel.Remote.VeroServer, Channel.Local.AndroidDevice, byteArrayOf(0x0F, 0x1F, 0x2F))).bytes.toHexString()
        val bytes2 = pb.parse(PacketProtocol.buildPacketBytes(Channel.Remote.VeroEvent, Channel.Local.AndroidDevice, byteArrayOf(0x0F, 0x1F))).bytes.toHexString()
        val bytes3 = pb.parse(PacketProtocol.buildPacketBytes(Channel.Remote.Un20Server, Channel.Local.AndroidDevice, byteArrayOf(0x0F, 0x1F, 0x2F, 0x3F))).bytes.toHexString()

        outputStream.writeBytes((bytes1 + bytes2 + bytes3).repeat(600))

        testSubscriber1.awaitTerminalEvent()
        testSubscriber2.awaitTerminalEvent()
        testSubscriber3.awaitTerminalEvent()

        router.disconnect()

        print("Final messages 1: ${testSubscriber1.values().map { it.bytes.toHexString() }}")
        print("Final messages 2: ${testSubscriber2.values().map { it.bytes.toHexString() }}")
        print("Final messages 3: ${testSubscriber3.values().map { it.bytes.toHexString() }}")
        assertHexStringsEqual(
            testSubscriber1.values().map { it.bytes.toHexString() }.reduce { a, b -> a + b },
            bytes1.repeat(7)
        )
        assertHexStringsEqual(
            testSubscriber2.values().map { it.bytes.toHexString() }.reduce { a, b -> a + b },
            bytes2.repeat(5)
        )
        assertHexStringsEqual(
            testSubscriber3.values().map { it.bytes.toHexString() }.reduce { a, b -> a + b },
            bytes3.repeat(11)
        )
    }

    private fun assertHexStringsEqual(expected: String, actual: String) {
        assertEquals(
            stripWhiteSpaceAndMakeLowercase(expected),
            stripWhiteSpaceAndMakeLowercase(actual)
        )
    }

    private fun OutputStream.writeBytes(byteString: String) = writeBytes(hexStringToByteArray(byteString))
    private fun OutputStream.writeBytes(bytes: ByteArray) = this.write(bytes)

    private fun InputStream.toPacketStream(): Flowable<Packet> = this
        .toFlowable()
        .toPacketStream(ByteArrayToPacketAccumulator(PacketParser()))

    private fun print(any: Any) = println(any)

    class TestMessage(val bytes: ByteArray) : IncomingMessage
    class TestMessageParser : MessageParser<TestMessage> {

        override fun parse(messageBytes: ByteArray): TestMessage = TestMessage(messageBytes)
    }

    class TestMessageAccumulator(testMessageParser: TestMessageParser) :
        PacketToMessageAccumulator<TestMessage>(VeroMessageProtocol, testMessageParser)
}

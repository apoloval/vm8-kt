package vm8.cpu.z80

import vm8.byteorder.ByteOrder

typealias Addr = UShort

/**
 * A bus where the Z80 processor is connected to.
 */
interface Bus {
    /**
     * Read a byte at given address from the memory using the system bus.
     */
    suspend fun memReadByte(addr: Addr): UByte

    /**
     * Write a byte at given address to the memory using the system bus.
     */
    suspend fun memWriteByte(addr: Addr, v: UByte)

    /**
     * Read a byte at given port from the IO using the system bus.
     */
    suspend fun ioReadByte(port: UByte): UByte

    /**
     * Write a byte at given port to the IO using the system bus.
     */
    suspend fun ioWriteByte(port: UByte, v: UByte)

    /**
     * Acknowledge an interrupt request.
     *
     * The devices that was interrupting has to answer to the acknowledgment transfer with a byte
     * according to the following modes:
     *
     *  - In mode 0: the opcode of the next instruction to be executed. If a multi-byte instruction
     *    is given, further bytes will be obtained with a [memReadByte].
     *  - In mode 1: the byte is ignored. It could be anything as it will be discarded.
     *  - In mode 2: the LSB of the vector that, combined with the MSB from I register, indicates the
     *    location of the ISR.
     */
    suspend fun intAck(): UByte

    /**
     * Read a word at given address from the memory using the system bus.
     */
    suspend fun memReadWord(addr: Addr): UShort = ByteOrder.LITTLE_ENDIAN.decode(
        memReadByte(addr),
        memReadByte(addr.inc()),
    )

    /**
     * Write a word at given address to the memory using the system bus.
     */
    suspend fun memWriteWord(addr: Addr, v: UShort) {
        val (v0, v1) = ByteOrder.LITTLE_ENDIAN.encode(v)
        memWriteByte(addr, v0)
        memWriteByte(addr.inc(), v1)
    }
}

/**
 * A bus that is disconnected from the system.
 */
object DisconnectedBus : Bus {
    override suspend fun memReadByte(addr: Addr): UByte = 0xFFu
    override suspend fun memWriteByte(addr: Addr, v: UByte) {}
    override suspend fun ioReadByte(port: UByte): UByte = 0xFFu
    override suspend fun ioWriteByte(port: UByte, v: UByte) {}
    override suspend fun intAck(): UByte = 0u
}

/**
 * A bus that emulates a minimal Z80 system, mainly for testing purposes.
 */
class MinimalSystem : Bus {
    val memory = ByteArray(64*1024)
    val io = ByteArray(256)
    var nextIntAck: UByte = 0u

    override suspend fun memReadByte(addr: Addr): UByte = memory[addr.toInt()].toUByte()

    override suspend fun memWriteByte(addr: Addr, v: UByte) { memory[addr.toInt()] = v.toByte() }

    override suspend fun ioReadByte(port: UByte): UByte = io[port.toInt()].toUByte()

    override suspend fun ioWriteByte(port: UByte, v: UByte) { io[port.toInt()] = v.toByte() }

    override suspend fun intAck(): UByte = nextIntAck
}

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
}

/**
 * A bus that emulates a minimal Z80 system, mainly for testing purposes.
 */
class MinimalSystem : Bus {
    val memory = ByteArray(64*1024)

    override suspend fun memReadByte(addr: Addr): UByte = memory[addr.toInt()].toUByte()

    override suspend fun memWriteByte(addr: Addr, v: UByte) { memory[addr.toInt()] = v.toByte() }
}

package vm8.cpu.z80

import vm8.byteorder.ByteOrder

typealias Addr = UShort

/**
 * A bus where the Z80 processor is connected to.
 */
interface Bus {
    // Read a datum at given address from the bus
    suspend fun read(addr: Addr): UByte

    // write a datum at given address to the bus
    suspend fun write(addr: Addr, v: UByte)

    suspend fun readWord(addr: Addr): UShort = ByteOrder.LITTLE_ENDIAN.decode(
        read(addr), 
        read(addr.inc()),
    )

    suspend fun writeWord(addr: Addr, v: UShort) {
        val (v0, v1) = ByteOrder.LITTLE_ENDIAN.encode(v)
        write(addr, v0)
        write(addr.inc(), v1)
    }
}

/**
 * A bus that is disconnected from the system.
 */
object DisconnectedBus : Bus {
    override suspend fun read(addr: Addr): UByte = 0xFFu
    override suspend fun write(addr: Addr, v: UByte) {}    
}

/**
 * A bus that emulates a minimal Z80 system, mainly for testing purposes.
 */
class MinimalSystem : Bus {
    val memory = ByteArray(64*1024)

    override suspend fun read(addr: Addr): UByte = memory[addr.toInt()].toUByte()

    override suspend fun write(addr: Addr, v: UByte) { memory[addr.toInt()] = v.toByte() }
}

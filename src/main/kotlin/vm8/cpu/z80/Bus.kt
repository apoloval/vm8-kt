package vm8.cpu.z80

import vm8.byteorder.*
import vm8.data.*

typealias Addr = Word

/**
 * A bus where the Z80 processor is connected to.
 */
interface Bus {
    // Read a datum at given address from the bus
    suspend fun read(addr: Addr): Octet

    // write a datum at given address to the bus
    suspend fun write(addr: Addr, v: Octet)

    suspend fun readWord(addr: Addr): Word = ByteOrder.LITTLE_ENDIAN.decode(
        read(addr), 
        read(addr.inc()),
    )
}

/**
 * A bus that is disconnected from the system.
 */
object DisconnectedBus : Bus {
    override suspend fun read(addr: Addr): Octet = Octet(0xFF)
    override suspend fun write(addr: Addr, v: Octet) {}    
}

/**
 * A bus that emulates a minimal Z80 system, mainly for testing purposes.
 */
class MinimalSystem : Bus {
    val memory = ByteArray(64*1024)
    override suspend fun read(addr: Addr): Octet = Octet(memory[addr.toInt()].toInt())
    override suspend fun write(addr: Addr, v: Octet) { memory[addr.toInt()] = v.toByte() }
}

package vm8.cpu.z80

import vm8.byteorder.*
import vm8.data.*

typealias Addr = Word

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

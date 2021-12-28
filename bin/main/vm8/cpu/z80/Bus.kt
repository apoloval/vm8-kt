package vm8.cpu.z80

import vm8.byteorder.*

typealias Addr = UShort

interface Bus : vm8.Bus<Addr, Byte> {
    suspend fun readWord(addr: Addr): Short = ByteOrder.LITTLE_ENDIAN.decode(
        read(addr), 
        read(addr.inc()),
    )
}

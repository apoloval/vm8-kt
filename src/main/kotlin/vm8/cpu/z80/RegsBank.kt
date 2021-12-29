package vm8.cpu.z80

import vm8.byteorder.*
import vm8.data.*

class RegsBank {
    var af: Word = Word(0x0000)
    var bc: Word = Word(0x0000)
    var pc: Word = Word(0x0000)

    var b: Octet 
        inline get() = bc.high()
        inline set(v) { bc = bc.setHigh(v) }

    var c: Octet 
        inline get() = bc.low()
        inline set(v) { bc = bc.setLow(v) }
}

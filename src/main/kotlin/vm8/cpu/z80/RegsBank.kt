package vm8.cpu.z80

import vm8.byteorder.*
import vm8.data.*

class RegsBank {
    var af: Word = Word(0x0000)
    var bc: Word = Word(0x0000)
    var de: Word = Word(0x0000)
    var hl: Word = Word(0x0000)
    var pc: Word = Word(0x0000)
    var sp: Word = Word(0x0000)

    var a: Octet 
        inline get() = af.high()
        inline set(v) { af = af.setHigh(v) }

    var f: Octet 
        inline get() = af.low()
        inline set(v) { af = af.setLow(v) }

    var b: Octet 
        inline get() = bc.high()
        inline set(v) { bc = bc.setHigh(v) }

    var c: Octet 
        inline get() = bc.low()
        inline set(v) { bc = bc.setLow(v) }

    var d: Octet 
        inline get() = de.high()
        inline set(v) { de = de.setHigh(v) }

    var e: Octet 
        inline get() = de.low()
        inline set(v) { de = de.setLow(v) }

    var h: Octet 
        inline get() = hl.high()
        inline set(v) { hl = hl.setHigh(v) }

    var l: Octet 
        inline get() = hl.low()
        inline set(v) { hl = hl.setLow(v) }

    inline fun updateFlags(fn: (Octet) -> Octet) {
        f = fn(f)
    }
}

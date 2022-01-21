package vm8.cpu.z80

import vm8.data.high
import vm8.data.low
import vm8.data.setHigh
import vm8.data.setLow

class RegsBank {
    var af: UShort = 0x0000u
    var bc: UShort = 0x0000u
    var de: UShort = 0x0000u
    var hl: UShort = 0x0000u
    var pc: UShort = 0x0000u
    var sp: UShort = 0x0000u

    var `af'`: UShort = 0x0000u
    var `bc'`: UShort = 0x0000u
    var `de'`: UShort = 0x0000u
    var `hl'`: UShort = 0x0000u

    var i: UByte = 0x00u

    var iff1: Boolean = false
    var iff2: Boolean = false

    var nmiff: Boolean = false
    var intff: Boolean = false
    var eiff: Boolean = false

    var a: UByte
        inline get() = af.high()
        inline set(v) { af = af.setHigh(v) }

    var f: UByte
        inline get() = af.low()
        inline set(v) { af = af.setLow(v) }

    var b: UByte
        inline get() = bc.high()
        inline set(v) { bc = bc.setHigh(v) }

    var c: UByte
        inline get() = bc.low()
        inline set(v) { bc = bc.setLow(v) }

    var d: UByte
        inline get() = de.high()
        inline set(v) { de = de.setHigh(v) }

    var e: UByte
        inline get() = de.low()
        inline set(v) { de = de.setLow(v) }

    var h: UByte
        inline get() = hl.high()
        inline set(v) { hl = hl.setHigh(v) }

    var l: UByte
        inline get() = hl.low()
        inline set(v) { hl = hl.setLow(v) }
}


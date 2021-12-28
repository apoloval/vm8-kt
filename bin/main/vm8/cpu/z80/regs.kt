package vm8.cpu.z80

import vm8.byteorder.*

class RegsBank {
    var af: Short = 0x0000
    var bc: Short = 0x0000
    var pc: Short = 0x0000

    val b: Byte get() = bc.high()
    val c: Byte get() = bc.low()

    fun load8(op: Reg8): Byte = when(op) {
        Reg8.B -> bc.high()
        Reg8.C -> bc.low()
    }

    fun store8(op: Reg8, v: Byte) = when(op) {
        Reg8.B -> bc = bc.setHigh(v)
        Reg8.C -> bc = bc.setLow(v)
    }

    fun load16(op: Reg16): Short = when(op) {
        Reg16.BC -> bc
        Reg16.PC -> pc
    }
    
    fun store16(op: Reg16, v: Short) = when(op) {
        Reg16.BC -> bc = v
        Reg16.PC -> pc = v
    }
}
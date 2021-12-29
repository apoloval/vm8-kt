package vm8.cpu.z80

import vm8.byteorder.*
import vm8.data.*

class RegsBank {
    var af: Word = Word(0x0000)
    var bc: Word = Word(0x0000)
    var pc: Word = Word(0x0000)

    val b: Octet get() = bc.high()
    val c: Octet get() = bc.low()

    fun load8(op: Reg8): Octet = when(op) {
        Reg8.B -> bc.high()
        Reg8.C -> bc.low()
    }

    fun store8(op: Reg8, v: Octet) = when(op) {
        Reg8.B -> bc = bc.setHigh(v)
        Reg8.C -> bc = bc.setLow(v)
    }

    fun load16(op: Reg16): Word = when(op) {
        Reg16.BC -> bc
        Reg16.PC -> pc
    }
    
    fun store16(op: Reg16, v: Word) = when(op) {
        Reg16.BC -> bc = v
        Reg16.PC -> pc = v
    }
}
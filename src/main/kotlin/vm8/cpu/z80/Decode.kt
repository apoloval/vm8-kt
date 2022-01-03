package vm8.cpu.z80

object OpCodes {
    const val `NOP`         : Int = 0x00
    const val `LD BC, NN`   : Int = 0x01
    const val `LD (BC), A`  : Int = 0x02
    const val `INC BC`      : Int = 0x03
    const val `INC B`       : Int = 0x04
    const val `DEC B`       : Int = 0x05
    const val `LD B, N`     : Int = 0x06
    const val `RLCA`        : Int = 0x07
    const val `EX AF, AF'`  : Int = 0x08
    const val `ADD HL, BC`  : Int = 0x09
    const val `LD A, (BC)`  : Int = 0x0A
    const val `DEC BC`      : Int = 0x0B
    const val `INC C`       : Int = 0x0C
    const val `DEC C`       : Int = 0x0D
    const val `LD C, N`     : Int = 0x0E
  
    const val `LD DE, NN`   : Int = 0x10

    const val `INC D`       : Int = 0x14
    const val `DEC D`       : Int = 0x15

    const val `INC E`       : Int = 0x1C
    const val `DEC E`       : Int = 0x1D

    const val `LD HL, NN`   : Int = 0x20

    const val `INC H`       : Int = 0x24
    const val `DEC H`       : Int = 0x25

    const val `INC L`       : Int = 0x2C
    const val `DEC L`       : Int = 0x2D

    const val `LD SP, NN`   : Int = 0x30

    const val `INC (HL)`    : Int = 0x34
    const val `DEC (HL)`    : Int = 0x35

    const val `INC A`       : Int = 0x3C
    const val `DEC A`       : Int = 0x3D

    const val `JP NN`       : Int = 0xC3
}

private val OPCODES_MAIN: Array<Inst> = Array(256) {
    when(it) {
        OpCodes.`NOP` -> Nop
        OpCodes.`LD BC, NN` -> Ld16(Reg16.BC, Imm16, cycles = 10, size = 3)
        OpCodes.`LD (BC), A` -> Ld8(Ind8(Reg16.BC), Reg8.A, cycles = 7, size = 1)
        OpCodes.`INC BC` -> Inc16(Reg16.BC, cycles = 6, size = 1)
        OpCodes.`INC B` -> Inc8(Reg8.B, cycles = 4, size = 1)
        OpCodes.`DEC B` -> Dec8(Reg8.B, cycles = 4, size = 1)
        OpCodes.`LD B, N` -> Ld8(Reg8.B, Imm8, cycles = 7, size = 2)
        OpCodes.`RLCA` -> Rlca(cycles = 4, size = 1)
        OpCodes.`EX AF, AF'` -> Ex(Reg16.AF, Reg16.`AF'`, cycles = 4, size = 1)
        OpCodes.`ADD HL, BC` -> Add16(Reg16.HL, Reg16.BC, cycles = 11, size = 1)
        OpCodes.`LD A, (BC)` -> Ld8(Reg8.A, Ind8(Reg16.BC), cycles = 7, size = 1)
        OpCodes.`DEC BC` -> Dec16(Reg16.BC, cycles = 6, size = 1)
        OpCodes.`INC C` -> Inc8(Reg8.C, cycles = 4, size = 1)
        OpCodes.`DEC C` -> Dec8(Reg8.C, cycles = 4, size = 1)
        OpCodes.`LD C, N` -> Ld8(Reg8.C, Imm8, cycles = 7, size = 2)

        OpCodes.`JP NN` -> Jp(Imm16)
        else -> Illegal
    }
}

suspend fun Processor.decode(): Inst {
    val opcode: Int = bus.read(regs.pc).toInt()
    return OPCODES_MAIN[opcode]
}

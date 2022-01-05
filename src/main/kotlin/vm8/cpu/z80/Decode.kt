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
    const val `RRCA`        : Int = 0x0F

    const val `DJNZ N`      : Int = 0x10
    const val `LD DE, NN`   : Int = 0x11
    const val `LD (DE), A`  : Int = 0x12
    const val `INC DE`      : Int = 0x13
    const val `INC D`       : Int = 0x14
    const val `DEC D`       : Int = 0x15
    const val `LD D, N`     : Int = 0x16
    const val `RLA`         : Int = 0x17
    const val `JR N`        : Int = 0x18
    const val `ADD HL, DE`  : Int = 0x19

    const val `INC E`       : Int = 0x1C
    const val `DEC E`       : Int = 0x1D

    const val `INC H`       : Int = 0x24
    const val `DEC H`       : Int = 0x25

    const val `INC L`       : Int = 0x2C
    const val `DEC L`       : Int = 0x2D

    const val `INC A`       : Int = 0x3C
    const val `DEC A`       : Int = 0x3D

    const val `JP NN`       : Int = 0xC3
}

private val OPCODES_MAIN: Array<Inst> = Array(256) {
    when(it) {
        // From 0x00 to 0x0F
        OpCodes.`NOP` -> Nop
        OpCodes.`LD BC, NN` -> Ld16(Reg16.BC, Imm16, cycles = 10, size = 3u)
        OpCodes.`LD (BC), A` -> Ld8(Ind8(Reg16.BC), Reg8.A, cycles = 7, size = 1u)
        OpCodes.`INC BC` -> Inc16(Reg16.BC, cycles = 6, size = 1u)
        OpCodes.`INC B` -> Inc8(Reg8.B, cycles = 4, size = 1u)
        OpCodes.`DEC B` -> Dec8(Reg8.B, cycles = 4, size = 1u)
        OpCodes.`LD B, N` -> Ld8(Reg8.B, Imm8, cycles = 7, size = 2u)
        OpCodes.`RLCA` -> Rlca(cycles = 4, size = 1u)
        OpCodes.`EX AF, AF'` -> Ex(Reg16.AF, Reg16.`AF'`, cycles = 4, size = 1u)
        OpCodes.`ADD HL, BC` -> Add16(Reg16.HL, Reg16.BC, cycles = 11, size = 1u)
        OpCodes.`LD A, (BC)` -> Ld8(Reg8.A, Ind8(Reg16.BC), cycles = 7, size = 1u)
        OpCodes.`DEC BC` -> Dec16(Reg16.BC, cycles = 6, size = 1u)
        OpCodes.`INC C` -> Inc8(Reg8.C, cycles = 4, size = 1u)
        OpCodes.`DEC C` -> Dec8(Reg8.C, cycles = 4, size = 1u)
        OpCodes.`LD C, N` -> Ld8(Reg8.C, Imm8, cycles = 7, size = 2u)
        OpCodes.`RRCA` -> Rrca(cycles = 4, size = 1u)
        OpCodes.`LD (DE), A` -> Ld8(Ind8(Reg16.DE), Reg8.A, cycles = 7, size = 1u)
        OpCodes.`RLA` -> Rla(cycles = 4, size = 1u)

        // From 0x10 to 0x1F
        OpCodes.`DJNZ N` -> Djnz(Reg8.B, Imm8, jcycles = 13, njcycles = 8, size = 2u)
        OpCodes.`LD DE, NN` -> Ld16(Reg16.DE, Imm16, cycles = 10, size = 3u)
        OpCodes.`INC DE` -> Inc16(Reg16.DE, cycles = 6, size = 1u)
        OpCodes.`INC D` -> Inc8(Reg8.D, cycles = 4, size = 1u)
        OpCodes.`DEC D` -> Dec8(Reg8.D, cycles = 4, size = 1u)
        OpCodes.`LD D, N` -> Ld8(Reg8.D, Imm8, cycles = 7, size = 2u)
        OpCodes.`JR N` -> Jr({ true }, Imm8, jcycles = 12, njcycles = 12, size = 2u)
        OpCodes.`ADD HL, DE` -> Add16(Reg16.HL, Reg16.DE, cycles = 11, size = 1u)

        OpCodes.`JP NN` -> Jp(Imm16)
        else -> Illegal
    }
}

suspend fun Processor.decode(): Inst {
    val opcode: Int = bus.read(regs.pc).toInt()
    return OPCODES_MAIN[opcode]
}

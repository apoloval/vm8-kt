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
    const val `LD A, (DE)`  : Int = 0x1A
    const val `DEC DE`      : Int = 0x1B
    const val `INC E`       : Int = 0x1C
    const val `DEC E`       : Int = 0x1D
    const val `LD E, N`     : Int = 0x1E
    const val `RRA`         : Int = 0x1F

    const val `JR NZ, N`    : Int = 0x20
    const val `LD HL, NN`   : Int = 0x21
    const val `LD (NN), HL` : Int = 0x22
    const val `INC HL`      : Int = 0x23
    const val `INC H`       : Int = 0x24
    const val `DEC H`       : Int = 0x25
    const val `LD H, N`     : Int = 0x26
    const val `DAA`         : Int = 0x27
    const val `JR Z, N`     : Int = 0x28
    const val `ADD HL, HL`  : Int = 0x29
    const val `LD HL, (NN)` : Int = 0x2A
    const val `DEC HL`      : Int = 0x2B
    const val `INC L`       : Int = 0x2C
    const val `DEC L`       : Int = 0x2D
    const val `LD L, N`     : Int = 0x2E
    const val `CPL`         : Int = 0x2F

    const val `JR NC, N`    : Int = 0x30
    const val `LD SP, NN`   : Int = 0x31
    const val `LD (NN), A`  : Int = 0x32
    const val `INC SP`      : Int = 0x33
    const val `INC (HL)`    : Int = 0x34
    const val `DEC (HL)`    : Int = 0x35
    const val `LD (HL), N`  : Int = 0x36
    const val `SCF`         : Int = 0x37
    const val `JR C, N`     : Int = 0x38
    const val `ADD HL, SP`  : Int = 0x39
    const val `LD A, (NN)`  : Int = 0x3A
    const val `DEC SP`      : Int = 0x3B
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

        // From 0x10 to 0x1F
        OpCodes.`DJNZ N` -> Djnz(Reg8.B, Imm8, jcycles = 13, njcycles = 8, size = 2u)
        OpCodes.`LD DE, NN` -> Ld16(Reg16.DE, Imm16, cycles = 10, size = 3u)
        OpCodes.`LD (DE), A` -> Ld8(Ind8(Reg16.DE), Reg8.A, cycles = 7, size = 1u)
        OpCodes.`INC DE` -> Inc16(Reg16.DE, cycles = 6, size = 1u)
        OpCodes.`INC D` -> Inc8(Reg8.D, cycles = 4, size = 1u)
        OpCodes.`DEC D` -> Dec8(Reg8.D, cycles = 4, size = 1u)
        OpCodes.`LD D, N` -> Ld8(Reg8.D, Imm8, cycles = 7, size = 2u)
        OpCodes.`RLA` -> Rla(cycles = 4, size = 1u)
        OpCodes.`JR N` -> Jr(JumpCond.ALWAYS, Imm8, jcycles = 12, njcycles = 12, size = 2u)
        OpCodes.`ADD HL, DE` -> Add16(Reg16.HL, Reg16.DE, cycles = 11, size = 1u)
        OpCodes.`LD A, (DE)` -> Ld8(Reg8.A, Ind8(Reg16.DE), cycles = 7, size = 1u)
        OpCodes.`DEC DE` -> Dec16(Reg16.DE, cycles = 6, size = 1u)
        OpCodes.`INC E` -> Inc8(Reg8.E, cycles = 4, size = 1u)
        OpCodes.`DEC E` -> Dec8(Reg8.E, cycles = 4, size = 1u)
        OpCodes.`LD E, N` -> Ld8(Reg8.E, Imm8, cycles = 7, size = 2u)
        OpCodes.`RRA` -> Rra(cycles = 4, size = 1u)

        // From 0x20 to 0x2F
        OpCodes.`JR NZ, N` -> Jr(JumpCond.NZ, Imm8, jcycles = 12, njcycles = 7, size = 2u)
        OpCodes.`LD HL, NN` -> Ld16(Reg16.HL, Imm16, cycles = 10, size = 3u)
        OpCodes.`LD (NN), HL` -> Ld16(Ind16(Imm16), Reg16.HL, cycles = 16, size = 3u)
        OpCodes.`INC HL` -> Inc16(Reg16.HL, cycles = 6, size = 1u)
        OpCodes.`INC H` -> Inc8(Reg8.H, cycles = 4, size = 1u)
        OpCodes.`DEC H` -> Dec8(Reg8.H, cycles = 4, size = 1u)
        OpCodes.`LD H, N` -> Ld8(Reg8.H, Imm8, cycles = 7, size = 2u)
        OpCodes.`DAA` -> Daa(cycles = 4, size = 1u)
        OpCodes.`JR Z, N` -> Jr(JumpCond.Z, Imm8, jcycles = 12, njcycles = 7, size = 2u)
        OpCodes.`ADD HL, HL` -> Add16(Reg16.HL, Reg16.HL, cycles = 11, size = 1u)
        OpCodes.`LD HL, (NN)` -> Ld16(Reg16.HL, Ind16(Imm16), cycles = 16, size = 3u)
        OpCodes.`DEC HL` -> Dec16(Reg16.HL, cycles = 6, size = 1u)
        OpCodes.`INC L` -> Inc8(Reg8.L, cycles = 4, size = 1u)
        OpCodes.`DEC L` -> Dec8(Reg8.L, cycles = 4, size = 1u)
        OpCodes.`LD L, N` -> Ld8(Reg8.L, Imm8, cycles = 7, size = 2u)
        OpCodes.`CPL` -> Cpl(cycles = 4, size = 1u)

        // From 0x30 to 0x3F
        OpCodes.`JR NC, N` -> Jr(JumpCond.NC, Imm8, jcycles = 12, njcycles = 7, size = 2u)
        OpCodes.`LD SP, NN` -> Ld16(Reg16.SP, Imm16, cycles = 10, size = 3u)
        OpCodes.`LD (NN), A` -> Ld8(Ind8(Imm16), Reg8.A, cycles = 13, size = 3u)
        OpCodes.`INC SP` -> Inc16(Reg16.SP, cycles = 6, size = 1u)
        OpCodes.`INC (HL)` -> Inc8(Ind8(Reg16.HL), cycles = 11, size = 1u)
        OpCodes.`DEC (HL)` -> Dec8(Ind8(Reg16.HL), cycles = 11, size = 1u)
        OpCodes.`LD (HL), N` -> Ld8(Ind8(Reg16.HL), Imm8, cycles = 10, size = 2u)
        OpCodes.`SCF` -> Scf(cycles = 4, size = 1u)
        OpCodes.`JR C, N` -> Jr(JumpCond.C, Imm8, jcycles = 12, njcycles = 7, size = 2u)
        OpCodes.`ADD HL, SP` -> Add16(Reg16.HL, Reg16.SP, cycles = 11, size = 1u)
        OpCodes.`LD A, (NN)` -> Ld8(Reg8.A, Ind8(Imm16), cycles = 13, size = 3u)
        OpCodes.`DEC SP` -> Dec16(Reg16.SP, cycles = 6, size = 1u)
        OpCodes.`INC A` -> Inc8(Reg8.A, cycles = 4, size = 1u)
        OpCodes.`DEC A` -> Dec8(Reg8.A, cycles = 4, size = 1u)

        OpCodes.`JP NN` -> Jp(Imm16)
        else -> Illegal
    }
}

suspend fun Processor.decode(): Inst {
    val opcode: Int = bus.read(regs.pc).toInt()
    return OPCODES_MAIN[opcode]
}

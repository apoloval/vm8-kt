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
    const val `LD A, N`     : Int = 0x3E
    const val `CCF`         : Int = 0x3F

    const val `LD B, B`     : Int = 0x40
    const val `LD B, C`     : Int = 0x41
    const val `LD B, D`     : Int = 0x42
    const val `LD B, E`     : Int = 0x43
    const val `LD B, H`     : Int = 0x44
    const val `LD B, L`     : Int = 0x45
    const val `LD B, (HL)`  : Int = 0x46
    const val `LD B, A`     : Int = 0x47
    const val `LD C, B`     : Int = 0x48
    const val `LD C, C`     : Int = 0x49
    const val `LD C, D`     : Int = 0x4A
    const val `LD C, E`     : Int = 0x4B
    const val `LD C, H`     : Int = 0x4C
    const val `LD C, L`     : Int = 0x4D
    const val `LD C, (HL)`  : Int = 0x4E
    const val `LD C, A`     : Int = 0x4F

    const val `LD D, B`     : Int = 0x50
    const val `LD D, C`     : Int = 0x51
    const val `LD D, D`     : Int = 0x52
    const val `LD D, E`     : Int = 0x53
    const val `LD D, H`     : Int = 0x54
    const val `LD D, L`     : Int = 0x55
    const val `LD D, (HL)`  : Int = 0x56
    const val `LD D, A`     : Int = 0x57
    const val `LD E, B`     : Int = 0x58
    const val `LD E, C`     : Int = 0x59
    const val `LD E, D`     : Int = 0x5A
    const val `LD E, E`     : Int = 0x5B
    const val `LD E, H`     : Int = 0x5C
    const val `LD E, L`     : Int = 0x5D
    const val `LD E, (HL)`  : Int = 0x5E
    const val `LD E, A`     : Int = 0x5F

    const val `LD H, B`     : Int = 0x60
    const val `LD H, C`     : Int = 0x61
    const val `LD H, D`     : Int = 0x62
    const val `LD H, E`     : Int = 0x63
    const val `LD H, H`     : Int = 0x64
    const val `LD H, L`     : Int = 0x65
    const val `LD H, (HL)`  : Int = 0x66
    const val `LD H, A`     : Int = 0x67
    const val `LD L, B`     : Int = 0x68
    const val `LD L, C`     : Int = 0x69
    const val `LD L, D`     : Int = 0x6A
    const val `LD L, E`     : Int = 0x6B
    const val `LD L, H`     : Int = 0x6C
    const val `LD L, L`     : Int = 0x6D
    const val `LD L, (HL)`  : Int = 0x6E
    const val `LD L, A`     : Int = 0x6F

    const val `LD (HL), B`  : Int = 0x70
    const val `LD (HL), C`  : Int = 0x71
    const val `LD (HL), D`  : Int = 0x72
    const val `LD (HL), E`  : Int = 0x73
    const val `LD (HL), H`  : Int = 0x74
    const val `LD (HL), L`  : Int = 0x75
    const val `HALT`        : Int = 0x76
    const val `LD (HL), A`  : Int = 0x77
    const val `LD A, B`     : Int = 0x78
    const val `LD A, C`     : Int = 0x79
    const val `LD A, D`     : Int = 0x7A
    const val `LD A, E`     : Int = 0x7B
    const val `LD A, H`     : Int = 0x7C
    const val `LD A, L`     : Int = 0x7D
    const val `LD A, (HL)`  : Int = 0x7E
    const val `LD A, A`     : Int = 0x7F

    const val `ADD A, B`    : Int = 0x80
    const val `ADD A, C`    : Int = 0x81
    const val `ADD A, D`    : Int = 0x82
    const val `ADD A, E`    : Int = 0x83
    const val `ADD A, H`    : Int = 0x84
    const val `ADD A, L`    : Int = 0x85
    const val `ADD A, (HL)` : Int = 0x86
    const val `ADD A, A`    : Int = 0x87
    const val `ADC A, B`    : Int = 0x88
    const val `ADC A, C`    : Int = 0x89
    const val `ADC A, D`    : Int = 0x8A
    const val `ADC A, E`    : Int = 0x8B
    const val `ADC A, H`    : Int = 0x8C
    const val `ADC A, L`    : Int = 0x8D
    const val `ADC A, (HL)` : Int = 0x8E
    const val `ADC A, A`    : Int = 0x8F

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
        OpCodes.`LD A, N` -> Ld8(Reg8.A, Imm8, cycles = 7, size = 2u)
        OpCodes.`CCF` -> Ccf(cycles = 4, size = 1u)

        // From 0x40 to 0x4F
        OpCodes.`LD B, B` -> Ld8(Reg8.B, Reg8.B, cycles = 4, size = 1u)
        OpCodes.`LD B, C` -> Ld8(Reg8.B, Reg8.C, cycles = 4, size = 1u)
        OpCodes.`LD B, D` -> Ld8(Reg8.B, Reg8.D, cycles = 4, size = 1u)
        OpCodes.`LD B, E` -> Ld8(Reg8.B, Reg8.E, cycles = 4, size = 1u)
        OpCodes.`LD B, H` -> Ld8(Reg8.B, Reg8.H, cycles = 4, size = 1u)
        OpCodes.`LD B, L` -> Ld8(Reg8.B, Reg8.L, cycles = 4, size = 1u)
        OpCodes.`LD B, (HL)` -> Ld8(Reg8.B, Ind8(Reg16.HL), cycles = 7, size = 1u)
        OpCodes.`LD B, A` -> Ld8(Reg8.B, Reg8.A, cycles = 4, size = 1u)
        OpCodes.`LD C, B` -> Ld8(Reg8.C, Reg8.B, cycles = 4, size = 1u)
        OpCodes.`LD C, C` -> Ld8(Reg8.C, Reg8.C, cycles = 4, size = 1u)
        OpCodes.`LD C, D` -> Ld8(Reg8.C, Reg8.D, cycles = 4, size = 1u)
        OpCodes.`LD C, E` -> Ld8(Reg8.C, Reg8.E, cycles = 4, size = 1u)
        OpCodes.`LD C, H` -> Ld8(Reg8.C, Reg8.H, cycles = 4, size = 1u)
        OpCodes.`LD C, L` -> Ld8(Reg8.C, Reg8.L, cycles = 4, size = 1u)
        OpCodes.`LD C, (HL)` -> Ld8(Reg8.C, Ind8(Reg16.HL), cycles = 7, size = 1u)
        OpCodes.`LD C, A` -> Ld8(Reg8.C, Reg8.A, cycles = 4, size = 1u)

        // From 0x50 to 0x5F
        OpCodes.`LD D, B` -> Ld8(Reg8.D, Reg8.B, cycles = 4, size = 1u)
        OpCodes.`LD D, C` -> Ld8(Reg8.D, Reg8.C, cycles = 4, size = 1u)
        OpCodes.`LD D, D` -> Ld8(Reg8.D, Reg8.D, cycles = 4, size = 1u)
        OpCodes.`LD D, E` -> Ld8(Reg8.D, Reg8.E, cycles = 4, size = 1u)
        OpCodes.`LD D, H` -> Ld8(Reg8.D, Reg8.H, cycles = 4, size = 1u)
        OpCodes.`LD D, L` -> Ld8(Reg8.D, Reg8.L, cycles = 4, size = 1u)
        OpCodes.`LD D, (HL)` -> Ld8(Reg8.D, Ind8(Reg16.HL), cycles = 7, size = 1u)
        OpCodes.`LD D, A` -> Ld8(Reg8.D, Reg8.A, cycles = 4, size = 1u)
        OpCodes.`LD E, B` -> Ld8(Reg8.E, Reg8.B, cycles = 4, size = 1u)
        OpCodes.`LD E, C` -> Ld8(Reg8.E, Reg8.C, cycles = 4, size = 1u)
        OpCodes.`LD E, D` -> Ld8(Reg8.E, Reg8.D, cycles = 4, size = 1u)
        OpCodes.`LD E, E` -> Ld8(Reg8.E, Reg8.E, cycles = 4, size = 1u)
        OpCodes.`LD E, H` -> Ld8(Reg8.E, Reg8.H, cycles = 4, size = 1u)
        OpCodes.`LD E, L` -> Ld8(Reg8.E, Reg8.L, cycles = 4, size = 1u)
        OpCodes.`LD E, (HL)` -> Ld8(Reg8.E, Ind8(Reg16.HL), cycles = 7, size = 1u)
        OpCodes.`LD E, A` -> Ld8(Reg8.E, Reg8.A, cycles = 4, size = 1u)

        // From 0x60 to 0x6F
        OpCodes.`LD H, B` -> Ld8(Reg8.H, Reg8.B, cycles = 4, size = 1u)
        OpCodes.`LD H, C` -> Ld8(Reg8.H, Reg8.C, cycles = 4, size = 1u)
        OpCodes.`LD H, D` -> Ld8(Reg8.H, Reg8.D, cycles = 4, size = 1u)
        OpCodes.`LD H, E` -> Ld8(Reg8.H, Reg8.E, cycles = 4, size = 1u)
        OpCodes.`LD H, H` -> Ld8(Reg8.H, Reg8.H, cycles = 4, size = 1u)
        OpCodes.`LD H, L` -> Ld8(Reg8.H, Reg8.L, cycles = 4, size = 1u)
        OpCodes.`LD H, (HL)` -> Ld8(Reg8.H, Ind8(Reg16.HL), cycles = 7, size = 1u)
        OpCodes.`LD H, A` -> Ld8(Reg8.H, Reg8.A, cycles = 4, size = 1u)
        OpCodes.`LD L, B` -> Ld8(Reg8.L, Reg8.B, cycles = 4, size = 1u)
        OpCodes.`LD L, C` -> Ld8(Reg8.L, Reg8.C, cycles = 4, size = 1u)
        OpCodes.`LD L, D` -> Ld8(Reg8.L, Reg8.D, cycles = 4, size = 1u)
        OpCodes.`LD L, E` -> Ld8(Reg8.L, Reg8.E, cycles = 4, size = 1u)
        OpCodes.`LD L, H` -> Ld8(Reg8.L, Reg8.H, cycles = 4, size = 1u)
        OpCodes.`LD L, L` -> Ld8(Reg8.L, Reg8.L, cycles = 4, size = 1u)
        OpCodes.`LD L, (HL)` -> Ld8(Reg8.L, Ind8(Reg16.HL), cycles = 7, size = 1u)
        OpCodes.`LD L, A` -> Ld8(Reg8.L, Reg8.A, cycles = 4, size = 1u)

        // From 0x70 to 0x7F
        OpCodes.`LD (HL), B` -> Ld8(Ind8(Reg16.HL), Reg8.B, cycles = 7, size = 1u)
        OpCodes.`LD (HL), C` -> Ld8(Ind8(Reg16.HL), Reg8.C, cycles = 7, size = 1u)
        OpCodes.`LD (HL), D` -> Ld8(Ind8(Reg16.HL), Reg8.D, cycles = 7, size = 1u)
        OpCodes.`LD (HL), E` -> Ld8(Ind8(Reg16.HL), Reg8.E, cycles = 7, size = 1u)
        OpCodes.`LD (HL), H` -> Ld8(Ind8(Reg16.HL), Reg8.H, cycles = 7, size = 1u)
        OpCodes.`LD (HL), L` -> Ld8(Ind8(Reg16.HL), Reg8.L, cycles = 7, size = 1u)
        OpCodes.`HALT` -> Halt
        OpCodes.`LD (HL), A` -> Ld8(Ind8(Reg16.HL), Reg8.A, cycles = 7, size = 1u)
        OpCodes.`LD A, B` -> Ld8(Reg8.A, Reg8.B, cycles = 4, size = 1u)
        OpCodes.`LD A, C` -> Ld8(Reg8.A, Reg8.C, cycles = 4, size = 1u)
        OpCodes.`LD A, D` -> Ld8(Reg8.A, Reg8.D, cycles = 4, size = 1u)
        OpCodes.`LD A, E` -> Ld8(Reg8.A, Reg8.E, cycles = 4, size = 1u)
        OpCodes.`LD A, H` -> Ld8(Reg8.A, Reg8.H, cycles = 4, size = 1u)
        OpCodes.`LD A, L` -> Ld8(Reg8.A, Reg8.L, cycles = 4, size = 1u)
        OpCodes.`LD A, (HL)` -> Ld8(Reg8.A, Ind8(Reg16.HL), cycles = 7, size = 1u)
        OpCodes.`LD A, A` -> Ld8(Reg8.A, Reg8.A, cycles = 4, size = 1u)

        // From 0x80 to 0x8F
        OpCodes.`ADD A, B` -> Add8(Reg8.A, Reg8.B, withCarry = false, cycles = 4, size = 1u)
        OpCodes.`ADD A, C` -> Add8(Reg8.A, Reg8.C, withCarry = false, cycles = 4, size = 1u)
        OpCodes.`ADD A, D` -> Add8(Reg8.A, Reg8.D, withCarry = false, cycles = 4, size = 1u)
        OpCodes.`ADD A, E` -> Add8(Reg8.A, Reg8.E, withCarry = false, cycles = 4, size = 1u)
        OpCodes.`ADD A, H` -> Add8(Reg8.A, Reg8.H, withCarry = false, cycles = 4, size = 1u)
        OpCodes.`ADD A, L` -> Add8(Reg8.A, Reg8.L, withCarry = false, cycles = 4, size = 1u)
        OpCodes.`ADD A, (HL)` -> Add8(Reg8.A, Ind8(Reg16.HL), withCarry = false, cycles = 7, size = 1u)
        OpCodes.`ADD A, A` -> Add8(Reg8.A, Reg8.A, withCarry = false, cycles = 4, size = 1u)
        OpCodes.`ADC A, B` -> Add8(Reg8.A, Reg8.B, withCarry = true, cycles = 4, size = 1u)
        OpCodes.`ADC A, C` -> Add8(Reg8.A, Reg8.C, withCarry = true, cycles = 4, size = 1u)
        OpCodes.`ADC A, D` -> Add8(Reg8.A, Reg8.D, withCarry = true, cycles = 4, size = 1u)
        OpCodes.`ADC A, E` -> Add8(Reg8.A, Reg8.E, withCarry = true, cycles = 4, size = 1u)
        OpCodes.`ADC A, H` -> Add8(Reg8.A, Reg8.H, withCarry = true, cycles = 4, size = 1u)
        OpCodes.`ADC A, L` -> Add8(Reg8.A, Reg8.L, withCarry = true, cycles = 4, size = 1u)
        OpCodes.`ADC A, (HL)` -> Add8(Reg8.A, Ind8(Reg16.HL), withCarry = true, cycles = 7, size = 1u)
        OpCodes.`ADC A, A` -> Add8(Reg8.A, Reg8.A, withCarry = true, cycles = 4, size = 1u)

        OpCodes.`JP NN` -> Jp(Imm16)
        else -> Illegal
    }
}

suspend fun Processor.decode(): Inst {
    val opcode: Int = bus.read(regs.pc).toInt()
    return OPCODES_MAIN[opcode]
}

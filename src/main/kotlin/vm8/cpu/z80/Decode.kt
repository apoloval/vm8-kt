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

    const val `SUB B`       : Int = 0x90
    const val `SUB C`       : Int = 0x91
    const val `SUB D`       : Int = 0x92
    const val `SUB E`       : Int = 0x93
    const val `SUB H`       : Int = 0x94
    const val `SUB L`       : Int = 0x95
    const val `SUB (HL)`    : Int = 0x96
    const val `SUB A`       : Int = 0x97
    const val `SBC B`       : Int = 0x98
    const val `SBC C`       : Int = 0x99
    const val `SBC D`       : Int = 0x9A
    const val `SBC E`       : Int = 0x9B
    const val `SBC H`       : Int = 0x9C
    const val `SBC L`       : Int = 0x9D
    const val `SBC (HL)`    : Int = 0x9E
    const val `SBC A`       : Int = 0x9F

    const val `AND B`       : Int = 0xA0
    const val `AND C`       : Int = 0xA1
    const val `AND D`       : Int = 0xA2
    const val `AND E`       : Int = 0xA3
    const val `AND H`       : Int = 0xA4
    const val `AND L`       : Int = 0xA5
    const val `AND (HL)`    : Int = 0xA6
    const val `AND A`       : Int = 0xA7
    const val `XOR B`       : Int = 0xA8
    const val `XOR C`       : Int = 0xA9
    const val `XOR D`       : Int = 0xAA
    const val `XOR E`       : Int = 0xAB
    const val `XOR H`       : Int = 0xAC
    const val `XOR L`       : Int = 0xAD
    const val `XOR (HL)`    : Int = 0xAE
    const val `XOR A`       : Int = 0xAF

    const val `OR B`        : Int = 0xB0
    const val `OR C`        : Int = 0xB1
    const val `OR D`        : Int = 0xB2
    const val `OR E`        : Int = 0xB3
    const val `OR H`        : Int = 0xB4
    const val `OR L`        : Int = 0xB5
    const val `OR (HL)`     : Int = 0xB6
    const val `OR A`        : Int = 0xB7
    const val `CP B`        : Int = 0xB8
    const val `CP C`        : Int = 0xB9
    const val `CP D`        : Int = 0xBA
    const val `CP E`        : Int = 0xBB
    const val `CP H`        : Int = 0xBC
    const val `CP L`        : Int = 0xBD
    const val `CP (HL)`     : Int = 0xBE
    const val `CP A`        : Int = 0xBF

    const val `RET NZ`      : Int = 0xC0
    const val `POP BC`      : Int = 0xC1
    const val `JP NZ, NN`   : Int = 0xC2
    const val `JP NN`       : Int = 0xC3
    const val `CALL NZ, NN` : Int = 0xC4
    const val `PUSH BC`     : Int = 0xC5
    const val `ADD A, N`    : Int = 0xC6
    const val `RST 0x00`    : Int = 0xC7
    const val `RET Z`       : Int = 0xC8
    const val `RET`         : Int = 0xC9
    const val `JP Z, NN`    : Int = 0xCA
    const val `CALL Z, NN`  : Int = 0xCC
    const val `CALL NN`     : Int = 0xCD
    const val `ADC A, N`    : Int = 0xCE
    const val `RST 0x08`    : Int = 0xCF

    const val `RET NC`      : Int = 0xD0
    const val `POP DE`      : Int = 0xD1
    const val `JP NC, NN`   : Int = 0xD2
    const val `OUT (N), A`  : Int = 0xD3
    const val `CALL NC, NN` : Int = 0xD4
    const val `PUSH DE`     : Int = 0xD5
    const val `SUB N`       : Int = 0xD6
    const val `RST 0x10`    : Int = 0xD7
    const val `RET C`       : Int = 0xD8
    const val `EXX`         : Int = 0xD9
    const val `JP C, NN`    : Int = 0xDA
    const val `IN A, (N)`   : Int = 0xDB
    const val `CALL C, NN`  : Int = 0xDC
    const val `SBC N`       : Int = 0xDE
    const val `RST 0x18`    : Int = 0xDF

    const val `RET PO`      : Int = 0xE0
    const val `POP HL`      : Int = 0xE1
    const val `JP PO, NN`   : Int = 0xE2
    const val `EX (SP), HL` : Int = 0xE3
    const val `CALL PO, NN` : Int = 0xE4
    const val `PUSH HL`     : Int = 0xE5
    const val `AND N`       : Int = 0xE6
    const val `RST 0x20`    : Int = 0xE7
    const val `RET PE`      : Int = 0xE8
    const val `JP (HL)`     : Int = 0xE9
    const val `JP PE, NN`   : Int = 0xEA
    const val `EX DE, HL`   : Int = 0xEB
    const val `CALL PE, NN` : Int = 0xEC
    const val `XOR N`       : Int = 0xEE
    const val `RST 0x28`    : Int = 0xEF

    const val `RET P`       : Int = 0xF0
    const val `POP AF`      : Int = 0xF1
    const val `JP P, NN`    : Int = 0xF2
    const val `DI`          : Int = 0xF3
    const val `CALL P, NN`  : Int = 0xF4
    const val `PUSH AF`     : Int = 0xF5
    const val `OR N`        : Int = 0xF6
    const val `RST 0x30`    : Int = 0xF7
    const val `RET M`       : Int = 0xF8
    const val `LD SP, HL`   : Int = 0xF9
    const val `JP M, NN`    : Int = 0xFA
    const val `EI`          : Int = 0xFB
    const val `CALL M, NN`  : Int = 0xFC
    const val `CP N`        : Int = 0xFE
    const val `RST 0x38`    : Int = 0xFF
}

suspend fun Processor.decode(): Inst {
    return decode(bus.memReadByte(regs.pc))
}

fun Processor.decode(opCode: UByte): Inst = when(opCode.toInt()) {
    /* 0x00 */ OpCodes.`NOP` -> Inst.`NOP`
    /* 0x01 */ OpCodes.`LD BC, NN` -> Inst.`LD BC, NN`
    /* 0x02 */ OpCodes.`LD (BC), A` -> Inst.`LD (BC), A`
    /* 0x03 */ OpCodes.`INC BC` -> Inst.`INC BC`
    /* 0x04 */ OpCodes.`INC B` -> Inst.`INC B`
    /* 0x05 */ OpCodes.`DEC B` -> Inst.`DEC B`
    /* 0x06 */ OpCodes.`LD B, N` -> Inst.`LD B, N`
    /* 0x07 */ OpCodes.`RLCA` -> Inst.`RLCA`
    /* 0x08 */ OpCodes.`EX AF, AF'` -> Inst.`EX AF, AF'`
    /* 0x09 */ OpCodes.`ADD HL, BC` -> Inst.`ADD HL, BC`
    /* 0x0A */ OpCodes.`LD A, (BC)` -> Inst.`LD A, (BC)`
    /* 0x0B */ OpCodes.`DEC BC` -> Inst.`DEC BC`
    /* 0x0C */ OpCodes.`INC C` -> Inst.`INC C`
    /* 0x0D */ OpCodes.`DEC C` -> Inst.`DEC C`
    /* 0x0E */ OpCodes.`LD C, N` -> Inst.`LD C, N`
    /* 0x0F */ OpCodes.`RRCA` -> Inst.`RRCA`

    /* 0x10 */ OpCodes.`DJNZ N` -> Inst.`DJNZ N`
    /* 0x11 */ OpCodes.`LD DE, NN` -> Inst.`LD DE, NN`
    /* 0x12 */ OpCodes.`LD (DE), A` -> Inst.`LD (DE), A`
    /* 0x13 */ OpCodes.`INC DE` -> Inst.`INC DE`
    /* 0x14 */ OpCodes.`INC D` -> Inst.`INC D`
    /* 0x15 */ OpCodes.`DEC D` -> Inst.`DEC D`
    /* 0x16 */ OpCodes.`LD D, N` -> Inst.`LD D, N`
    /* 0x17 */ OpCodes.`RLA` -> Inst.`RLA`
    /* 0x18 */ OpCodes.`JR N` -> Inst.`JR N`
    /* 0x19 */ OpCodes.`ADD HL, DE` -> Inst.`ADD HL, DE`
    /* 0x1A */ OpCodes.`LD A, (DE)` -> Inst.`LD A, (DE)`
    /* 0x1B */ OpCodes.`DEC DE` -> Inst.`DEC DE`
    /* 0x1C */ OpCodes.`INC E` -> Inst.`INC E`
    /* 0x1D */ OpCodes.`DEC E` -> Inst.`DEC E`
    /* 0x1E */ OpCodes.`LD E, N` -> Inst.`LD E, N`
    /* 0x1F */ OpCodes.`RRA` -> Inst.`RRA`

    /* 0x20 */ OpCodes.`JR NZ, N` -> Inst.`JR NZ, N`
    /* 0x21 */ OpCodes.`LD HL, NN` -> Inst.`LD HL, NN`
    /* 0x22 */ OpCodes.`LD (NN), HL` -> Inst.`LD (NN), HL`
    /* 0x23 */ OpCodes.`INC HL` -> Inst.`INC HL`
    /* 0x24 */ OpCodes.`INC H` -> Inst.`INC H`
    /* 0x25 */ OpCodes.`DEC H` -> Inst.`DEC H`
    /* 0x26 */ OpCodes.`LD H, N` -> Inst.`LD H, N`
    /* 0x27 */ OpCodes.`DAA` -> Inst.`DAA`
    /* 0x28 */ OpCodes.`JR Z, N` -> Inst.`JR Z, N`
    /* 0x29 */ OpCodes.`ADD HL, HL` -> Inst.`ADD HL, HL`
    /* 0x2A */ OpCodes.`LD HL, (NN)` -> Inst.`LD HL, (NN)`
    /* 0x2B */ OpCodes.`DEC HL` -> Inst.`DEC HL`
    /* 0x2C */ OpCodes.`INC L` -> Inst.`INC L`
    /* 0x2D */ OpCodes.`DEC L` -> Inst.`DEC L`
    /* 0x2E */ OpCodes.`LD L, N` -> Inst.`LD L, N`
    /* 0x2F */ OpCodes.`CPL` -> Inst.`CPL`

    /* 0x30 */ OpCodes.`JR NC, N` -> Inst.`JR NC, N`
    /* 0x31 */ OpCodes.`LD SP, NN` -> Inst.`LD SP, NN`
    /* 0x32 */ OpCodes.`LD (NN), A` -> Inst.`LD (NN), A`
    /* 0x33 */ OpCodes.`INC SP` -> Inst.`INC SP`
    /* 0x34 */ OpCodes.`INC (HL)` -> Inst.`INC (HL)`
    /* 0x35 */ OpCodes.`DEC (HL)` -> Inst.`DEC (HL)`
    /* 0x36 */ OpCodes.`LD (HL), N` -> Inst.`LD (HL), N`
    /* 0x37 */ OpCodes.`SCF` -> Inst.`SCF`
    /* 0x38 */ OpCodes.`JR C, N` -> Inst.`JR C, N`
    /* 0x39 */ OpCodes.`ADD HL, SP` -> Inst.`ADD HL, SP`
    /* 0x3A */ OpCodes.`LD A, (NN)` -> Inst.`LD A, (NN)`
    /* 0x3B */ OpCodes.`DEC SP` -> Inst.`DEC SP`
    /* 0x3C */ OpCodes.`INC A` -> Inst.`INC A`
    /* 0x3D */ OpCodes.`DEC A` -> Inst.`DEC A`
    /* 0x3E */ OpCodes.`LD A, N` -> Inst.`LD A, N`
    /* 0x3F */ OpCodes.`CCF` -> Inst.`CCF`

    /* 0x40 */ OpCodes.`LD B, B` -> Inst.`LD B, B`
    /* 0x41 */ OpCodes.`LD B, C` -> Inst.`LD B, C`
    /* 0x42 */ OpCodes.`LD B, D` -> Inst.`LD B, D`
    /* 0x43 */ OpCodes.`LD B, E` -> Inst.`LD B, E`
    /* 0x44 */ OpCodes.`LD B, H` -> Inst.`LD B, H`
    /* 0x45 */ OpCodes.`LD B, L` -> Inst.`LD B, L`
    /* 0x46 */ OpCodes.`LD B, (HL)` -> Inst.`LD B, (HL)`
    /* 0x47 */ OpCodes.`LD B, A` -> Inst.`LD B, A`
    /* 0x48 */ OpCodes.`LD C, B` -> Inst.`LD C, B`
    /* 0x49 */ OpCodes.`LD C, C` -> Inst.`LD C, C`
    /* 0x4A */ OpCodes.`LD C, D` -> Inst.`LD C, D`
    /* 0x4B */ OpCodes.`LD C, E` -> Inst.`LD C, E`
    /* 0x4C */ OpCodes.`LD C, H` -> Inst.`LD C, H`
    /* 0x4D */ OpCodes.`LD C, L` -> Inst.`LD C, L`
    /* 0x4E */ OpCodes.`LD C, (HL)` -> Inst.`LD C, (HL)`
    /* 0x4F */ OpCodes.`LD C, A` -> Inst.`LD C, A`

    /* 0x50 */ OpCodes.`LD D, B` -> Inst.`LD D, B`
    /* 0x51 */ OpCodes.`LD D, C` -> Inst.`LD D, C`
    /* 0x52 */ OpCodes.`LD D, D` -> Inst.`LD D, D`
    /* 0x53 */ OpCodes.`LD D, E` -> Inst.`LD D, E`
    /* 0x54 */ OpCodes.`LD D, H` -> Inst.`LD D, H`
    /* 0x55 */ OpCodes.`LD D, L` -> Inst.`LD D, L`
    /* 0x56 */ OpCodes.`LD D, (HL)` -> Inst.`LD D, (HL)`
    /* 0x57 */ OpCodes.`LD D, A` -> Inst.`LD D, A`
    /* 0x58 */ OpCodes.`LD E, B` -> Inst.`LD E, B`
    /* 0x59 */ OpCodes.`LD E, C` -> Inst.`LD E, C`
    /* 0x5A */ OpCodes.`LD E, D` -> Inst.`LD E, D`
    /* 0x5B */ OpCodes.`LD E, E` -> Inst.`LD E, E`
    /* 0x5C */ OpCodes.`LD E, H` -> Inst.`LD E, H`
    /* 0x5D */ OpCodes.`LD E, L` -> Inst.`LD E, L`
    /* 0x5E */ OpCodes.`LD E, (HL)` -> Inst.`LD E, (HL)`
    /* 0x5F */ OpCodes.`LD E, A` -> Inst.`LD E, A`

    /* 0x60 */ OpCodes.`LD H, B` -> Inst.`LD H, B`
    /* 0x61 */ OpCodes.`LD H, C` -> Inst.`LD H, C`
    /* 0x62 */ OpCodes.`LD H, D` -> Inst.`LD H, D`
    /* 0x63 */ OpCodes.`LD H, E` -> Inst.`LD H, E`
    /* 0x64 */ OpCodes.`LD H, H` -> Inst.`LD H, H`
    /* 0x65 */ OpCodes.`LD H, L` -> Inst.`LD H, L`
    /* 0x66 */ OpCodes.`LD H, (HL)` -> Inst.`LD H, (HL)`
    /* 0x67 */ OpCodes.`LD H, A` -> Inst.`LD H, A`
    /* 0x68 */ OpCodes.`LD L, B` -> Inst.`LD L, B`
    /* 0x69 */ OpCodes.`LD L, C` -> Inst.`LD L, C`
    /* 0x6A */ OpCodes.`LD L, D` -> Inst.`LD L, D`
    /* 0x6B */ OpCodes.`LD L, E` -> Inst.`LD L, E`
    /* 0x6C */ OpCodes.`LD L, H` -> Inst.`LD L, H`
    /* 0x6D */ OpCodes.`LD L, L` -> Inst.`LD L, L`
    /* 0x6E */ OpCodes.`LD L, (HL)` -> Inst.`LD L, (HL)`
    /* 0x6F */ OpCodes.`LD L, A` -> Inst.`LD L, A`

    /* 0x70 */ OpCodes.`LD (HL), B` -> Inst.`LD (HL), B`
    /* 0x71 */ OpCodes.`LD (HL), C` -> Inst.`LD (HL), C`
    /* 0x72 */ OpCodes.`LD (HL), D` -> Inst.`LD (HL), D`
    /* 0x73 */ OpCodes.`LD (HL), E` -> Inst.`LD (HL), E`
    /* 0x74 */ OpCodes.`LD (HL), H` -> Inst.`LD (HL), H`
    /* 0x75 */ OpCodes.`LD (HL), L` -> Inst.`LD (HL), L`
    /* 0x76 */ OpCodes.`HALT` -> Inst.`HALT`
    /* 0x77 */ OpCodes.`LD (HL), A` -> Inst.`LD (HL), A`
    /* 0x78 */ OpCodes.`LD A, B` -> Inst.`LD A, B`
    /* 0x79 */ OpCodes.`LD A, C` -> Inst.`LD A, C`
    /* 0x7A */ OpCodes.`LD A, D` -> Inst.`LD A, D`
    /* 0x7B */ OpCodes.`LD A, E` -> Inst.`LD A, E`
    /* 0x7C */ OpCodes.`LD A, H` -> Inst.`LD A, H`
    /* 0x7D */ OpCodes.`LD A, L` -> Inst.`LD A, L`
    /* 0x7E */ OpCodes.`LD A, (HL)` -> Inst.`LD A, (HL)`
    /* 0x7F */ OpCodes.`LD A, A` -> Inst.`LD A, A`

    /* 0x80 */ OpCodes.`ADD A, B` -> Inst.`ADD A, B`
    /* 0x81 */ OpCodes.`ADD A, C` -> Inst.`ADD A, C`
    /* 0x82 */ OpCodes.`ADD A, D` -> Inst.`ADD A, D`
    /* 0x83 */ OpCodes.`ADD A, E` -> Inst.`ADD A, E`
    /* 0x84 */ OpCodes.`ADD A, H` -> Inst.`ADD A, H`
    /* 0x85 */ OpCodes.`ADD A, L` -> Inst.`ADD A, L`
    /* 0x86 */ OpCodes.`ADD A, (HL)` -> Inst.`ADD A, (HL)`
    /* 0x87 */ OpCodes.`ADD A, A` -> Inst.`ADD A, A`
    /* 0x88 */ OpCodes.`ADC A, B` -> Inst.`ADC A, B`
    /* 0x89 */ OpCodes.`ADC A, C` -> Inst.`ADC A, C`
    /* 0x8A */ OpCodes.`ADC A, D` -> Inst.`ADC A, D`
    /* 0x8B */ OpCodes.`ADC A, E` -> Inst.`ADC A, E`
    /* 0x8C */ OpCodes.`ADC A, H` -> Inst.`ADC A, H`
    /* 0x8D */ OpCodes.`ADC A, L` -> Inst.`ADC A, L`
    /* 0x8E */ OpCodes.`ADC A, (HL)` -> Inst.`ADC A, (HL)`
    /* 0x8F */ OpCodes.`ADC A, A` -> Inst.`ADC A, A`

    /* 0x90 */ OpCodes.`SUB B` -> Inst.`SUB B`
    /* 0x91 */ OpCodes.`SUB C` -> Inst.`SUB C`
    /* 0x92 */ OpCodes.`SUB D` -> Inst.`SUB D`
    /* 0x93 */ OpCodes.`SUB E` -> Inst.`SUB E`
    /* 0x94 */ OpCodes.`SUB H` -> Inst.`SUB H`
    /* 0x95 */ OpCodes.`SUB L` -> Inst.`SUB L`
    /* 0x96 */ OpCodes.`SUB (HL)` -> Inst.`SUB (HL)`
    /* 0x97 */ OpCodes.`SUB A` -> Inst.`SUB A`
    /* 0x98 */ OpCodes.`SBC B` -> Inst.`SBC B`
    /* 0x99 */ OpCodes.`SBC C` -> Inst.`SBC C`
    /* 0x9A */ OpCodes.`SBC D` -> Inst.`SBC D`
    /* 0x9B */ OpCodes.`SBC E` -> Inst.`SBC E`
    /* 0x9C */ OpCodes.`SBC H` -> Inst.`SBC H`
    /* 0x9D */ OpCodes.`SBC L` -> Inst.`SBC L`
    /* 0x9E */ OpCodes.`SBC (HL)` -> Inst.`SBC (HL)`
    /* 0x9F */ OpCodes.`SBC A` -> Inst.`SBC A`

    /* 0xA0 */ OpCodes.`AND B` -> Inst.`AND B`
    /* 0xA1 */ OpCodes.`AND C` -> Inst.`AND C`
    /* 0xA2 */ OpCodes.`AND D` -> Inst.`AND D`
    /* 0xA3 */ OpCodes.`AND E` -> Inst.`AND E`
    /* 0xA4 */ OpCodes.`AND H` -> Inst.`AND H`
    /* 0xA5 */ OpCodes.`AND L` -> Inst.`AND L`
    /* 0xA6 */ OpCodes.`AND (HL)` -> Inst.`AND (HL)`
    /* 0xA7 */ OpCodes.`AND A` -> Inst.`AND A`
    /* 0xA8 */ OpCodes.`XOR B` -> Inst.`XOR B`
    /* 0xA9 */ OpCodes.`XOR C` -> Inst.`XOR C`
    /* 0xAA */ OpCodes.`XOR D` -> Inst.`XOR D`
    /* 0xAB */ OpCodes.`XOR E` -> Inst.`XOR E`
    /* 0xAC */ OpCodes.`XOR H` -> Inst.`XOR H`
    /* 0xAD */ OpCodes.`XOR L` -> Inst.`XOR L`
    /* 0xAE */ OpCodes.`XOR (HL)` -> Inst.`XOR (HL)`
    /* 0xAF */ OpCodes.`XOR A` -> Inst.`XOR A`

    /* 0xB0 */ OpCodes.`OR B` -> Inst.`OR B`
    /* 0xB1 */ OpCodes.`OR C` -> Inst.`OR C`
    /* 0xB2 */ OpCodes.`OR D` -> Inst.`OR D`
    /* 0xB3 */ OpCodes.`OR E` -> Inst.`OR E`
    /* 0xB4 */ OpCodes.`OR H` -> Inst.`OR H`
    /* 0xB5 */ OpCodes.`OR L` -> Inst.`OR L`
    /* 0xB6 */ OpCodes.`OR (HL)` -> Inst.`OR (HL)`
    /* 0xB7 */ OpCodes.`OR A` -> Inst.`OR A`
    /* 0xB8 */ OpCodes.`CP B` -> Inst.`CP B`
    /* 0xB9 */ OpCodes.`CP C` -> Inst.`CP C`
    /* 0xBA */ OpCodes.`CP D` -> Inst.`CP D`
    /* 0xBB */ OpCodes.`CP E` -> Inst.`CP E`
    /* 0xBC */ OpCodes.`CP H` -> Inst.`CP H`
    /* 0xBD */ OpCodes.`CP L` -> Inst.`CP L`
    /* 0xBE */ OpCodes.`CP (HL)` -> Inst.`CP (HL)`
    /* 0xBF */ OpCodes.`CP A` -> Inst.`CP A`

    /* 0xC0 */ OpCodes.`RET NZ` -> Inst.`RET NZ`
    /* 0xC1 */ OpCodes.`POP BC` -> Inst.`POP BC`
    /* 0xC2 */ OpCodes.`JP NZ, NN` -> Inst.`JP NZ, NN`
    /* 0xC3 */ OpCodes.`JP NN` -> Inst.`JP NN`
    /* 0xC4 */ OpCodes.`CALL NZ, NN` -> Inst.`CALL NZ, NN`
    /* 0xC5 */ OpCodes.`PUSH BC` -> Inst.`PUSH BC`
    /* 0xC6 */ OpCodes.`ADD A, N` -> Inst.`ADD A, N`
    /* 0xC7 */ OpCodes.`RST 0x00` -> Inst.`RST 0x00`
    /* 0xC8 */ OpCodes.`RET Z` -> Inst.`RET Z`
    /* 0xC9 */ OpCodes.`RET` -> Inst.`RET`
    /* 0xCA */ OpCodes.`JP Z, NN` -> Inst.`JP Z, NN`
    /* 0xCC */ OpCodes.`CALL Z, NN` -> Inst.`CALL Z, NN`
    /* 0xCD */ OpCodes.`CALL NN` -> Inst.`CALL NN`
    /* 0xCE */ OpCodes.`ADC A, N` -> Inst.`ADC A, N`
    /* 0xCF */ OpCodes.`RST 0x08` -> Inst.`RST 0x08`

    /* 0xD0 */ OpCodes.`RET NC` -> Inst.`RET NC`
    /* 0xD1 */ OpCodes.`POP DE` -> Inst.`POP DE`
    /* 0xD2 */ OpCodes.`JP NC, NN` -> Inst.`JP NC, NN`
    /* 0xD3 */ OpCodes.`OUT (N), A` -> Inst.`OUT (N), A`
    /* 0xD4 */ OpCodes.`CALL NC, NN` -> Inst.`CALL NC, NN`
    /* 0xD5 */ OpCodes.`PUSH DE` -> Inst.`PUSH DE`
    /* 0xD6 */ OpCodes.`SUB N` -> Inst.`SUB N`
    /* 0xD7 */ OpCodes.`RST 0x10` -> Inst.`RST 0x10`
    /* 0xD8 */ OpCodes.`RET C` -> Inst.`RET C`
    /* 0xD9 */ OpCodes.`EXX` -> Inst.`EXX`
    /* 0xDA */ OpCodes.`JP C, NN` -> Inst.`JP C, NN`
    /* 0xDB */ OpCodes.`IN A, (N)` -> Inst.`IN A, (N)`
    /* 0xDC */ OpCodes.`CALL C, NN` -> Inst.`CALL C, NN`
    /* 0xDE */ OpCodes.`SBC N` -> Inst.`SBC N`
    /* 0xDF */ OpCodes.`RST 0x18` -> Inst.`RST 0x18`

    /* 0xE0 */ OpCodes.`RET PO` -> Inst.`RET PO`
    /* 0xE1 */ OpCodes.`POP HL` -> Inst.`POP HL`
    /* 0xE2 */ OpCodes.`JP PO, NN` -> Inst.`JP PO, NN`
    /* 0xE3 */ OpCodes.`EX (SP), HL` -> Inst.`EX (SP), HL`
    /* 0xE4 */ OpCodes.`CALL PO, NN` -> Inst.`CALL PO, NN`
    /* 0xE5 */ OpCodes.`PUSH HL` -> Inst.`PUSH HL`
    /* 0xE6 */ OpCodes.`AND N` -> Inst.`AND N`
    /* 0xE7 */ OpCodes.`RST 0x20` -> Inst.`RST 0x20`
    /* 0xE8 */ OpCodes.`RET PE` -> Inst.`RET PE`
    /* 0xE9 */ OpCodes.`JP (HL)` -> Inst.`JP (HL)`
    /* 0xEA */ OpCodes.`JP PE, NN` -> Inst.`JP PE, NN`
    /* 0xEB */ OpCodes.`EX DE, HL` -> Inst.`EX DE, HL`
    /* 0xEC */ OpCodes.`CALL PE, NN` -> Inst.`CALL PE, NN`
    /* 0xEE */ OpCodes.`XOR N` -> Inst.`XOR N`
    /* 0xEF */ OpCodes.`RST 0x28` -> Inst.`RST 0x28`

    /* 0xF0 */ OpCodes.`RET P` -> Inst.`RET P`
    /* 0xF1 */ OpCodes.`POP AF` -> Inst.`POP AF`
    /* 0xF2 */ OpCodes.`JP P, NN` -> Inst.`JP P, NN`
    /* 0xF3 */ OpCodes.`DI` -> Inst.`DI`
    /* 0xF4 */ OpCodes.`CALL P, NN` -> Inst.`CALL P, NN`
    /* 0xF5 */ OpCodes.`PUSH AF` -> Inst.`PUSH AF`
    /* 0xF6 */ OpCodes.`OR N` -> Inst.`OR N`
    /* 0xF7 */ OpCodes.`RST 0x30` -> Inst.`RST 0x30`
    /* 0xF8 */ OpCodes.`RET M` -> Inst.`RET M`
    /* 0xF9 */ OpCodes.`LD SP, HL` -> Inst.`LD SP, HL`
    /* 0xFA */ OpCodes.`JP M, NN` -> Inst.`JP M, NN`
    /* 0xFB */ OpCodes.`EI` -> Inst.`EI`
    /* 0xFC */ OpCodes.`CALL M, NN` -> Inst.`CALL M, NN`
    /* 0xFE */ OpCodes.`CP N` -> Inst.`CP N`
    /* 0xFF */ OpCodes.`RST 0x38` -> Inst.`RST 0x38`

    else -> Illegal
}


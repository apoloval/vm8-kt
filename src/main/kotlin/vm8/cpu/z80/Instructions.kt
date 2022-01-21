package vm8.cpu.z80

import vm8.cpu.z80.Imm8.get
import vm8.data.*

/**
 * An instruction that can be executed by a Z80 processor.
 */
sealed interface Inst {
    /**
     * Execute the instruction over a [Processor].
     */
    suspend fun Processor.exec()

    /**
     * The number of cycles required to execute this instruction
     */
    val cycles: Long

    /**
     * The size of this instruction in bytes
     */
    val size: UByte

    fun Processor.incCycles(value: Long = this@Inst.cycles) {
        cycles += value
    }

    fun Processor.incPC(value: Byte = size.toByte()) {
        regs.pc = regs.pc.increment(value)
    }
    
    companion object {
        /* 0x00 */ val `NOP`            : Inst = Nop
        /* 0x01 */ val `LD BC, NN`      : Inst = Ld16(Reg16.BC, Imm16, cycles = 10, size = 3u)
        /* 0x02 */ val `LD (BC), A`     : Inst = Ld8(Ind8(Reg16.BC), Reg8.A, cycles = 7, size = 1u)
        /* 0x03 */ val `INC BC`         : Inst = Inc16(Reg16.BC, cycles = 6, size = 1u)
        /* 0x04 */ val `INC B`          : Inst = Inc8(Reg8.B, cycles = 4, size = 1u)
        /* 0x05 */ val `DEC B`          : Inst = Dec8(Reg8.B, cycles = 4, size = 1u)
        /* 0x06 */ val `LD B, N`        : Inst = Ld8(Reg8.B, Imm8, cycles = 7, size = 2u)
        /* 0x07 */ val `RLCA`           : Inst = Rlca(cycles = 4, size = 1u)
        /* 0x08 */ val `EX AF, AF'`     : Inst = Ex(Reg16.AF, Reg16.`AF'`, cycles = 4, size = 1u)
        /* 0x09 */ val `ADD HL, BC`     : Inst = Add16(Reg16.HL, Reg16.BC, cycles = 11, size = 1u)
        /* 0x0A */ val `LD A, (BC)`     : Inst = Ld8(Reg8.A, Ind8(Reg16.BC), cycles = 7, size = 1u)
        /* 0x0B */ val `DEC BC`         : Inst = Dec16(Reg16.BC, cycles = 6, size = 1u)
        /* 0x0C */ val `INC C`          : Inst = Inc8(Reg8.C, cycles = 4, size = 1u)
        /* 0x0D */ val `DEC C`          : Inst = Dec8(Reg8.C, cycles = 4, size = 1u)
        /* 0x0E */ val `LD C, N`        : Inst = Ld8(Reg8.C, Imm8, cycles = 7, size = 2u)
        /* 0x0F */ val `RRCA`           : Inst = Rrca(cycles = 4, size = 1u)

        /* 0x10 */ val `DJNZ N`         : Inst = Djnz(Reg8.B, Imm8, jcycles = 13, cycles = 8, size = 2u)
        /* 0x11 */ val `LD DE, NN`      : Inst = Ld16(Reg16.DE, Imm16, cycles = 10, size = 3u)
        /* 0x12 */ val `LD (DE), A`     : Inst = Ld8(Ind8(Reg16.DE), Reg8.A, cycles = 7, size = 1u)
        /* 0x13 */ val `INC DE`         : Inst = Inc16(Reg16.DE, cycles = 6, size = 1u)
        /* 0x14 */ val `INC D`          : Inst = Inc8(Reg8.D, cycles = 4, size = 1u)
        /* 0x15 */ val `DEC D`          : Inst = Dec8(Reg8.D, cycles = 4, size = 1u)
        /* 0x16 */ val `LD D, N`        : Inst = Ld8(Reg8.D, Imm8, cycles = 7, size = 2u)
        /* 0x17 */ val `RLA`            : Inst = Rla(cycles = 4, size = 1u)
        /* 0x18 */ val `JR N`           : Inst = Jr(FlagsPredicate.ALWAYS, Imm8, jcycles = 12, cycles = 12, size = 2u)
        /* 0x19 */ val `ADD HL, DE`     : Inst = Add16(Reg16.HL, Reg16.DE, cycles = 11, size = 1u)
        /* 0x1A */ val `LD A, (DE)`     : Inst = Ld8(Reg8.A, Ind8(Reg16.DE), cycles = 7, size = 1u)
        /* 0x1B */ val `DEC DE`         : Inst = Dec16(Reg16.DE, cycles = 6, size = 1u)
        /* 0x1C */ val `INC E`          : Inst = Inc8(Reg8.E, cycles = 4, size = 1u)
        /* 0x1D */ val `DEC E`          : Inst = Dec8(Reg8.E, cycles = 4, size = 1u)
        /* 0x1E */ val `LD E, N`        : Inst = Ld8(Reg8.E, Imm8, cycles = 7, size = 2u)
        /* 0x1F */ val `RRA`            : Inst = Rra(cycles = 4, size = 1u)

        /* 0x20 */ val `JR NZ, N`       : Inst = Jr(FlagsPredicate.NZ, Imm8, jcycles = 12, cycles = 7, size = 2u)
        /* 0x21 */ val `LD HL, NN`      : Inst = Ld16(Reg16.HL, Imm16, cycles = 10, size = 3u)
        /* 0x22 */ val `LD (NN), HL`    : Inst = Ld16(Ind16(Imm16), Reg16.HL, cycles = 16, size = 3u)
        /* 0x23 */ val `INC HL`         : Inst = Inc16(Reg16.HL, cycles = 6, size = 1u)
        /* 0x24 */ val `INC H`          : Inst = Inc8(Reg8.H, cycles = 4, size = 1u)
        /* 0x25 */ val `DEC H`          : Inst = Dec8(Reg8.H, cycles = 4, size = 1u)
        /* 0x26 */ val `LD H, N`        : Inst = Ld8(Reg8.H, Imm8, cycles = 7, size = 2u)
        /* 0x27 */ val `DAA`            : Inst = Daa(cycles = 4, size = 1u)
        /* 0x28 */ val `JR Z, N`        : Inst = Jr(FlagsPredicate.Z, Imm8, jcycles = 12, cycles = 7, size = 2u)
        /* 0x29 */ val `ADD HL, HL`     : Inst = Add16(Reg16.HL, Reg16.HL, cycles = 11, size = 1u)
        /* 0x2A */ val `LD HL, (NN)`    : Inst = Ld16(Reg16.HL, Ind16(Imm16), cycles = 16, size = 3u)
        /* 0x2B */ val `DEC HL`         : Inst = Dec16(Reg16.HL, cycles = 6, size = 1u)
        /* 0x2C */ val `INC L`          : Inst = Inc8(Reg8.L, cycles = 4, size = 1u)
        /* 0x2D */ val `DEC L`          : Inst = Dec8(Reg8.L, cycles = 4, size = 1u)
        /* 0x2E */ val `LD L, N`        : Inst = Ld8(Reg8.L, Imm8, cycles = 7, size = 2u)
        /* 0x2F */ val `CPL`            : Inst = Cpl(cycles = 4, size = 1u)

        /* 0x30 */ val `JR NC, N`       : Inst = Jr(FlagsPredicate.NC, Imm8, jcycles = 12, cycles = 7, size = 2u)
        /* 0x31 */ val `LD SP, NN`      : Inst = Ld16(Reg16.SP, Imm16, cycles = 10, size = 3u)
        /* 0x32 */ val `LD (NN), A`     : Inst = Ld8(Ind8(Imm16), Reg8.A, cycles = 13, size = 3u)
        /* 0x33 */ val `INC SP`         : Inst = Inc16(Reg16.SP, cycles = 6, size = 1u)
        /* 0x34 */ val `INC (HL)`       : Inst = Inc8(Ind8(Reg16.HL), cycles = 11, size = 1u)
        /* 0x35 */ val `DEC (HL)`       : Inst = Dec8(Ind8(Reg16.HL), cycles = 11, size = 1u)
        /* 0x36 */ val `LD (HL), N`     : Inst = Ld8(Ind8(Reg16.HL), Imm8, cycles = 10, size = 2u)
        /* 0x37 */ val `SCF`            : Inst = Scf(cycles = 4, size = 1u)
        /* 0x38 */ val `JR C, N`        : Inst = Jr(FlagsPredicate.C, Imm8, jcycles = 12, cycles = 7, size = 2u)
        /* 0x39 */ val `ADD HL, SP`     : Inst = Add16(Reg16.HL, Reg16.SP, cycles = 11, size = 1u)
        /* 0x3A */ val `LD A, (NN)`     : Inst = Ld8(Reg8.A, Ind8(Imm16), cycles = 13, size = 3u)
        /* 0x3B */ val `DEC SP`         : Inst = Dec16(Reg16.SP, cycles = 6, size = 1u)
        /* 0x3C */ val `INC A`          : Inst = Inc8(Reg8.A, cycles = 4, size = 1u)
        /* 0x3D */ val `DEC A`          : Inst = Dec8(Reg8.A, cycles = 4, size = 1u)
        /* 0x3E */ val `LD A, N`        : Inst = Ld8(Reg8.A, Imm8, cycles = 7, size = 2u)
        /* 0x3F */ val `CCF`            : Inst = Ccf(cycles = 4, size = 1u)

        /* 0x40 */ val `LD B, B`        : Inst = Ld8(Reg8.B, Reg8.B, cycles = 4, size = 1u)
        /* 0x41 */ val `LD B, C`        : Inst = Ld8(Reg8.B, Reg8.C, cycles = 4, size = 1u)
        /* 0x42 */ val `LD B, D`        : Inst = Ld8(Reg8.B, Reg8.D, cycles = 4, size = 1u)
        /* 0x43 */ val `LD B, E`        : Inst = Ld8(Reg8.B, Reg8.E, cycles = 4, size = 1u)
        /* 0x44 */ val `LD B, H`        : Inst = Ld8(Reg8.B, Reg8.H, cycles = 4, size = 1u)
        /* 0x45 */ val `LD B, L`        : Inst = Ld8(Reg8.B, Reg8.L, cycles = 4, size = 1u)
        /* 0x46 */ val `LD B, (HL)`     : Inst = Ld8(Reg8.B, Ind8(Reg16.HL), cycles = 7, size = 1u)
        /* 0x47 */ val `LD B, A`        : Inst = Ld8(Reg8.B, Reg8.A, cycles = 4, size = 1u)
        /* 0x48 */ val `LD C, B`        : Inst = Ld8(Reg8.C, Reg8.B, cycles = 4, size = 1u)
        /* 0x49 */ val `LD C, C`        : Inst = Ld8(Reg8.C, Reg8.C, cycles = 4, size = 1u)
        /* 0x4A */ val `LD C, D`        : Inst = Ld8(Reg8.C, Reg8.D, cycles = 4, size = 1u)
        /* 0x4B */ val `LD C, E`        : Inst = Ld8(Reg8.C, Reg8.E, cycles = 4, size = 1u)
        /* 0x4C */ val `LD C, H`        : Inst = Ld8(Reg8.C, Reg8.H, cycles = 4, size = 1u)
        /* 0x4D */ val `LD C, L`        : Inst = Ld8(Reg8.C, Reg8.L, cycles = 4, size = 1u)
        /* 0x4E */ val `LD C, (HL)`     : Inst = Ld8(Reg8.C, Ind8(Reg16.HL), cycles = 7, size = 1u)
        /* 0x4F */ val `LD C, A`        : Inst = Ld8(Reg8.C, Reg8.A, cycles = 4, size = 1u)

        /* 0x50 */ val `LD D, B`        : Inst = Ld8(Reg8.D, Reg8.B, cycles = 4, size = 1u)
        /* 0x51 */ val `LD D, C`        : Inst = Ld8(Reg8.D, Reg8.C, cycles = 4, size = 1u)
        /* 0x52 */ val `LD D, D`        : Inst = Ld8(Reg8.D, Reg8.D, cycles = 4, size = 1u)
        /* 0x53 */ val `LD D, E`        : Inst = Ld8(Reg8.D, Reg8.E, cycles = 4, size = 1u)
        /* 0x54 */ val `LD D, H`        : Inst = Ld8(Reg8.D, Reg8.H, cycles = 4, size = 1u)
        /* 0x55 */ val `LD D, L`        : Inst = Ld8(Reg8.D, Reg8.L, cycles = 4, size = 1u)
        /* 0x56 */ val `LD D, (HL)`     : Inst = Ld8(Reg8.D, Ind8(Reg16.HL), cycles = 7, size = 1u)
        /* 0x57 */ val `LD D, A`        : Inst = Ld8(Reg8.D, Reg8.A, cycles = 4, size = 1u)
        /* 0x58 */ val `LD E, B`        : Inst = Ld8(Reg8.E, Reg8.B, cycles = 4, size = 1u)
        /* 0x59 */ val `LD E, C`        : Inst = Ld8(Reg8.E, Reg8.C, cycles = 4, size = 1u)
        /* 0x5A */ val `LD E, D`        : Inst = Ld8(Reg8.E, Reg8.D, cycles = 4, size = 1u)
        /* 0x5B */ val `LD E, E`        : Inst = Ld8(Reg8.E, Reg8.E, cycles = 4, size = 1u)
        /* 0x5C */ val `LD E, H`        : Inst = Ld8(Reg8.E, Reg8.H, cycles = 4, size = 1u)
        /* 0x5D */ val `LD E, L`        : Inst = Ld8(Reg8.E, Reg8.L, cycles = 4, size = 1u)
        /* 0x5E */ val `LD E, (HL)`     : Inst = Ld8(Reg8.E, Ind8(Reg16.HL), cycles = 7, size = 1u)
        /* 0x5F */ val `LD E, A`        : Inst = Ld8(Reg8.E, Reg8.A, cycles = 4, size = 1u)

        /* 0x60 */ val `LD H, B`        : Inst = Ld8(Reg8.H, Reg8.B, cycles = 4, size = 1u)
        /* 0x61 */ val `LD H, C`        : Inst = Ld8(Reg8.H, Reg8.C, cycles = 4, size = 1u)
        /* 0x62 */ val `LD H, D`        : Inst = Ld8(Reg8.H, Reg8.D, cycles = 4, size = 1u)
        /* 0x63 */ val `LD H, E`        : Inst = Ld8(Reg8.H, Reg8.E, cycles = 4, size = 1u)
        /* 0x64 */ val `LD H, H`        : Inst = Ld8(Reg8.H, Reg8.H, cycles = 4, size = 1u)
        /* 0x65 */ val `LD H, L`        : Inst = Ld8(Reg8.H, Reg8.L, cycles = 4, size = 1u)
        /* 0x66 */ val `LD H, (HL)`     : Inst = Ld8(Reg8.H, Ind8(Reg16.HL), cycles = 7, size = 1u)
        /* 0x67 */ val `LD H, A`        : Inst = Ld8(Reg8.H, Reg8.A, cycles = 4, size = 1u)
        /* 0x68 */ val `LD L, B`        : Inst = Ld8(Reg8.L, Reg8.B, cycles = 4, size = 1u)
        /* 0x69 */ val `LD L, C`        : Inst = Ld8(Reg8.L, Reg8.C, cycles = 4, size = 1u)
        /* 0x6A */ val `LD L, D`        : Inst = Ld8(Reg8.L, Reg8.D, cycles = 4, size = 1u)
        /* 0x6B */ val `LD L, E`        : Inst = Ld8(Reg8.L, Reg8.E, cycles = 4, size = 1u)
        /* 0x6C */ val `LD L, H`        : Inst = Ld8(Reg8.L, Reg8.H, cycles = 4, size = 1u)
        /* 0x6D */ val `LD L, L`        : Inst = Ld8(Reg8.L, Reg8.L, cycles = 4, size = 1u)
        /* 0x6E */ val `LD L, (HL)`     : Inst = Ld8(Reg8.L, Ind8(Reg16.HL), cycles = 7, size = 1u)
        /* 0x6F */ val `LD L, A`        : Inst = Ld8(Reg8.L, Reg8.A, cycles = 4, size = 1u)

        /* 0x70 */ val `LD (HL), B`     : Inst = Ld8(Ind8(Reg16.HL), Reg8.B, cycles = 7, size = 1u)
        /* 0x71 */ val `LD (HL), C`     : Inst = Ld8(Ind8(Reg16.HL), Reg8.C, cycles = 7, size = 1u)
        /* 0x72 */ val `LD (HL), D`     : Inst = Ld8(Ind8(Reg16.HL), Reg8.D, cycles = 7, size = 1u)
        /* 0x73 */ val `LD (HL), E`     : Inst = Ld8(Ind8(Reg16.HL), Reg8.E, cycles = 7, size = 1u)
        /* 0x74 */ val `LD (HL), H`     : Inst = Ld8(Ind8(Reg16.HL), Reg8.H, cycles = 7, size = 1u)
        /* 0x75 */ val `LD (HL), L`     : Inst = Ld8(Ind8(Reg16.HL), Reg8.L, cycles = 7, size = 1u)
        /* 0x76 */ val `HALT`           : Inst = Halt
        /* 0x77 */ val `LD (HL), A`     : Inst = Ld8(Ind8(Reg16.HL), Reg8.A, cycles = 7, size = 1u)
        /* 0x78 */ val `LD A, B`        : Inst = Ld8(Reg8.A, Reg8.B, cycles = 4, size = 1u)
        /* 0x79 */ val `LD A, C`        : Inst = Ld8(Reg8.A, Reg8.C, cycles = 4, size = 1u)
        /* 0x7A */ val `LD A, D`        : Inst = Ld8(Reg8.A, Reg8.D, cycles = 4, size = 1u)
        /* 0x7B */ val `LD A, E`        : Inst = Ld8(Reg8.A, Reg8.E, cycles = 4, size = 1u)
        /* 0x7C */ val `LD A, H`        : Inst = Ld8(Reg8.A, Reg8.H, cycles = 4, size = 1u)
        /* 0x7D */ val `LD A, L`        : Inst = Ld8(Reg8.A, Reg8.L, cycles = 4, size = 1u)
        /* 0x7E */ val `LD A, (HL)`     : Inst = Ld8(Reg8.A, Ind8(Reg16.HL), cycles = 7, size = 1u)
        /* 0x7F */ val `LD A, A`        : Inst = Ld8(Reg8.A, Reg8.A, cycles = 4, size = 1u)

        /* 0x80 */ val `ADD A, B`       : Inst = Add8(Reg8.A, Reg8.B, withCarry = false, cycles = 4, size = 1u)
        /* 0x81 */ val `ADD A, C`       : Inst = Add8(Reg8.A, Reg8.C, withCarry = false, cycles = 4, size = 1u)
        /* 0x82 */ val `ADD A, D`       : Inst = Add8(Reg8.A, Reg8.D, withCarry = false, cycles = 4, size = 1u)
        /* 0x83 */ val `ADD A, E`       : Inst = Add8(Reg8.A, Reg8.E, withCarry = false, cycles = 4, size = 1u)
        /* 0x84 */ val `ADD A, H`       : Inst = Add8(Reg8.A, Reg8.H, withCarry = false, cycles = 4, size = 1u)
        /* 0x85 */ val `ADD A, L`       : Inst = Add8(Reg8.A, Reg8.L, withCarry = false, cycles = 4, size = 1u)
        /* 0x86 */ val `ADD A, (HL)`    : Inst = Add8(Reg8.A, Ind8(Reg16.HL), withCarry = false, cycles = 7, size = 1u)
        /* 0x87 */ val `ADD A, A`       : Inst = Add8(Reg8.A, Reg8.A, withCarry = false, cycles = 4, size = 1u)
        /* 0x88 */ val `ADC A, B`       : Inst = Add8(Reg8.A, Reg8.B, withCarry = true, cycles = 4, size = 1u)
        /* 0x89 */ val `ADC A, C`       : Inst = Add8(Reg8.A, Reg8.C, withCarry = true, cycles = 4, size = 1u)
        /* 0x8A */ val `ADC A, D`       : Inst = Add8(Reg8.A, Reg8.D, withCarry = true, cycles = 4, size = 1u)
        /* 0x8B */ val `ADC A, E`       : Inst = Add8(Reg8.A, Reg8.E, withCarry = true, cycles = 4, size = 1u)
        /* 0x8C */ val `ADC A, H`       : Inst = Add8(Reg8.A, Reg8.H, withCarry = true, cycles = 4, size = 1u)
        /* 0x8D */ val `ADC A, L`       : Inst = Add8(Reg8.A, Reg8.L, withCarry = true, cycles = 4, size = 1u)
        /* 0x8E */ val `ADC A, (HL)`    : Inst = Add8(Reg8.A, Ind8(Reg16.HL), withCarry = true, cycles = 7, size = 1u)
        /* 0x8F */ val `ADC A, A`       : Inst = Add8(Reg8.A, Reg8.A, withCarry = true, cycles = 4, size = 1u)

        /* 0x90 */ val `SUB B`          : Inst = Sub8(Reg8.A, Reg8.B, withCarry = false, cycles = 4, size = 1u)
        /* 0x91 */ val `SUB C`          : Inst = Sub8(Reg8.A, Reg8.C, withCarry = false, cycles = 4, size = 1u)
        /* 0x92 */ val `SUB D`          : Inst = Sub8(Reg8.A, Reg8.D, withCarry = false, cycles = 4, size = 1u)
        /* 0x93 */ val `SUB E`          : Inst = Sub8(Reg8.A, Reg8.E, withCarry = false, cycles = 4, size = 1u)
        /* 0x94 */ val `SUB H`          : Inst = Sub8(Reg8.A, Reg8.H, withCarry = false, cycles = 4, size = 1u)
        /* 0x95 */ val `SUB L`          : Inst = Sub8(Reg8.A, Reg8.L, withCarry = false, cycles = 4, size = 1u)
        /* 0x96 */ val `SUB (HL)`       : Inst = Sub8(Reg8.A, Ind8(Reg16.HL), withCarry = false, cycles = 7, size = 1u)
        /* 0x97 */ val `SUB A`          : Inst = Sub8(Reg8.A, Reg8.A, withCarry = false, cycles = 4, size = 1u)
        /* 0x98 */ val `SBC B`          : Inst = Sub8(Reg8.A, Reg8.B, withCarry = true, cycles = 4, size = 1u)
        /* 0x99 */ val `SBC C`          : Inst = Sub8(Reg8.A, Reg8.C, withCarry = true, cycles = 4, size = 1u)
        /* 0x9A */ val `SBC D`          : Inst = Sub8(Reg8.A, Reg8.D, withCarry = true, cycles = 4, size = 1u)
        /* 0x9B */ val `SBC E`          : Inst = Sub8(Reg8.A, Reg8.E, withCarry = true, cycles = 4, size = 1u)
        /* 0x9C */ val `SBC H`          : Inst = Sub8(Reg8.A, Reg8.H, withCarry = true, cycles = 4, size = 1u)
        /* 0x9D */ val `SBC L`          : Inst = Sub8(Reg8.A, Reg8.L, withCarry = true, cycles = 4, size = 1u)
        /* 0x9E */ val `SBC (HL)`       : Inst = Sub8(Reg8.A, Ind8(Reg16.HL), withCarry = true, cycles = 7, size = 1u)
        /* 0x9F */ val `SBC A`          : Inst = Sub8(Reg8.A, Reg8.A, withCarry = true, cycles = 4, size = 1u)

        /* 0xA0 */ val `AND B`          : Inst = And8(Reg8.A, Reg8.B,  cycles = 4, size = 1u)
        /* 0xA1 */ val `AND C`          : Inst = And8(Reg8.A, Reg8.C,  cycles = 4, size = 1u)
        /* 0xA2 */ val `AND D`          : Inst = And8(Reg8.A, Reg8.D,  cycles = 4, size = 1u)
        /* 0xA3 */ val `AND E`          : Inst = And8(Reg8.A, Reg8.E,  cycles = 4, size = 1u)
        /* 0xA4 */ val `AND H`          : Inst = And8(Reg8.A, Reg8.H,  cycles = 4, size = 1u)
        /* 0xA5 */ val `AND L`          : Inst = And8(Reg8.A, Reg8.L,  cycles = 4, size = 1u)
        /* 0xA6 */ val `AND (HL)`       : Inst = And8(Reg8.A, Ind8(Reg16.HL), cycles = 7, size = 1u)
        /* 0xA7 */ val `AND A`          : Inst = And8(Reg8.A, Reg8.A, cycles = 4, size = 1u)
        /* 0xA8 */ val `XOR B`          : Inst = Xor8(Reg8.A, Reg8.B,  cycles = 4, size = 1u)
        /* 0xA9 */ val `XOR C`          : Inst = Xor8(Reg8.A, Reg8.C,  cycles = 4, size = 1u)
        /* 0xAA */ val `XOR D`          : Inst = Xor8(Reg8.A, Reg8.D,  cycles = 4, size = 1u)
        /* 0xAB */ val `XOR E`          : Inst = Xor8(Reg8.A, Reg8.E,  cycles = 4, size = 1u)
        /* 0xAC */ val `XOR H`          : Inst = Xor8(Reg8.A, Reg8.H,  cycles = 4, size = 1u)
        /* 0xAD */ val `XOR L`          : Inst = Xor8(Reg8.A, Reg8.L,  cycles = 4, size = 1u)
        /* 0xAE */ val `XOR (HL)`       : Inst = Xor8(Reg8.A, Ind8(Reg16.HL), cycles = 7, size = 1u)
        /* 0xAF */ val `XOR A`          : Inst = Xor8(Reg8.A, Reg8.A, cycles = 4, size = 1u)

        /* 0xB0 */ val `OR B`           : Inst = Or8(Reg8.A, Reg8.B,  cycles = 4, size = 1u)
        /* 0xB1 */ val `OR C`           : Inst = Or8(Reg8.A, Reg8.C,  cycles = 4, size = 1u)
        /* 0xB2 */ val `OR D`           : Inst = Or8(Reg8.A, Reg8.D,  cycles = 4, size = 1u)
        /* 0xB3 */ val `OR E`           : Inst = Or8(Reg8.A, Reg8.E,  cycles = 4, size = 1u)
        /* 0xB4 */ val `OR H`           : Inst = Or8(Reg8.A, Reg8.H,  cycles = 4, size = 1u)
        /* 0xB5 */ val `OR L`           : Inst = Or8(Reg8.A, Reg8.L,  cycles = 4, size = 1u)
        /* 0xB6 */ val `OR (HL)`        : Inst = Or8(Reg8.A, Ind8(Reg16.HL), cycles = 7, size = 1u)
        /* 0xB7 */ val `OR A`           : Inst = Or8(Reg8.A, Reg8.A, cycles = 4, size = 1u)
        /* 0xB8 */ val `CP B`           : Inst = Cp8(Reg8.A, Reg8.B,  cycles = 4, size = 1u)
        /* 0xB9 */ val `CP C`           : Inst = Cp8(Reg8.A, Reg8.C,  cycles = 4, size = 1u)
        /* 0xBA */ val `CP D`           : Inst = Cp8(Reg8.A, Reg8.D,  cycles = 4, size = 1u)
        /* 0xBB */ val `CP E`           : Inst = Cp8(Reg8.A, Reg8.E,  cycles = 4, size = 1u)
        /* 0xBC */ val `CP H`           : Inst = Cp8(Reg8.A, Reg8.H,  cycles = 4, size = 1u)
        /* 0xBD */ val `CP L`           : Inst = Cp8(Reg8.A, Reg8.L,  cycles = 4, size = 1u)
        /* 0xBE */ val `CP (HL)`        : Inst = Cp8(Reg8.A, Ind8(Reg16.HL), cycles = 7, size = 1u)
        /* 0xBF */ val `CP A`           : Inst = Cp8(Reg8.A, Reg8.A, cycles = 4, size = 1u)

        /* 0xC0 */ val `RET NZ`         : Inst = Ret(FlagsPredicate.NZ, jcycles = 11, cycles = 5, size = 1u)
        /* 0xC1 */ val `POP BC`         : Inst = Pop(Reg16.BC, cycles = 10, size = 1u)
        /* 0xC2 */ val `JP NZ, NN`      : Inst = Jp(FlagsPredicate.NZ, Imm16, cycles = 10, size = 3u)
        /* 0xC3 */ val `JP NN`          : Inst = Jp(FlagsPredicate.ALWAYS, Imm16, cycles = 10, size = 3u)
        /* 0xC4 */ val `CALL NZ, NN`    : Inst = Call(FlagsPredicate.NZ)
        /* 0xC5 */ val `PUSH BC`        : Inst = Push(Reg16.BC, cycles = 11, size = 1u)
        /* 0xC6 */ val `ADD A, N`       : Inst = Add8(Reg8.A, Imm8, withCarry = false, cycles = 7, size = 2u)
        /* 0xC7 */ val `RST 0x00`       : Inst = Rst(0x0000u)
        /* 0xC8 */ val `RET Z`          : Inst = Ret(FlagsPredicate.Z, jcycles = 11, cycles = 5, size = 1u)
        /* 0xC9 */ val `RET`            : Inst = Ret(FlagsPredicate.ALWAYS, jcycles = 10, cycles = 10, size = 1u)
        /* 0xCA */ val `JP Z, NN`       : Inst = Jp(FlagsPredicate.Z, Imm16, cycles = 10, size = 3u)
        /* 0xCC */ val `CALL Z, NN`     : Inst = Call(FlagsPredicate.Z)
        /* 0xCD */ val `CALL NN`        : Inst = Call(FlagsPredicate.ALWAYS)
        /* 0xCE */ val `ADC A, N`       : Inst = Add8(Reg8.A, Imm8, withCarry = true, cycles = 7, size = 2u)
        /* 0xCF */ val `RST 0x08`       : Inst = Rst(0x0008u)

        /* 0xD0 */ val `RET NC`         : Inst = Ret(FlagsPredicate.NC, jcycles = 11, cycles = 5, size = 1u)
        /* 0xD1 */ val `POP DE`         : Inst = Pop(Reg16.DE, cycles = 10, size = 1u)
        /* 0xD2 */ val `JP NC, NN`      : Inst = Jp(FlagsPredicate.NC, Imm16, cycles = 10, size = 3u)
        /* 0xD3 */ val `OUT (N), A`     : Inst = Out(Imm8, Reg8.A, cycles = 11, size = 2u)
        /* 0xD4 */ val `CALL NC, NN`    : Inst = Call(FlagsPredicate.NC)
        /* 0xD5 */ val `PUSH DE`        : Inst = Push(Reg16.DE, cycles = 11, size = 1u)
        /* 0xD6 */ val `SUB N`          : Inst = Sub8(Reg8.A, Imm8, withCarry = false, cycles = 7, size = 2u)
        /* 0xD7 */ val `RST 0x10`       : Inst = Rst(0x0010u)
        /* 0xD8 */ val `RET C`          : Inst = Ret(FlagsPredicate.C, jcycles = 11, cycles = 5, size = 1u)
        /* 0xD9 */ val `EXX`            : Inst = Exx
        /* 0xDA */ val `JP C, NN`       : Inst = Jp(FlagsPredicate.C, Imm16, cycles = 10, size = 3u)
        /* 0xDB */ val `IN A, (N)`      : Inst = In(Reg8.A, Imm8, cycles = 11, size = 2u)
        /* 0xDC */ val `CALL C, NN`     : Inst = Call(FlagsPredicate.C)
        /* 0xDE */ val `SBC N`          : Inst = Sub8(Reg8.A, Imm8, withCarry = true, cycles = 7, size = 2u)
        /* 0xDF */ val `RST 0x18`       : Inst = Rst(0x0018u)

        /* 0xE0 */ val `RET PO`         : Inst = Ret(FlagsPredicate.PO, jcycles = 11, cycles = 5, size = 1u)
        /* 0xE1 */ val `POP HL`         : Inst = Pop(Reg16.HL, cycles = 10, size = 1u)
        /* 0xE2 */ val `JP PO, NN`      : Inst = Jp(FlagsPredicate.PO, Imm16, cycles = 10, size = 3u)
        /* 0xE3 */ val `EX (SP), HL`    : Inst = Ex(Ind16(Reg16.SP), Reg16.HL, cycles = 19, size = 1u)
        /* 0xE4 */ val `CALL PO, NN`    : Inst = Call(FlagsPredicate.PO)
        /* 0xE5 */ val `PUSH HL`        : Inst = Push(Reg16.HL, cycles = 11, size = 1u)
        /* 0xE6 */ val `AND N`          : Inst = And8(Reg8.A, Imm8, cycles = 7, size = 2u)
        /* 0xE7 */ val `RST 0x20`       : Inst = Rst(0x0020u)
        /* 0xE8 */ val `RET PE`         : Inst = Ret(FlagsPredicate.PE, jcycles = 11, cycles = 5, size = 1u)
        /* 0xE9 */ val `JP (HL)`        : Inst = Jp(FlagsPredicate.ALWAYS, Reg16.HL, cycles = 4, size = 1u)
        /* 0xEA */ val `JP PE, NN`      : Inst = Jp(FlagsPredicate.PE, Imm16, cycles = 10, size = 3u)
        /* 0xEB */ val `EX DE, HL`      : Inst = Ex(Reg16.DE, Reg16.HL, cycles = 4, size = 1u)
        /* 0xEC */ val `CALL PE, NN`    : Inst = Call(FlagsPredicate.PE)
        /* 0xEE */ val `XOR N`          : Inst = Xor8(Reg8.A, Imm8, cycles = 7, size = 2u)
        /* 0xEF */ val `RST 0x28`       : Inst = Rst(0x0028u)

        /* 0xF0 */ val `RET P`          : Inst = Ret(FlagsPredicate.P, jcycles = 11, cycles = 5, size = 1u)
        /* 0xF1 */ val `POP AF`         : Inst = Pop(Reg16.AF, cycles = 10, size = 1u)
        /* 0xF2 */ val `JP P, NN`       : Inst = Jp(FlagsPredicate.P, Imm16, cycles = 10, size = 3u)
        /* 0xF3 */ val `DI`             : Inst = Di
        /* 0xF4 */ val `CALL P, NN`     : Inst = Call(FlagsPredicate.P)
        /* 0xF5 */ val `PUSH AF`        : Inst = Push(Reg16.AF, cycles = 11, size = 1u)
        /* 0xF6 */ val `OR N`           : Inst = Or8(Reg8.A, Imm8, cycles = 7, size = 2u)
        /* 0xF7 */ val `RST 0x30`       : Inst = Rst(0x0030u)
        /* 0xF8 */ val `RET M`          : Inst = Ret(FlagsPredicate.M, jcycles = 11, cycles = 5, size = 1u)
        /* 0xF9 */ val `LD SP, HL`      : Inst = Ld16(Reg16.SP, Reg16.HL, cycles = 6, size = 1u)
        /* 0xFA */ val `JP M, NN`       : Inst = Jp(FlagsPredicate.M, Imm16, cycles = 10, size = 3u)
        /* 0xFB */ val `EI`             : Inst = Ei
        /* 0xFC */ val `CALL M, NN`     : Inst = Call(FlagsPredicate.M)
        /* 0xFE */ val `CP N`           : Inst = Cp8(Reg8.A, Imm8, cycles = 7, size = 2u)
        /* 0xFF */ val `RST 0x38`       : Inst = Rst(0x0038u)
    }
}

/**
 * ADD instruction for 8-bit operands.
 */
data class Add8(
    val dst: DestOp8,
    val src: SrcOp8,
    val withCarry: Boolean,
    override val cycles: Long,
    override val size: UByte,
) : Inst {
    override suspend fun Processor.exec() {
        val a = load8(dst)
        var b = load8(src)
        if (withCarry && Flag.C.isSet(regs.f)) {
            b++
        }
        val c = (a + b).toUByte()
        store8(dst, c)
        apply(PrecomputedFlags.ofAdd(a, b))
        incCycles()
        incPC()
    }
}

/**
 * ADD instruction for 16-bit operands.
 */
data class Add16(
    val dst: DestOp16,
    val src: SrcOp16,
    override val cycles: Long,
    override val size: UByte,
) : Inst {
    override suspend fun Processor.exec() {
        val a = load16(dst)
        val b = load16(src)
        val c = (a + b).toUShort()
        store16(dst, c)
        apply(PrecomputedFlags.ofAdd(a, b))
        incPC()
        incCycles()
    }
}

/**
 * AND instruction for 8-bit operands.
 */
data class And8(
    val dst: DestOp8,
    val src: SrcOp8,
    override val cycles: Long,
    override val size: UByte,
) : Inst {
    override suspend fun Processor.exec() {
        val a = load8(dst)
        val b = load8(src)
        val c = (a and b)
        store8(dst, c)
        apply(PrecomputedFlags.ofAnd(c))
        incPC()
        incCycles()
    }
}

/**
 * CALL instruction.
 */
data class Call(val cond: FlagsPredicate) : Inst {
    override val cycles: Long = 10L
    override val size: UByte = 3u

    override suspend fun Processor.exec() {
        if (cond.evaluate(regs.f)) {
            val dest = load16(Imm16)
            call(dest)
            cycles += 17
        } else {
            incPC()
            incCycles()
        }
    }
}

/**
 * CCF instruction.
 */
data class Ccf(override val cycles: Long, override val size: UByte) : Inst {
    override suspend fun Processor.exec() {
        apply(
            (Flag.C on (Flag.C.isReset(regs.f))) and
            (Flag.H on (Flag.C.isSet(regs.f))) and
            (Flag.F3 on regs.a.bit(3)) and
            (Flag.F5 on regs.a.bit(5)) - Flag.N
        )
        incPC()
        incCycles()
    }
}

/**
 * CP instruction for 8-bit operands.
 */
data class Cp8(
    val dst: DestOp8, 
    val src: SrcOp8, 
    override val cycles: Long, 
    override val size: UByte,
) : Inst {
    override suspend fun Processor.exec() {
        val a = load8(dst)
        val b = load8(src)
        apply(PrecomputedFlags.ofCp(a, b))
        incPC()
        incCycles()
    }
}

/**
 * CPL instruction.
 */
data class Cpl(override val cycles: Long, override val size: UByte) : Inst {
    override suspend fun Processor.exec() {
        regs.a = regs.a.inv()
        apply(
            (Flag.F3 on regs.a.bit(3)) and
            (Flag.F5 on regs.a.bit(5)) + Flag.N + Flag.H
        )
        incPC()
        incCycles()
    }
}

/**
 * DAA instruction.
 */
data class Daa(override val cycles: Long, override val size: UByte) : Inst {
    override suspend fun Processor.exec() {
        var hasHalfCarry = false
        var hasCarry = false
        if (Flag.N.isReset(regs.f)) {
            if (regs.a.low() > 9u || Flag.H.isSet(regs.f)) {
                regs.a = regs.a.increment(0x06)
                hasHalfCarry = true
            }
            if (regs.a.high() > 9u || Flag.C.isSet(regs.f)) {
                regs.a = regs.a.increment(0x60)
                hasCarry = true
            }
        } else {
            if (regs.a.low() > 9u || Flag.H.isSet(regs.f)) {
                regs.a = regs.a.increment(-0x06)
                hasHalfCarry = true
            }
            if (regs.a.high() > 9u || Flag.C.isSet(regs.f)) {
                regs.a = regs.a.increment(-0x60)
                hasCarry = true
            }
        }

        apply(
            (Flag.C on hasCarry) and
            (Flag.P on regs.a.parity()) and
            (Flag.F3 on regs.a.bit(3)) and
            (Flag.H on hasHalfCarry) and
            (Flag.F5 on regs.a.bit(5)) and
            (Flag.Z on regs.a.isZero()) and
            (Flag.S on regs.a.isNegative())
        )

        incPC()
        incCycles()
    }
}

/**
 * DEC instruction for 8-bit operands
 */
data class Dec8(val dest: DestOp8, override val cycles: Long, override val size: UByte) : Inst {
    override suspend fun Processor.exec() {
        val a = load8(dest)
        val c = a.dec()
        store8(dest, c)
        apply(PrecomputedFlags.ofDec(a))
        incPC()
        incCycles()
    }
}

/**
 * DEC instruction for 16-bit operands
 */
data class Dec16(val dst: DestOp16, override val cycles: Long, override val size: UByte) : Inst {
    override suspend fun Processor.exec() {
        val a = load16(dst)
        val c = a.dec()
        store16(dst, c)
        incPC()
        incCycles()
    }
}

/**
 * DI instruction.
 */
object Di : Inst {
    override val cycles: Long = 4L
    override val size: UByte = 1u

    override suspend fun Processor.exec() {
        regs.iff1 = false
        regs.iff2 = false
        incPC()
        incCycles()
    }
}

/**
 * DJNZ instruction.
 */
data class Djnz(
    val dst: DestOp8,
    val relj: SrcOp8,
    val jcycles: Long,
    override val cycles: Long,
    override val size: UByte,
) : Inst {
    override suspend fun Processor.exec() {
        val a = load8(dst)
        val c = a.dec()
        store8(dst, c)
        if (c.isZero()) {
            incPC()
            incCycles()
        } else {
            incPC(load8(relj).toByte())
            incCycles(jcycles)
        }
    }
}

/**
 * EI instruction.
 */
object Ei : Inst {
    override val cycles: Long = 4L
    override val size: UByte = 1u

    override suspend fun Processor.exec() {
        intEnabled = true
        incPC()
        incCycles()
    }
}

/**
 * EX instruction
 */
data class Ex(val a: DestOp16, val b: DestOp16, override val cycles: Long, override val size: UByte) : Inst {
    override suspend fun Processor.exec() {
        swap16(a, b)
        incPC()
        incCycles()
    }
}

/**
 * EXX instruction
 */
object Exx : Inst {
    override val cycles: Long = 4L
    override val size: UByte = 1u

    override suspend fun Processor.exec() {
        swap16(Reg16.BC, Reg16.`BC'`)
        swap16(Reg16.DE, Reg16.`DE'`)
        swap16(Reg16.HL, Reg16.`HL'`)
        incPC()
        incCycles()
    }
}

/**
 * HALT instruction
 */
object Halt : Inst {
    override val cycles: Long = 4L
    override val size: UByte = 1u

    override suspend fun Processor.exec() {
        // TODO: HALT can be interrupted
        incCycles()
    }
}

/**
 * IN instruction.
 */
data class In(val dst: DestOp8, val port: SrcOp8, override val cycles: Long, override val size: UByte) : Inst {
    override suspend fun Processor.exec() {
        store8(dst, bus.ioReadByte(load8(port)))
        incPC()
        incCycles()
    }
}

/**
 * INC instruction for 8-bit operands
 */
data class Inc8(val dest: DestOp8, override val cycles: Long, override val size: UByte) : Inst {
    override suspend fun Processor.exec() {
        val a = load8(dest)
        val c = a.inc()
        store8(dest, c)
        apply(PrecomputedFlags.ofInc(a))
        incPC()
        incCycles()
    }
}

/**
 * INC instruction for 16-bit operands
 */
data class Inc16(val dest: DestOp16, override val cycles: Long, override val size: UByte) : Inst {
    override suspend fun Processor.exec() {
        var v = load16(dest)
        v++
        store16(dest, v)
        incPC()
        incCycles()
    }
}

/**
 * JP instruction.
 */
data class Jp(
    val pred: FlagsPredicate,
    val addr: SrcOp16,
    override val cycles: Long,
    override val size: UByte,
) : Inst {
    override suspend fun Processor.exec() {
        regs.pc = if (pred.evaluate(regs.f)) {
            load16(addr)
        } else {
            regs.pc.increment(size)
        }
        incCycles()
    }
}

/**
 * JR instruction
 */
data class Jr(
    val pred: FlagsPredicate,
    val relj: SrcOp8,
    val jcycles: Long,
    override val cycles: Long,
    override val size: UByte,
) : Inst {
    override suspend fun Processor.exec() {
        if (pred.evaluate(regs.f)) {
            incPC(load8(relj).toByte())
            incCycles(jcycles)
        } else {
            incPC()
            incCycles()
        }
    }
}

/**
 * LOAD instruction for 8-bit operands
 */
data class Ld8(val dest: DestOp8, val src: SrcOp8, override val cycles: Long, override val size: UByte): Inst {
    override suspend fun Processor.exec() {
        val v = load8(src)
        store8(dest, v)
        incPC()
        incCycles()
    }
}

/**
 * LOAD instruction for 16-bit operands
 */
data class Ld16(val dest: DestOp16, val src: SrcOp16, override val cycles: Long, override val size: UByte): Inst {
    override suspend fun Processor.exec() {
        val v = load16(src)
        store16(dest, v)
        incPC()
        incCycles()
    }
}

/**
 * NOP instruction
 */
object Nop : Inst {
    override val cycles: Long = 4
    override val size: UByte = 1u
    override suspend fun Processor.exec() {
        incPC()
        incCycles()
    }
}

/**
 * OR instruction for 8-bit operands.
 */
data class Or8(val dst: DestOp8, val src: SrcOp8, override val cycles: Long, override val size: UByte) : Inst {
    override suspend fun Processor.exec() {
        val a = load8(dst)
        val b = load8(src)
        val c = (a or b)
        store8(dst, c)
        apply(PrecomputedFlags.ofOr(c))
        incPC()
        incCycles()
    }
}

/**
 * OUT instruction.
 */
data class Out(val port: SrcOp8, val src: SrcOp8, override val cycles: Long, override val size: UByte) : Inst {
    override suspend fun Processor.exec() {
        bus.ioWriteByte(load8(port), load8(src))
        incPC()
        incCycles()
    }
}

/**
 * POP instruction.
 */
data class Pop(val reg: Reg16, override val cycles: Long, override val size: UByte) : Inst {
    override suspend fun Processor.exec() {
        with(reg) { set(pop()) }
        incPC()
        incCycles()
    }
}

/**
 * PUSH instruction.
 */
data class Push(val reg: Reg16, override val cycles: Long, override val size: UByte) : Inst {
    override suspend fun Processor.exec() {
        push(with(reg) { get() })
        incPC()
        incCycles()
    }
}

/**
 * RET instruction.
 */
data class Ret(
    val pred: FlagsPredicate,
    val jcycles: Long,
    override val cycles: Long,
    override val size: UByte,
) : Inst {
    override suspend fun Processor.exec() {
        if (pred.evaluate(regs.f)) {
            regs.pc = bus.memReadWord(regs.sp)
            regs.sp = regs.sp.increment(2u)
            incCycles(jcycles)
        } else {
            incPC()
            incCycles()
        }
    }
}

/**
 * RLA instruction.
 */
data class Rla(override val cycles: Long, override val size: UByte) : Inst {
    override suspend fun Processor.exec() {
        val (v, carry) = regs.a.rotateLeft(isFlag(Flag.C))
        regs.a = v
        apply(PrecomputedFlags.ofRotateA(v, carry))        
        incPC()
        incCycles()
    }
}

/**
 * RLCA instruction
 */
data class Rlca(override val cycles: Long, override val size: UByte) : Inst {
    override suspend fun Processor.exec() {
        val (v, carry) = regs.a.rotateLeft()
        regs.a = v
        apply(PrecomputedFlags.ofRotateA(v, carry))
        incPC()
        incCycles()
    }
}

/**
 * RRA instruction
 */
data class Rra(override val cycles: Long, override val size: UByte) : Inst {
    override suspend fun Processor.exec() {
        val (v, carry) = regs.a.rotateRight(isFlag(Flag.C))
        regs.a = v
        apply(PrecomputedFlags.ofRotateA(v, carry))        
        incPC()
        incCycles()
    }
}

/**
 * RRCA instruction
 */
data class Rrca(override val cycles: Long, override val size: UByte) : Inst {
    override suspend fun Processor.exec() {
        val (v, carry) = regs.a.rotateRight()
        regs.a = v
        apply(PrecomputedFlags.ofRotateA(v, carry))
        incPC()
        incCycles()
    }
}

/**
 * RST instruction
 */
data class Rst(val addr: UShort) : Inst {
    override val cycles: Long = 11
    override val size: UByte = 1u

    override suspend fun Processor.exec() {
        call(addr)
        incCycles()
    }
}

/**
 * SCF instruction.
 */
data class Scf(override val cycles: Long, override val size: UByte) : Inst {
    override suspend fun Processor.exec() {
        apply(
            (Flag.F3 on regs.a.bit(3)) and
            (Flag.F5 on regs.a.bit(5)) + Flag.C - Flag.N - Flag.H
        )
        incPC()
        incCycles()
    }
}

/**
 * SUB instruction for 8-bit operands.
 */
data class Sub8(
    val dst: DestOp8,
    val src: SrcOp8,
    val withCarry: Boolean,
    override val cycles: Long,
    override val size: UByte,
) : Inst {
    override suspend fun Processor.exec() {
        val a = load8(dst)
        var b = load8(src)
        if (withCarry && Flag.C.isSet(regs.f)) {
            b++
        }
        val c = (a - b).toUByte()
        store8(dst, c)
        apply(PrecomputedFlags.ofSub(a, b))
        incPC()
        incCycles()
    }
}

/**
 * XOR instruction for 8-bit operands.
 */
data class Xor8(val dst: DestOp8, val src: SrcOp8, override val cycles: Long, override val size: UByte) : Inst {
    override suspend fun Processor.exec() {
        val a = load8(dst)
        val b = load8(src)
        val c = (a xor b)
        store8(dst, c)
        apply(PrecomputedFlags.ofXor(c))
        incPC()
        incCycles()
    }
}

/**
 * Illegal pseudo-instruction to refer to an illegal opcode.
 */
object Illegal : Inst {
    override val cycles: Long = 0L
    override val size: UByte = 0u

    override suspend fun Processor.exec() = throw Exception("illegal opcode")
}

package vm8.cpu.z80

object OpCodes {
    const val `NOP`         : Int = 0x00
    const val `LD BC, NN`   : Int = 0x01

    const val `INC B`       : Int = 0x04
    const val `DEC B`       : Int = 0x05

    const val `INC C`       : Int = 0x0C
    const val `DEC C`       : Int = 0x0D    
  
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
        OpCodes.`INC B` -> Inc8(Reg8.B)
        OpCodes.`DEC C` -> Dec8(Reg8.C)
        OpCodes.`JP NN` -> Jp(Imm16)
        else -> Illegal
    }
}

suspend fun Processor.decode(): Inst {
    val opcode: Int = bus.read(regs.pc).toInt()
    return OPCODES_MAIN[opcode]
}

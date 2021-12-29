package vm8.cpu.z80

object OpCodes {
    const val `NOP`         : Int = 0x00
    const val `LD BC, NN`   : Int = 0x01

    const val `INC B`       : Int = 0x04

    const val `DEC C`       : Int = 0x0D

    const val `JP NN`       : Int = 0xC3

    const val `LD DE, NN`   : Int = 0x10
    const val `LD HL, NN`   : Int = 0x20
    const val `LD SP, NN`   : Int = 0x30
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

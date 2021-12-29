package vm8.cpu.z80

const val OPCODE_NOP:       Int = 0x00
const val OPCODE_INC_B:     Int = 0x04
const val OPCODE_DEC_C:     Int = 0x0D
const val OPCODE_JP_NN:     Int = 0xC3

private val OPCODES_MAIN: Array<Inst> = Array(256) {
    when(it) {
        OPCODE_NOP -> Nop
        OPCODE_INC_B -> Inc8(Reg8.B)
        OPCODE_DEC_C -> Dec8(Reg8.C)
        OPCODE_JP_NN -> Jp(Imm16)
        else -> Illegal
    }
}

suspend fun Processor.decode(): Inst {
    val opcode: Int = bus.read(regs.pc).toInt()
    return OPCODES_MAIN[opcode]
}

package vm8.cpu.z80

suspend fun Processor.exec(inst: Inst): Int {
    when (inst) {
        Illegal -> throw Exception("illegal opcode")
        Nop -> {
            regs.pc++
            return 4
        }
        is Inc8 -> {
            unary8(inst.dest) { it.inc() }
            regs.pc++
            return 4
        }
        is Dec8 -> {
            unary8(inst.dest) { it.dec() }
            regs.pc++
            return 4
        }
        is Jp -> {
            regs.pc = load16(inst.addr)
            return 10
        }
        else -> TODO()
    }
}
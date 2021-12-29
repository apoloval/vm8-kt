package vm8.cpu.z80

sealed interface Inst {
    /**
     * Execute the instruction over a [Processor].
     * 
     * @return the number of clock cycles required to execute.
     */
    suspend fun Processor.exec(): Int
}

object Illegal : Inst {
    override suspend fun Processor.exec(): Int = throw Exception("illegal opcode")
}

object Nop : Inst {
    override suspend fun Processor.exec(): Int {
        regs.pc++
        return 4
    }
}

data class Inc8(val dest: DestOp8) : Inst {
    override suspend fun Processor.exec(): Int {
        unary8(dest) { it.inc() }
        regs.pc++
        return 4
    }
}

data class Dec8(val dest: DestOp8) : Inst {
    override suspend fun Processor.exec(): Int {
        unary8(dest) { it.dec() }
        regs.pc++
        return 4
    }
}

data class Jp(val addr: SrcOp16) : Inst {
    override suspend fun Processor.exec(): Int {
        regs.pc = load16(addr)
        return 10
    }
}
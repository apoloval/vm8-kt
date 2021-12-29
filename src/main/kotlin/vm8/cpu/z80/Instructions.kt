package vm8.cpu.z80

/**
 * An instruction that can be executed by a Z80 processor.
 */
sealed interface Inst {
    /**
     * Execute the instruction over a [Processor].
     * 
     * @return the number of clock cycles required to execute.
     */
    suspend fun Processor.exec(): Int
}

/**
 * Illegal pseudo-instruction to refer to an illegal opcode.
 */
object Illegal : Inst {
    override suspend fun Processor.exec(): Int = throw Exception("illegal opcode")
}

/**
 * NOP instruction
 */
object Nop : Inst {
    override suspend fun Processor.exec(): Int {
        regs.pc++
        return 4
    }
}

/**
 * INC instruction for 8-bit operands
 */
data class Inc8(val dest: DestOp8) : Inst {
    override suspend fun Processor.exec(): Int {
        var v = load8(dest)
        v++
        store8(dest, v)
        regs.pc++
        return 4
    }
}

/**
 * DEC instruction for 8-bit operands
 */
data class Dec8(val dest: DestOp8) : Inst {
    override suspend fun Processor.exec(): Int {
        var v = load8(dest)
        v--
        store8(dest, v)
        regs.pc++
        return 4
    }
}

/**
 * JP instruction.
 */
data class Jp(val addr: SrcOp16) : Inst {
    override suspend fun Processor.exec(): Int {
        regs.pc = load16(addr)
        return 10
    }
}
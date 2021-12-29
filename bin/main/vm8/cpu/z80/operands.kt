package vm8.cpu.z80

import vm8.data.*

sealed interface SrcOp8 {
    suspend fun Processor.get(): Octet
}

sealed interface DestOp8 : SrcOp8 {
    suspend fun Processor.set(v: Octet)
}

inline suspend fun Processor.load8(op: SrcOp8): Octet = with(op) { get() }

inline suspend fun Processor.store8(op: DestOp8, v: Octet) { with(op) { set(v) } }

enum class Reg8 : DestOp8 {
    B {
        override suspend fun Processor.get(): Octet = regs.b
        override suspend fun Processor.set(v: Octet) { regs.b = v }
    }, 
    C {
        override suspend fun Processor.get(): Octet = regs.c
        override suspend fun Processor.set(v: Octet) { regs.c = v }
    };    
}

sealed interface SrcOp16 {
    suspend fun Processor.get(): Word
}

sealed interface DestOp16 : SrcOp16 {
    suspend fun Processor.set(v: Word)
}

inline suspend fun Processor.load16(op: SrcOp16): Word = with(op) { get() }

inline suspend fun Processor.store16(op: DestOp16, v: Word) { with(op) { set(v) } }

enum class Reg16 : DestOp16 {
    BC {
        override suspend fun Processor.get(): Word = regs.bc
        override suspend fun Processor.set(v: Word) { regs.bc = v }
    }, 
    PC {
        override suspend fun Processor.get(): Word = regs.pc
        override suspend fun Processor.set(v: Word) { regs.pc = v }
    },
}

object Imm16 : SrcOp16 {
    override suspend fun Processor.get(): Word = bus.readWord(regs.pc.inc())
}

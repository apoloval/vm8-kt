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
    A {
        override suspend fun Processor.get(): Octet = regs.a
        override suspend fun Processor.set(v: Octet) { regs.a = v }
    }, 
    F {
        override suspend fun Processor.get(): Octet = regs.f
        override suspend fun Processor.set(v: Octet) { regs.f = v }
    }, 
    B {
        override suspend fun Processor.get(): Octet = regs.b
        override suspend fun Processor.set(v: Octet) { regs.b = v }
    }, 
    C {
        override suspend fun Processor.get(): Octet = regs.c
        override suspend fun Processor.set(v: Octet) { regs.c = v }
    },
    D {
        override suspend fun Processor.get(): Octet = regs.d
        override suspend fun Processor.set(v: Octet) { regs.d = v }
    },    
    E {
        override suspend fun Processor.get(): Octet = regs.e
        override suspend fun Processor.set(v: Octet) { regs.e = v }
    },    
    H {
        override suspend fun Processor.get(): Octet = regs.h
        override suspend fun Processor.set(v: Octet) { regs.h = v }
    },    
    L {
        override suspend fun Processor.get(): Octet = regs.l
        override suspend fun Processor.set(v: Octet) { regs.l = v }
    },    
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
    AF {
        override suspend fun Processor.get(): Word = regs.af
        override suspend fun Processor.set(v: Word) { regs.af = v }
    }, 
    BC {
        override suspend fun Processor.get(): Word = regs.bc
        override suspend fun Processor.set(v: Word) { regs.bc = v }
    }, 
    DE {
        override suspend fun Processor.get(): Word = regs.de
        override suspend fun Processor.set(v: Word) { regs.de = v }
    }, 
    HL {
        override suspend fun Processor.get(): Word = regs.hl
        override suspend fun Processor.set(v: Word) { regs.hl = v }
    }, 
    PC {
        override suspend fun Processor.get(): Word = regs.pc
        override suspend fun Processor.set(v: Word) { regs.pc = v }
    },
    SP {
        override suspend fun Processor.get(): Word = regs.sp
        override suspend fun Processor.set(v: Word) { regs.sp = v }
    },
}

object Imm16 : SrcOp16 {
    override suspend fun Processor.get(): Word = bus.readWord(regs.pc.inc())
}

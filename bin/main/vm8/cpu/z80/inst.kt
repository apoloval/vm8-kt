package vm8.cpu.z80

sealed interface Inst

object Illegal : Inst
object Nop : Inst
data class Inc8(val dest: DestOp8) : Inst
data class Dec8(val dest: DestOp8) : Inst
data class Jp(val addr: SrcOp16) : Inst

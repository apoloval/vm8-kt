package vm8.cpu.z80

sealed interface SrcOp8
sealed interface DestOp8 : SrcOp8

enum class Reg8 : DestOp8 {
    B, C
}

data class MemOp8(val addr: Addr): DestOp8


sealed interface SrcOp16
sealed interface DestOp16 : SrcOp16

enum class Reg16 : DestOp16 {
    BC, PC,
}

object Imm16 : SrcOp16

data class MemOp16(val addr: Addr): SrcOp16

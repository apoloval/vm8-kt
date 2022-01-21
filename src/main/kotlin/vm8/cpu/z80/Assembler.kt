package vm8.cpu.z80

import vm8.byteorder.ByteOrder

@Suppress("EXPERIMENTAL_IS_NOT_ENABLED", "UNUSED_PARAMETER")
@OptIn(ExperimentalUnsignedTypes::class)
class Assembler(private val buffer: ByteArray, org: UShort = 0u) {
    private var pointer: UShort = org
    private val symbols: MutableMap<String, UShort> = mutableMapOf()
    private val relocations: MutableList<Relocation> = mutableListOf()

    private sealed interface Relocation {
        fun relocate()
    }

    private inner class WordFromSymbol(val addr: UShort, val symbol: String) : Relocation {
        override fun relocate() {
            val word = symbols.getValue(symbol).toUShort()
            val (v0, v1) = ByteOrder.LITTLE_ENDIAN.encode(word)
            buffer[addr.toInt()] = v0.toByte()
            buffer[addr.toInt()+1] = v1.toByte()
        }
    }

    private inner class ByteDistanceToSymbol(val addr: UShort, val from: UShort, val to: String) : Relocation {
        override fun relocate() {
            val toAddr = symbols.getValue(to).toUShort()
            val dist = (toAddr.toInt() - from.toInt()).toByte()
            buffer[addr.toInt()] = dist
        }
    }


    // Register names
    object A
    object B
    object C // can also be a flag condition
    object D
    object E
    object H
    object L
    object AF
    object `AF'`
    object BC { operator fun not() = `(BC)` }
    object DE { operator fun not() = `(DE)` }
    object HL { operator fun not() = `(HL)` }
    object SP { operator fun not() = `(SP)` }

    // Indirect registers
    object `(BC)`
    object `(DE)`
    object `(HL)`
    object `(SP)`

    // Jump conditions
    object NC
    object Z
    object NZ
    object PE
    object PO
    object P
    object M

    data class Indirect<T>(val expr: T)

    operator fun UByte.not() = Indirect(this)
    operator fun UShort.not() = Indirect(this)
    operator fun UInt.not() = this.toUShort().not()

    operator fun String.unaryPlus() = LABEL(this)

    fun relocate() {
        relocations.forEach { it.relocate() }
    }

    fun LABEL(name: String) = symbols.put(name, pointer)

    fun DB(vararg bytes: Int) = DB(*bytes.map { it.toUByte() }.toUByteArray())

    fun DB(vararg bytes: UByte) {
        for (b in bytes) {
            buffer[(pointer++).toInt()] = b.toByte()
        }
    }

    fun DD(to: String, from: UShort = (pointer - 1u).toUShort()) {
        relocations.add(ByteDistanceToSymbol(pointer, from, to))
        DB(0)
    }

    fun DW(vararg bytes: Int) = DW(*bytes.map { it.toUShort() }.toUShortArray())

    fun DW(vararg bytes: UShort) {
        for (b in bytes) {
            val (v0, v1) = ByteOrder.LITTLE_ENDIAN.encode(b)
            buffer[(pointer++).toInt()] = v0.toByte()
            buffer[(pointer++).toInt()] = v1.toByte()
        }
    }

    fun DW(symbol: String) {
        relocations.add(WordFromSymbol(pointer, symbol))
        DW(0)
    }

    fun ADC(dst: A, src: UByte) { DB(OpCodes.`ADC A, N`); DB(src) }
    fun ADC(dst: A, src: A) = DB(OpCodes.`ADC A, A`)
    fun ADC(dst: A, src: B) = DB(OpCodes.`ADC A, B`)
    fun ADC(dst: A, src: C) = DB(OpCodes.`ADC A, C`)
    fun ADC(dst: A, src: D) = DB(OpCodes.`ADC A, D`)
    fun ADC(dst: A, src: E) = DB(OpCodes.`ADC A, E`)
    fun ADC(dst: A, src: H) = DB(OpCodes.`ADC A, H`)
    fun ADC(dst: A, src: L) = DB(OpCodes.`ADC A, L`)
    fun ADC(dst: A, src: `(HL)`) = DB(OpCodes.`ADC A, (HL)`)

    fun ADD(dst: A, src: UByte) { DB(OpCodes.`ADD A, N`); DB(src) }
    fun ADD(dst: A, src: A) = DB(OpCodes.`ADD A, A`)
    fun ADD(dst: A, src: B) = DB(OpCodes.`ADD A, B`)
    fun ADD(dst: A, src: C) = DB(OpCodes.`ADD A, C`)
    fun ADD(dst: A, src: D) = DB(OpCodes.`ADD A, D`)
    fun ADD(dst: A, src: E) = DB(OpCodes.`ADD A, E`)
    fun ADD(dst: A, src: H) = DB(OpCodes.`ADD A, H`)
    fun ADD(dst: A, src: L) = DB(OpCodes.`ADD A, L`)
    fun ADD(dst: A, src: `(HL)`) = DB(OpCodes.`ADD A, (HL)`)

    fun ADD(dst: HL, src: BC) = DB(OpCodes.`ADD HL, BC`)
    fun ADD(dst: HL, src: DE) = DB(OpCodes.`ADD HL, DE`)
    fun ADD(dst: HL, src: HL) = DB(OpCodes.`ADD HL, HL`)
    fun ADD(dst: HL, src: SP) = DB(OpCodes.`ADD HL, SP`)

    fun AND(src: UByte) { DB(OpCodes.`AND N`); DB(src) }
    fun AND(src: A) = DB(OpCodes.`AND A`)
    fun AND(src: B) = DB(OpCodes.`AND B`)
    fun AND(src: C) = DB(OpCodes.`AND C`)
    fun AND(src: D) = DB(OpCodes.`AND D`)
    fun AND(src: E) = DB(OpCodes.`AND E`)
    fun AND(src: H) = DB(OpCodes.`AND H`)
    fun AND(src: L) = DB(OpCodes.`AND L`)
    fun AND(src: `(HL)`) = DB(OpCodes.`AND (HL)`)

    fun CALL(dst: String) { DB(OpCodes.`CALL NN`); DW(dst) }
    fun CALL(dst: UShort) { DB(OpCodes.`CALL NN`); DW(dst) }
    fun CALL(cond: NZ, dst: UShort) { DB(OpCodes.`CALL NZ, NN`); DW(dst) }
    fun CALL(cond: NZ, dst: String) { DB(OpCodes.`CALL NZ, NN`); DW(dst) }
    fun CALL(cond: Z, dst: UShort) { DB(OpCodes.`CALL Z, NN`); DW(dst) }
    fun CALL(cond: Z, dst: String) { DB(OpCodes.`CALL Z, NN`); DW(dst) }
    fun CALL(cond: NC, dst: UShort) { DB(OpCodes.`CALL NC, NN`); DW(dst) }
    fun CALL(cond: NC, dst: String) { DB(OpCodes.`CALL NC, NN`); DW(dst) }
    fun CALL(cond: C, dst: UShort) { DB(OpCodes.`CALL C, NN`); DW(dst) }
    fun CALL(cond: C, dst: String) { DB(OpCodes.`CALL C, NN`); DW(dst) }
    fun CALL(cond: PO, dst: UShort) { DB(OpCodes.`CALL PO, NN`); DW(dst) }
    fun CALL(cond: PO, dst: String) { DB(OpCodes.`CALL PO, NN`); DW(dst) }
    fun CALL(cond: PE, dst: UShort) { DB(OpCodes.`CALL PE, NN`); DW(dst) }
    fun CALL(cond: PE, dst: String) { DB(OpCodes.`CALL PE, NN`); DW(dst) }
    fun CALL(cond: P, dst: UShort) { DB(OpCodes.`CALL P, NN`); DW(dst) }
    fun CALL(cond: P, dst: String) { DB(OpCodes.`CALL P, NN`); DW(dst) }
    fun CALL(cond: M, dst: UShort) { DB(OpCodes.`CALL M, NN`); DW(dst) }
    fun CALL(cond: M, dst: String) { DB(OpCodes.`CALL M, NN`); DW(dst) }

    val CCF: Unit get() = DB(OpCodes.CCF)

    fun CP(src: UByte) { DB(OpCodes.`CP N`); DB(src) }
    fun CP(src: A) = DB(OpCodes.`CP A`)
    fun CP(src: B) = DB(OpCodes.`CP B`)
    fun CP(src: C) = DB(OpCodes.`CP C`)
    fun CP(src: D) = DB(OpCodes.`CP D`)
    fun CP(src: E) = DB(OpCodes.`CP E`)
    fun CP(src: H) = DB(OpCodes.`CP H`)
    fun CP(src: L) = DB(OpCodes.`CP L`)
    fun CP(src: `(HL)`) = DB(OpCodes.`CP (HL)`)

    val CPL: Unit get() = DB(OpCodes.CPL)

    val DAA: Unit get() = DB(OpCodes.DAA)

    fun DEC(dst: A) = DB(OpCodes.`DEC A`)
    fun DEC(dst: B) = DB(OpCodes.`DEC B`)
    fun DEC(dst: C) = DB(OpCodes.`DEC C`)
    fun DEC(dst: D) = DB(OpCodes.`DEC D`)
    fun DEC(dst: E) = DB(OpCodes.`DEC E`)
    fun DEC(dst: H) = DB(OpCodes.`DEC H`)
    fun DEC(dst: L) = DB(OpCodes.`DEC L`)
    fun DEC(dst: `(HL)`) = DB(OpCodes.`DEC (HL)`)

    fun DEC(dst: BC) = DB(OpCodes.`DEC BC`)
    fun DEC(dst: DE) = DB(OpCodes.`DEC DE`)
    fun DEC(dst: HL) = DB(OpCodes.`DEC HL`)
    fun DEC(dst: SP) = DB(OpCodes.`DEC SP`)

    val DI: Unit get() = DB(OpCodes.DI)

    fun DJNZ(n: Int) { DB(OpCodes.`DJNZ N`); DB(n) }

    val EI: Unit get() = DB(OpCodes.EI)

    fun EX(a: AF, b: `AF'`) = DB(OpCodes.`EX AF, AF'`)
    fun EX(a: `(SP)`, b: HL) = DB(OpCodes.`EX (SP), HL`)
    fun EX(a: DE, b: HL) = DB(OpCodes.`EX DE, HL`)

    val EXX: Unit get() = DB(OpCodes.EXX)

    val HALT: Unit get() = DB(OpCodes.HALT)

    fun IN(dst: A, port: Indirect<UByte>) { DB(OpCodes.`IN A, (N)`); DB(port.expr) }

    fun INC(dst: A) = DB(OpCodes.`INC A`)
    fun INC(dst: B) = DB(OpCodes.`INC B`)
    fun INC(dst: C) = DB(OpCodes.`INC C`)
    fun INC(dst: D) = DB(OpCodes.`INC D`)
    fun INC(dst: E) = DB(OpCodes.`INC E`)
    fun INC(dst: H) = DB(OpCodes.`INC H`)
    fun INC(dst: L) = DB(OpCodes.`INC L`)
    fun INC(dst: `(HL)`) = DB(OpCodes.`INC (HL)`)

    fun INC(dst: BC) = DB(OpCodes.`INC BC`)
    fun INC(dst: DE) = DB(OpCodes.`INC DE`)
    fun INC(dst: HL) = DB(OpCodes.`INC HL`)
    fun INC(dst: SP) = DB(OpCodes.`INC SP`)

    fun JP(addr: UShort) { DB(OpCodes.`JP NN`); DW(addr) }
    fun JP(addr: String) { DB(OpCodes.`JP NN`); DW(addr) }
    fun JP(cond: Z, addr: UShort) { DB(OpCodes.`JP Z, NN`); DW(addr) }
    fun JP(cond: Z, addr: String) { DB(OpCodes.`JP Z, NN`); DW(addr) }
    fun JP(cond: NZ, addr: UShort) { DB(OpCodes.`JP NZ, NN`); DW(addr) }
    fun JP(cond: NZ, addr: String) { DB(OpCodes.`JP NZ, NN`); DW(addr) }
    fun JP(cond: C, addr: UShort) { DB(OpCodes.`JP C, NN`); DW(addr) }
    fun JP(cond: C, addr: String) { DB(OpCodes.`JP C, NN`); DW(addr) }
    fun JP(cond: NC, addr: UShort) { DB(OpCodes.`JP NC, NN`); DW(addr) }
    fun JP(cond: NC, addr: String) { DB(OpCodes.`JP NC, NN`); DW(addr) }
    fun JP(cond: PE, addr: UShort) { DB(OpCodes.`JP PE, NN`); DW(addr) }
    fun JP(cond: PE, addr: String) { DB(OpCodes.`JP PE, NN`); DW(addr) }
    fun JP(cond: PO, addr: UShort) { DB(OpCodes.`JP PO, NN`); DW(addr) }
    fun JP(cond: PO, addr: String) { DB(OpCodes.`JP PO, NN`); DW(addr) }
    fun JP(cond: P, addr: UShort) { DB(OpCodes.`JP P, NN`); DW(addr) }
    fun JP(cond: P, addr: String) { DB(OpCodes.`JP P, NN`); DW(addr) }
    fun JP(cond: M, addr: UShort) { DB(OpCodes.`JP M, NN`); DW(addr) }
    fun JP(cond: M, addr: String) { DB(OpCodes.`JP M, NN`); DW(addr) }
    fun JP(addr: `(HL)`) { DB(OpCodes.`JP (HL)`) }

    fun JR(rel: Byte) { DB(OpCodes.`JR N`); DB(rel.toUByte()) }
    fun JR(to: String) { DB(OpCodes.`JR N`); DD(to) }
    fun JR(cond: Z, n: Byte) { DB(OpCodes.`JR Z, N`); DB(n.toUByte()) }
    fun JR(cond: Z, to: String) { DB(OpCodes.`JR Z, N`); DD(to) }
    fun JR(cond: NZ, n: Byte) { DB(OpCodes.`JR NZ, N`); DB(n.toUByte()) }
    fun JR(cond: NZ, to: String) { DB(OpCodes.`JR NZ, N`); DD(to) }
    fun JR(cond: C, n: Byte) { DB(OpCodes.`JR C, N`); DB(n.toUByte()) }
    fun JR(cond: C, to: String) { DB(OpCodes.`JR C, N`); DD(to) }
    fun JR(cond: NC, n: Byte) { DB(OpCodes.`JR NC, N`); DB(n.toUByte()) }
    fun JR(cond: NC, to: String) { DB(OpCodes.`JR NC, N`); DD(to) }

    fun LD(dst: A, src: UByte) { DB(OpCodes.`LD A, N`); DB(src) }
    fun LD(dst: B, src: UByte) { DB(OpCodes.`LD B, N`); DB(src) }
    fun LD(dst: C, src: UByte) { DB(OpCodes.`LD C, N`); DB(src) }
    fun LD(dst: D, src: UByte) { DB(OpCodes.`LD D, N`); DB(src) }
    fun LD(dst: E, src: UByte) { DB(OpCodes.`LD E, N`); DB(src) }
    fun LD(dst: H, src: UByte) { DB(OpCodes.`LD H, N`); DB(src) }
    fun LD(dst: L, src: UByte) { DB(OpCodes.`LD L, N`); DB(src) }
    fun LD(dst: A, src: A) { DB(OpCodes.`LD A, A`) }
    fun LD(dst: A, src: B) { DB(OpCodes.`LD A, B`) }
    fun LD(dst: A, src: C) { DB(OpCodes.`LD A, C`) }
    fun LD(dst: A, src: D) { DB(OpCodes.`LD A, D`) }
    fun LD(dst: A, src: E) { DB(OpCodes.`LD A, E`) }
    fun LD(dst: A, src: H) { DB(OpCodes.`LD A, H`) }
    fun LD(dst: A, src: L) { DB(OpCodes.`LD A, L`) }
    fun LD(dst: A, src: `(HL)`) { DB(OpCodes.`LD A, (HL)`) }
    fun LD(dst: A, src: `(BC)`) { DB(OpCodes.`LD A, (BC)`) }
    fun LD(dst: A, src: `(DE)`) { DB(OpCodes.`LD A, (DE)`) }
    fun LD(dst: A, src: Indirect<UShort>) { DB(OpCodes.`LD A, (NN)`); DW(src.expr) }
    fun LD(dst: B, src: A) { DB(OpCodes.`LD B, A`) }
    fun LD(dst: B, src: B) { DB(OpCodes.`LD B, B`) }
    fun LD(dst: B, src: C) { DB(OpCodes.`LD B, C`) }
    fun LD(dst: B, src: D) { DB(OpCodes.`LD B, D`) }
    fun LD(dst: B, src: E) { DB(OpCodes.`LD B, E`) }
    fun LD(dst: B, src: H) { DB(OpCodes.`LD B, H`) }
    fun LD(dst: B, src: L) { DB(OpCodes.`LD B, L`) }
    fun LD(dst: B, src: `(HL)`) { DB(OpCodes.`LD B, (HL)`) }
    fun LD(dst: C, src: A) { DB(OpCodes.`LD C, A`) }
    fun LD(dst: C, src: B) { DB(OpCodes.`LD C, B`) }
    fun LD(dst: C, src: C) { DB(OpCodes.`LD C, C`) }
    fun LD(dst: C, src: D) { DB(OpCodes.`LD C, D`) }
    fun LD(dst: C, src: E) { DB(OpCodes.`LD C, E`) }
    fun LD(dst: C, src: H) { DB(OpCodes.`LD C, H`) }
    fun LD(dst: C, src: L) { DB(OpCodes.`LD C, L`) }
    fun LD(dst: C, src: `(HL)`) { DB(OpCodes.`LD C, (HL)`) }
    fun LD(dst: D, src: A) { DB(OpCodes.`LD D, A`) }
    fun LD(dst: D, src: B) { DB(OpCodes.`LD D, B`) }
    fun LD(dst: D, src: C) { DB(OpCodes.`LD D, C`) }
    fun LD(dst: D, src: D) { DB(OpCodes.`LD D, D`) }
    fun LD(dst: D, src: E) { DB(OpCodes.`LD D, E`) }
    fun LD(dst: D, src: H) { DB(OpCodes.`LD D, H`) }
    fun LD(dst: D, src: L) { DB(OpCodes.`LD D, L`) }
    fun LD(dst: D, src: `(HL)`) { DB(OpCodes.`LD D, (HL)`) }
    fun LD(dst: E, src: A) { DB(OpCodes.`LD E, A`) }
    fun LD(dst: E, src: B) { DB(OpCodes.`LD E, B`) }
    fun LD(dst: E, src: C) { DB(OpCodes.`LD E, C`) }
    fun LD(dst: E, src: D) { DB(OpCodes.`LD E, D`) }
    fun LD(dst: E, src: E) { DB(OpCodes.`LD E, E`) }
    fun LD(dst: E, src: H) { DB(OpCodes.`LD E, H`) }
    fun LD(dst: E, src: L) { DB(OpCodes.`LD E, L`) }
    fun LD(dst: E, src: `(HL)`) { DB(OpCodes.`LD E, (HL)`) }
    fun LD(dst: H, src: A) { DB(OpCodes.`LD H, A`) }
    fun LD(dst: H, src: B) { DB(OpCodes.`LD H, B`) }
    fun LD(dst: H, src: C) { DB(OpCodes.`LD H, C`) }
    fun LD(dst: H, src: D) { DB(OpCodes.`LD H, D`) }
    fun LD(dst: H, src: E) { DB(OpCodes.`LD H, E`) }
    fun LD(dst: H, src: H) { DB(OpCodes.`LD H, H`) }
    fun LD(dst: H, src: L) { DB(OpCodes.`LD H, L`) }
    fun LD(dst: H, src: `(HL)`) { DB(OpCodes.`LD H, (HL)`) }
    fun LD(dst: L, src: A) { DB(OpCodes.`LD L, A`) }
    fun LD(dst: L, src: B) { DB(OpCodes.`LD L, B`) }
    fun LD(dst: L, src: C) { DB(OpCodes.`LD L, C`) }
    fun LD(dst: L, src: D) { DB(OpCodes.`LD L, D`) }
    fun LD(dst: L, src: E) { DB(OpCodes.`LD L, E`) }
    fun LD(dst: L, src: H) { DB(OpCodes.`LD L, H`) }
    fun LD(dst: L, src: L) { DB(OpCodes.`LD L, L`) }
    fun LD(dst: L, src: `(HL)`) { DB(OpCodes.`LD L, (HL)`) }
    fun LD(dst: `(BC)`, src: A) { DB(OpCodes.`LD (BC), A`) }
    fun LD(dst: `(DE)`, src: A) { DB(OpCodes.`LD (DE), A`) }
    fun LD(dst: `(HL)`, src: UByte) { DB(OpCodes.`LD (HL), N`); DB(src) }
    fun LD(dst: `(HL)`, src: A) { DB(OpCodes.`LD (HL), A`) }
    fun LD(dst: `(HL)`, src: B) { DB(OpCodes.`LD (HL), B`) }
    fun LD(dst: `(HL)`, src: C) { DB(OpCodes.`LD (HL), C`) }
    fun LD(dst: `(HL)`, src: D) { DB(OpCodes.`LD (HL), D`) }
    fun LD(dst: `(HL)`, src: E) { DB(OpCodes.`LD (HL), E`) }
    fun LD(dst: `(HL)`, src: H) { DB(OpCodes.`LD (HL), H`) }
    fun LD(dst: `(HL)`, src: L) { DB(OpCodes.`LD (HL), L`) }

    fun LD(dst: Indirect<UShort>, src: A) { DB(OpCodes.`LD (NN), A`); DW(dst.expr) }
    fun LD(dst: BC, src: UShort) { DB(OpCodes.`LD BC, NN`); DW(src) }
    fun LD(dst: BC, src: String) { DB(OpCodes.`LD BC, NN`); DW(src) }
    fun LD(dst: DE, src: UShort) { DB(OpCodes.`LD DE, NN`); DW(src) }
    fun LD(dst: DE, src: String) { DB(OpCodes.`LD DE, NN`); DW(src) }
    fun LD(dst: HL, src: UShort) { DB(OpCodes.`LD HL, NN`); DW(src) }
    fun LD(dst: HL, src: String) { DB(OpCodes.`LD HL, NN`); DW(src) }
    fun LD(dst: SP, src: UShort) { DB(OpCodes.`LD SP, NN`); DW(src) }
    fun LD(dst: SP, src: String) { DB(OpCodes.`LD SP, NN`); DW(src) }
    fun LD(dst: SP, src: HL) { DB(OpCodes.`LD SP, HL`) }
    fun LD(dst: Indirect<UShort>, src: HL) { DB(OpCodes.`LD (NN), HL`); DW(dst.expr) }
    fun LD(dst: HL, src: Indirect<UShort>) { DB(OpCodes.`LD HL, (NN)`); DW(src.expr) }

    val NOP: Unit get() = DB(OpCodes.NOP)

    fun OR(src: UByte) { DB(OpCodes.`OR N`); DB(src) }
    fun OR(src: A) = DB(OpCodes.`OR A`)
    fun OR(src: B) = DB(OpCodes.`OR B`)
    fun OR(src: C) = DB(OpCodes.`OR C`)
    fun OR(src: D) = DB(OpCodes.`OR D`)
    fun OR(src: E) = DB(OpCodes.`OR E`)
    fun OR(src: H) = DB(OpCodes.`OR H`)
    fun OR(src: L) = DB(OpCodes.`OR L`)
    fun OR(src: `(HL)`) = DB(OpCodes.`OR (HL)`)

    fun OUT(port: Indirect<UByte>, src: A) { DB(OpCodes.`OUT (N), A`); DB(port.expr) }

    fun POP(dst: BC) = DB(OpCodes.`POP BC`)
    fun POP(dst: DE) = DB(OpCodes.`POP DE`)
    fun POP(dst: HL) = DB(OpCodes.`POP HL`)
    fun POP(dst: AF) = DB(OpCodes.`POP AF`)

    fun PUSH(dst: BC) = DB(OpCodes.`PUSH BC`)
    fun PUSH(dst: DE) = DB(OpCodes.`PUSH DE`)
    fun PUSH(dst: HL) = DB(OpCodes.`PUSH HL`)
    fun PUSH(dst: AF) = DB(OpCodes.`PUSH AF`)

    val RET: Unit get() { DB(OpCodes.`RET`) }
    fun RET(cond: NZ) = DB(OpCodes.`RET NZ`)
    fun RET(cond: Z) = DB(OpCodes.`RET Z`)
    fun RET(cond: NC) = DB(OpCodes.`RET NC`)
    fun RET(cond: C) = DB(OpCodes.`RET C`)
    fun RET(cond: PE) = DB(OpCodes.`RET PE`)
    fun RET(cond: PO) = DB(OpCodes.`RET PO`)
    fun RET(cond: P) = DB(OpCodes.`RET P`)
    fun RET(cond: M) = DB(OpCodes.`RET M`)

    val RLA: Unit get() { DB(OpCodes.RLA) }

    val RLCA: Unit get() { DB(OpCodes.RLCA) }

    val RRA: Unit get() { DB(OpCodes.RRA) }

    val RRCA: Unit get() { DB(OpCodes.RRCA) }

    fun RST(v: Int) { when(v) {
        0x00 -> DB(OpCodes.`RST 0x00`)
        0x08 -> DB(OpCodes.`RST 0x08`)
        0x10 -> DB(OpCodes.`RST 0x10`)
        0x18 -> DB(OpCodes.`RST 0x18`)
        0x20 -> DB(OpCodes.`RST 0x20`)
        0x28 -> DB(OpCodes.`RST 0x28`)
        0x30 -> DB(OpCodes.`RST 0x30`)
        0x38 -> DB(OpCodes.`RST 0x38`)
        else -> throw IllegalArgumentException("invalid vector for RST instruction: $v")
    }}

    val SCF: Unit get() { DB(OpCodes.SCF) }

    fun SBC(src: UByte) { DB(OpCodes.`SBC N`); DB(src) }
    fun SBC(src: A) = DB(OpCodes.`SBC A`)
    fun SBC(src: B) = DB(OpCodes.`SBC B`)
    fun SBC(src: C) = DB(OpCodes.`SBC C`)
    fun SBC(src: D) = DB(OpCodes.`SBC D`)
    fun SBC(src: E) = DB(OpCodes.`SBC E`)
    fun SBC(src: H) = DB(OpCodes.`SBC H`)
    fun SBC(src: L) = DB(OpCodes.`SBC L`)
    fun SBC(src: `(HL)`) = DB(OpCodes.`SBC (HL)`)

    fun SUB(src: UByte) { DB(OpCodes.`SUB N`); DB(src) }
    fun SUB(src: A) = DB(OpCodes.`SUB A`)
    fun SUB(src: B) = DB(OpCodes.`SUB B`)
    fun SUB(src: C) = DB(OpCodes.`SUB C`)
    fun SUB(src: D) = DB(OpCodes.`SUB D`)
    fun SUB(src: E) = DB(OpCodes.`SUB E`)
    fun SUB(src: H) = DB(OpCodes.`SUB H`)
    fun SUB(src: L) = DB(OpCodes.`SUB L`)
    fun SUB(src: `(HL)`) = DB(OpCodes.`SUB (HL)`)

    fun XOR(src: UByte) { DB(OpCodes.`XOR N`); DB(src) }
    fun XOR(src: A) = DB(OpCodes.`XOR A`)
    fun XOR(src: B) = DB(OpCodes.`XOR B`)
    fun XOR(src: C) = DB(OpCodes.`XOR C`)
    fun XOR(src: D) = DB(OpCodes.`XOR D`)
    fun XOR(src: E) = DB(OpCodes.`XOR E`)
    fun XOR(src: H) = DB(OpCodes.`XOR H`)
    fun XOR(src: L) = DB(OpCodes.`XOR L`)
    fun XOR(src: `(HL)`) = DB(OpCodes.`XOR (HL)`)
}

fun ByteArray.asm(org: UShort = 0u, f: Assembler.() -> Unit): ByteArray {
    val asm = Assembler(this, org)
    asm.f()
    asm.relocate()
    return this
}

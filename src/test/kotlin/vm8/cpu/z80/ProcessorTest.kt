package vm8.cpu.z80

import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import vm8.cpu.z80.Assembler.*
import vm8.data.*

class ProcessorTest : FunSpec({

    context("General purpose arithmetic and CPU control") {
        test("HALT") { behavesLike { prevFlags ->
            whenProcessorRuns { HALT }
            expect(cycles = 4, pc = 0x0000u, flags = prevFlags)
        }}

        test("NOP") { behavesLike { prevFlags ->
            whenProcessorRuns { NOP }
            expect(cycles = 4, pc = 0x0001u, flags = prevFlags)
        }}

        test("CCF") { behavesLike { a: UByte, prevFlags ->
            given { regs.a = a }
            whenProcessorRuns { CCF }
            expect(cycles = 4, pc = 0x0001u) {
                expectFlags { flag -> when(flag) {
                    Flag.C -> flagNotCopiedFrom(flag, prevFlags)
                    Flag.N -> flagIsReset(flag)
                    Flag.H -> flagCopiedFrom(flag, prevFlags, copiedFromFlag = Flag.C)
                    Flag.F3, Flag.F5 -> flagCopiedFrom(flag, regs.a)
                    else -> flagCopiedFrom(flag, prevFlags)
                }}
            }
        }}

        test("SCF") { behavesLike { a: UByte, prevFlags ->
            given { regs.a = a }
            whenProcessorRuns { SCF }
            expect(cycles = 4, pc = 0x0001u) {
                expectFlags { flag -> when(flag) {
                    Flag.C -> flagIsSet(flag)
                    Flag.N, Flag.H -> flagIsReset(flag)
                    Flag.F3, Flag.F5 -> flagCopiedFrom(flag, regs.a)
                    else -> flagCopiedFrom(flag, prevFlags)
                }}
            }
        }}
    }

    context("Arithmetic and logic") {
        context("ADC 8-bit") {
            data class TestCase(
                val cycles: Int,
                val size: Int,
                val sameOperand: Boolean = false,
                val result: suspend ProcessorBehavior.() -> UByte,
                val prepare: suspend ProcessorBehavior.(UByte, UByte) -> Unit,
            )

            withData(mapOf(
                "ADC A, A" to TestCase(
                    cycles = 4,
                    size = 1,
                    sameOperand = true,
                    result = { regs.a },
                ) { a, _ ->
                    regs.a = a
                    mem.asm { ADC(A, A) }
                },
                "ADC A, B" to TestCase(
                    cycles = 4,
                    size = 1,
                    result = { regs.a },
                ) { a, b ->
                    regs.a = a
                    regs.b = b
                    mem.asm { ADC(A, B) }
                },
                "ADC A, C" to TestCase(
                    cycles = 4,
                    size = 1,
                    result = { regs.a },
                ) { a, b ->
                    regs.a = a
                    regs.c = b
                    mem.asm { ADC(A, C) }
                },
                "ADC A, D" to TestCase(
                    cycles = 4,
                    size = 1,
                    result = { regs.a },
                ) { a, b ->
                    regs.a = a
                    regs.d = b
                    mem.asm { ADC(A, D) }
                },
                "ADC A, E" to TestCase(
                    cycles = 4,
                    size = 1,
                    result = { regs.a },
                ) { a, b ->
                    regs.a = a
                    regs.e = b
                    mem.asm { ADC(A, E) }
                },
                "ADC A, H" to TestCase(
                    cycles = 4,
                    size = 1,
                    result = { regs.a },
                ) { a, b ->
                    regs.a = a
                    regs.h = b
                    mem.asm { ADC(A, H) }
                },
                "ADC A, L" to TestCase(
                    cycles = 4,
                    size = 1,
                    result = { regs.a },
                ) { a, b ->
                    regs.a = a
                    regs.l = b
                    mem.asm { ADC(A, L) }
                },
                "ADC A, (HL)" to TestCase(
                    cycles = 7,
                    size = 1,
                    result = { regs.a },
                ) { a, b ->
                    regs.hl = 0x8000u
                    regs.a = a
                    bus.write(0x8000u, b)
                    mem.asm { ADC(A, !HL) }
                },
            )) { (cycles, size, sameOperand, result, prepare) -> behavesLike { a: UByte, b: UByte, prevFlags ->
                prepare(a, b)
                whenProcessorRuns()
                expect(cycles, pc = size.toUShort()) {
                    var expected = if (sameOperand) { (a + a).toUByte() } else { (a + b).toUByte() }
                    if (Flag.C.isSet(prevFlags)) {
                        expected++
                    }

                    result() shouldBe expected

                    expectFlags { flag -> when(flag) {
                        Flag.C -> flagIsSetOn(flag, carry(a, result()))
                        Flag.N -> flagIsReset(flag)
                        Flag.PV -> flagIsSetOn(flag, overflow(a, result()))
                        Flag.H -> flagIsSetOn(flag, halfCarry(a, result()))
                        Flag.Z -> flagIsSetOn(flag, isZero(result()))
                        Flag.S -> flagIsSetOn(flag, isNegative(result()))
                        Flag.F3, Flag.F5 -> flagCopiedFrom(flag, result())
                    }}
                }
            }}
        }

        context("ADD 8-bit") {
            data class TestCase(
                val cycles: Int,
                val size: Int,
                val sameOperand: Boolean = false,
                val result: suspend ProcessorBehavior.() -> UByte,
                val prepare: suspend ProcessorBehavior.(UByte, UByte) -> Unit,
            )

            withData(mapOf(
                "ADD A, A" to TestCase(
                    cycles = 4,
                    size = 1,
                    sameOperand = true,
                    result = { regs.a },
                ) { a, _ ->
                    regs.a = a
                    mem.asm { ADD(A, A) }
                },
                "ADD A, B" to TestCase(
                    cycles = 4,
                    size = 1,
                    result = { regs.a },
                ) { a, b ->
                    regs.a = a
                    regs.b = b
                    mem.asm { ADD(A, B) }
                },
                "ADD A, C" to TestCase(
                    cycles = 4,
                    size = 1,
                    result = { regs.a },
                ) { a, b ->
                    regs.a = a
                    regs.c = b
                    mem.asm { ADD(A, C) }
                },
                "ADD A, D" to TestCase(
                    cycles = 4,
                    size = 1,
                    result = { regs.a },
                ) { a, b ->
                    regs.a = a
                    regs.d = b
                    mem.asm { ADD(A, D) }
                },
                "ADD A, E" to TestCase(
                    cycles = 4,
                    size = 1,
                    result = { regs.a },
                ) { a, b ->
                    regs.a = a
                    regs.e = b
                    mem.asm { ADD(A, E) }
                },
                "ADD A, H" to TestCase(
                    cycles = 4,
                    size = 1,
                    result = { regs.a },
                ) { a, b ->
                    regs.a = a
                    regs.h = b
                    mem.asm { ADD(A, H) }
                },
                "ADD A, L" to TestCase(
                    cycles = 4,
                    size = 1,
                    result = { regs.a },
                ) { a, b ->
                    regs.a = a
                    regs.l = b
                    mem.asm { ADD(A, L) }
                },
                "ADD A, (HL)" to TestCase(
                    cycles = 7,
                    size = 1,
                    result = { regs.a },
                ) { a, b ->
                    regs.hl = 0x8000u
                    regs.a = a
                    bus.write(0x8000u, b)
                    mem.asm { ADD(A, !HL) }
                },
            )) { (cycles, size, sameOperand, result, prepare) -> behavesLike { a: UByte, b: UByte, _ ->
                prepare(a, b)
                whenProcessorRuns()
                expect(cycles, pc = size.toUShort()) {
                    if (sameOperand) { result() shouldBe (a + a).toUByte() }
                    else { result() shouldBe (a + b).toUByte() }

                    expectFlags { flag -> when(flag) {
                        Flag.C -> flagIsSetOn(flag, carry(a, result()))
                        Flag.N -> flagIsReset(flag)
                        Flag.PV -> flagIsSetOn(flag, overflow(a, result()))
                        Flag.H -> flagIsSetOn(flag, halfCarry(a, result()))
                        Flag.Z -> flagIsSetOn(flag, isZero(result()))
                        Flag.S -> flagIsSetOn(flag, isNegative(result()))
                        Flag.F3, Flag.F5 -> flagCopiedFrom(flag, result())
                    }}
                }
            }}
        }

        context("ADD 16-bit") {
            data class TestCase(
                val cycles: Int,
                val size: Int,
                val sameOperand: Boolean = false,
                val result: ProcessorBehavior.() -> UShort,
                val prepare: ProcessorBehavior.(UShort, UShort) -> Unit,
            )

            withData(mapOf(
                "ADD HL, BC" to TestCase(
                    cycles = 11,
                    size = 1,
                    result = { regs.hl },
                ) { a, b ->
                    regs.hl = a
                    regs.bc = b
                    mem.asm { ADD(HL, BC) }
                },
                "ADD HL, DE" to TestCase(
                    cycles = 11,
                    size = 1,
                    result = { regs.hl },
                ) { a, b ->
                    regs.hl = a
                    regs.de = b
                    mem.asm { ADD(HL, DE) }
                },
                "ADD HL, HL" to TestCase(
                    cycles = 11,
                    size = 1,
                    sameOperand = true,
                    result = { regs.hl },
                ) { a, _ ->
                    regs.hl = a
                    mem.asm { ADD(HL, HL) }
                },
                "ADD HL, SP" to TestCase(
                    cycles = 11,
                    size = 1,
                    result = { regs.hl },
                ) { a, b ->
                    regs.hl = a
                    regs.sp = b
                    mem.asm { ADD(HL, SP) }
                },
            )) { (cycles, size, sameOperand, result, prepare) -> behavesLike { a: UShort, b: UShort, prevFlags ->
                prepare(a, b)
                whenProcessorRuns()
                expect(cycles, pc = size.toUShort()) {
                    if (sameOperand) { result() shouldBe (a + a).toUShort() }
                    else { result() shouldBe (a + b).toUShort() }

                    expectFlags { flag -> when(flag) {
                        Flag.C -> flagIsSetOn(flag, carry(a, result()))
                        Flag.N -> flagIsReset(flag)
                        Flag.F3, Flag.F5 -> flagCopiedFrom(flag, result().high())
                        Flag.H -> flagIsSetOn(flag, halfCarry(a, result()))
                        Flag.V, Flag.Z, Flag.S -> flagCopiedFrom(flag, prevFlags)
                        else -> flagCopiedFrom(flag, prevFlags)
                    }}
                }
            }}
        }

        context("AND 8-bit") {
            data class TestCase(
                val cycles: Int,
                val size: Int,
                val sameOperand: Boolean = false,
                val result: suspend ProcessorBehavior.() -> UByte,
                val prepare: suspend ProcessorBehavior.(UByte, UByte) -> Unit,
            )

            withData(mapOf(
                "AND A" to TestCase(
                    cycles = 4,
                    size = 1,
                    sameOperand = true,
                    result = { regs.a },
                ) { a, _ ->
                    regs.a = a
                    mem.asm { AND(A) }
                },
                "AND B" to TestCase(
                    cycles = 4,
                    size = 1,
                    result = { regs.a },
                ) { a, b ->
                    regs.a = a
                    regs.b = b
                    mem.asm { AND(B) }
                },
                "AND C" to TestCase(
                    cycles = 4,
                    size = 1,
                    result = { regs.a },
                ) { a, b ->
                    regs.a = a
                    regs.c = b
                    mem.asm { AND(C) }
                },
                "AND D" to TestCase(
                    cycles = 4,
                    size = 1,
                    result = { regs.a },
                ) { a, b ->
                    regs.a = a
                    regs.d = b
                    mem.asm { AND(D) }
                },
                "AND E" to TestCase(
                    cycles = 4,
                    size = 1,
                    result = { regs.a },
                ) { a, b ->
                    regs.a = a
                    regs.e = b
                    mem.asm { AND(E) }
                },
                "AND H" to TestCase(
                    cycles = 4,
                    size = 1,
                    result = { regs.a },
                ) { a, b ->
                    regs.a = a
                    regs.h = b
                    mem.asm { AND(H) }
                },
                "AND L" to TestCase(
                    cycles = 4,
                    size = 1,
                    result = { regs.a },
                ) { a, b ->
                    regs.a = a
                    regs.l = b
                    mem.asm { AND(L) }
                },
                "AND (HL)" to TestCase(
                    cycles = 7,
                    size = 1,
                    result = { regs.a },
                ) { a, b ->
                    regs.hl = 0x8000u
                    regs.a = a
                    bus.write(0x8000u, b)
                    mem.asm { AND(!HL) }
                },
            )) { (cycles, size, sameOperand, result, prepare) -> behavesLike { a: UByte, b: UByte, _ ->
                prepare(a, b)
                whenProcessorRuns()
                expect(cycles, pc = size.toUShort()) {
                    if (sameOperand) { result() shouldBe (a and a).toUByte() }
                    else { result() shouldBe (a and b).toUByte() }

                    expectFlags { flag -> when(flag) {
                        Flag.C, Flag.N -> flagIsReset(flag)
                        Flag.PV -> flagIsSetOn(flag, hasEvenParity(result()))
                        Flag.H -> flagIsSet(flag)
                        Flag.Z -> flagIsSetOn(flag, isZero(result()))
                        Flag.S -> flagIsSetOn(flag, isNegative(result()))
                        Flag.F3, Flag.F5 -> flagCopiedFrom(flag, result())
                    }}
                }
            }}
        }

        test("CPL") { behavesLike { a: UByte, prevFlags ->
            given { regs.a = a }
            whenProcessorRuns { CPL }
            expect(cycles = 4, pc = 0x0001u) {
                regs.a shouldBe a.inv()

                expectFlags { flag -> when(flag) {
                    Flag.C, Flag.PV, Flag.Z, Flag.S -> flagCopiedFrom(flag, prevFlags)
                    Flag.N, Flag.H -> flagIsSet(flag)
                    Flag.F3, Flag.F5 -> flagCopiedFrom(flag, regs.a)
                }}
            }
        }}

        context("DAA") {
            data class TestCase(val prepare: ProcessorBehavior.(UByte, UByte) -> Unit)

            withData(mapOf(
                "after ADD" to TestCase { a, b ->
                    regs.a = (a+b).toUByte()
                    cpu.apply(PrecomputedFlags.ofAdd(a, b))
                },
                "after SUB" to TestCase { a, b ->
                    regs.a = (a-b).toUByte()
                    cpu.apply(PrecomputedFlags.ofSub(a, b))
                },
            )) { (prepare) -> behavesLike(Arb.bcd(), Arb.bcd()) { a: UByte, b: UByte, _ ->
                prepare(a, b)
                val value = regs.a
                val prevFlags = regs.f
                whenProcessorRuns { DAA }
                expect(cycles = 4, pc = 0x0001u) {
                    regs.a.low() shouldBeLessThan 10u
                    regs.a.high() shouldBeLessThan 10u

                    expectFlags { flag -> when(flag) {
                        Flag.C -> whenFlagIsSetThen(flag, areNotEqual(value.high(), regs.a.high()))
                        Flag.N -> flagCopiedFrom(flag, prevFlags)
                        Flag.PV -> flagIsSetOn(flag, hasEvenParity(regs.a))
                        Flag.H -> whenFlagIsSetThen(flag, areNotEqual(value.low(), regs.a.low()))
                        Flag.Z -> flagIsSetOn(flag, isZero(regs.a))
                        Flag.S -> flagIsSetOn(flag, isNegative(regs.a))
                        Flag.F3, Flag.F5 -> flagCopiedFrom(flag, regs.a)
                    }}
                }
            }}
        }

        context("DEC 8-bit") {
            data class TestCase(
                val cycles: Int,
                val size: Int,
                val result: suspend ProcessorBehavior.() -> UByte,
                val prepare: suspend ProcessorBehavior.(UByte) -> Unit
            )

            withData(
                mapOf(
                    "DEC A" to TestCase(
                        cycles = 4,
                        size = 1,
                        result = { regs.a },
                    ) {
                        regs.a = it
                        mem.asm { DEC(A) }
                    },
                    "DEC B" to TestCase(
                        cycles = 4,
                        size = 1,
                        result = { regs.b },
                    ) {
                        regs.b = it
                        mem.asm { DEC(B) }
                    },
                    "DEC C" to TestCase(
                        cycles = 4,
                        size = 1,
                        result = { regs.c },
                    ) {
                        regs.c = it
                        mem.asm { DEC(C) }
                    },
                    "DEC D" to TestCase(
                        cycles = 4,
                        size = 1,
                        result = { regs.d },
                    ) {
                        regs.d = it
                        mem.asm { DEC(D) }
                    },
                    "DEC E" to TestCase(
                        cycles = 4,
                        size = 1,
                        result = { regs.e },
                    ) {
                        regs.e = it
                        mem.asm { DEC(E) }
                    },
                    "DEC H" to TestCase(
                        cycles = 4,
                        size = 1,
                        result = { regs.h },
                    ) {
                        regs.h = it
                        mem.asm { DEC(H) }
                    },
                    "DEC L" to TestCase(
                        cycles = 4,
                        size = 1,
                        result = { regs.l },
                    ) {
                        regs.l = it
                        mem.asm { DEC(L) }
                    },
                    "DEC (HL)" to TestCase(
                        cycles = 11,
                        size = 1,
                        result = { bus.read(0x8000u) },
                    ) {
                        regs.hl = 0x8000u
                        bus.write(0x8000u, it)
                        mem.asm { DEC(!HL) }
                    },
                )
            ) { (cycles, size, result, prepare) -> behavesLike { value: UByte, prevFlags ->
                prepare(value)
                whenProcessorRuns()

                expect(cycles, pc = size.toUShort()) {
                    result() shouldBe value.dec()

                    expectFlags { flag -> when(flag) {
                        Flag.C -> flagCopiedFrom(flag, prevFlags)
                        Flag.N -> flagIsSet(flag)
                        Flag.PV -> flagIsSetOn(flag, underflow(value, result()))
                        Flag.H -> flagIsSetOn(flag, halfBorrow(value, result()))
                        Flag.Z -> flagIsSetOn(flag, isZero(result()))
                        Flag.S -> flagIsSetOn(flag, isNegative(result()))
                        Flag.F3, Flag.F5 -> flagCopiedFrom(flag, result())
                    }}
                }
            }}
        }

        context("DEC 16-bit") {
            data class TestCase(
                val cycles: Int,
                val size: Int,
                val result: ProcessorBehavior.() -> UShort,
                val prepare: ProcessorBehavior.(UShort) -> Unit,
            )

            withData(mapOf(
                "DEC BC" to TestCase(
                    cycles = 6,
                    size = 1,
                    result = { regs.bc }
                ) {
                    regs.bc = it
                    mem.asm { DEC(BC) }
                },
                "DEC DE" to TestCase(
                    cycles = 6,
                    size = 1,
                    result = { regs.de }
                ) {
                    regs.de = it
                    mem.asm { DEC(DE) }
                },
                "DEC HL" to TestCase(
                    cycles = 6,
                    size = 1,
                    result = { regs.hl }
                ) {
                    regs.hl = it
                    mem.asm { DEC(HL) }
                },
                "DEC SP" to TestCase(
                    cycles = 6,
                    size = 1,
                    result = { regs.sp }
                ) {
                    regs.sp = it
                    mem.asm { DEC(SP) }
                },
            )) { (cycles, size, result, prepare) -> behavesLike { value: UShort, prevFlags ->
                prepare(value)
                whenProcessorRuns()
                expect(cycles, pc = size.toUShort(), prevFlags) {
                    result() shouldBe value.dec()
                }
            }}
        }

        context("INC 8-bit") {
            data class TestCase(
                val cycles: Int,
                val size: Int,
                val result: suspend ProcessorBehavior.() -> UByte,
                val prepare: suspend ProcessorBehavior.(UByte) -> Unit
            )

            withData(
                mapOf(
                    "INC A" to TestCase(
                        cycles = 4,
                        size = 1,
                        result = { regs.a }
                    ) {
                        regs.a = it
                        mem.asm { INC(A) }
                    },
                    "INC B" to TestCase(
                        cycles = 4,
                        size = 1,
                        result = { regs.b }
                    ) {
                        regs.b = it
                        mem.asm { INC(B) }
                    },
                    "INC C" to TestCase(
                        cycles = 4,
                        size = 1,
                        result = { regs.c }
                    ) {
                        regs.c = it
                        mem.asm { INC(C) }
                    },
                    "INC D" to TestCase(
                        cycles = 4,
                        size = 1,
                        result = { regs.d }
                    ) {
                        regs.d = it
                        mem.asm { INC(D) }
                    },
                    "INC E" to TestCase(
                        cycles = 4,
                        size = 1,
                        result = { regs.e }
                    ) {
                        regs.e = it
                        mem.asm { INC(E) }
                    },
                    "INC H" to TestCase(
                        cycles = 4,
                        size = 1,
                        result = { regs.h }
                    ) {
                        regs.h = it
                        mem.asm { INC(H) }
                    },
                    "INC L" to TestCase(
                        cycles = 4,
                        size = 1,
                        result = { regs.l }
                    ) {
                        regs.l = it
                        mem.asm { INC(L) }
                    },
                    "INC (HL)" to TestCase(
                        cycles = 11,
                        size = 1,
                        result = { bus.read(0x8000u) }
                    ) {
                        bus.write(0x8000u, it)
                        regs.hl = 0x8000u
                        mem.asm { INC(!HL) }
                    },
                )
            ) { (cycles, size, result, prepare) -> behavesLike { value: UByte, prevFlags ->
                prepare(value)
                whenProcessorRuns()

                expect(cycles, pc = size.toUShort()) {
                    result() shouldBe value.inc()

                    expectFlags { flag -> when(flag) {
                        Flag.C -> flagCopiedFrom(flag, prevFlags)
                        Flag.N -> flagIsReset(flag)
                        Flag.PV -> flagIsSetOn(flag, overflow(value, result()))
                        Flag.H -> flagIsSetOn(flag, halfCarry(value, result()))
                        Flag.Z -> flagIsSetOn(flag, isZero(result()))
                        Flag.S -> flagIsSetOn(flag, isNegative(result()))
                        Flag.F3, Flag.F5 -> flagCopiedFrom(flag, result())
                    }}
                }
            }}
        }

        context("INC 16-bit") {
            data class TestCase(
                val cycles: Int,
                val size: Int,
                val result: ProcessorBehavior.() -> UShort,
                val prepare: ProcessorBehavior.(UShort) -> Unit,
            )

            withData(mapOf(
                "INC BC" to TestCase(
                    cycles = 6,
                    size = 1,
                    result = { regs.bc }
                ) {
                    regs.bc = it
                    mem.asm { INC(BC) }
                },
                "INC DE" to TestCase(
                    cycles = 6,
                    size = 1,
                    result = { regs.de }
                ) {
                    regs.de = it
                    mem.asm { INC(DE) }
                },
                "INC HL" to TestCase(
                    cycles = 6,
                    size = 1,
                    result = { regs.hl }
                ) {
                    regs.hl = it
                    mem.asm { INC(HL) }
                },
                "INC SP" to TestCase(
                    cycles = 6,
                    size = 1,
                    result = { regs.sp }
                ) {
                    regs.sp = it
                    mem.asm { INC(SP) }
                },
            )) { (cycles, size, result, prepare) -> behavesLike { value: UShort, prevFlags ->
                prepare(value)
                whenProcessorRuns()
                expect(cycles, pc = size.toUShort(), prevFlags) {
                    result() shouldBe value.inc()
                }
            }}
        }

        context("OR 8-bit") {
            data class TestCase(
                val cycles: Int,
                val size: Int,
                val sameOperand: Boolean = false,
                val result: suspend ProcessorBehavior.() -> UByte,
                val prepare: suspend ProcessorBehavior.(UByte, UByte) -> Unit,
            )

            withData(mapOf(
                "OR A" to TestCase(
                    cycles = 4,
                    size = 1,
                    sameOperand = true,
                    result = { regs.a },
                ) { a, _ ->
                    regs.a = a
                    mem.asm { OR(A) }
                },
                "OR B" to TestCase(
                    cycles = 4,
                    size = 1,
                    result = { regs.a },
                ) { a, b ->
                    regs.a = a
                    regs.b = b
                    mem.asm { OR(B) }
                },
                "OR C" to TestCase(
                    cycles = 4,
                    size = 1,
                    result = { regs.a },
                ) { a, b ->
                    regs.a = a
                    regs.c = b
                    mem.asm { OR(C) }
                },
                "OR D" to TestCase(
                    cycles = 4,
                    size = 1,
                    result = { regs.a },
                ) { a, b ->
                    regs.a = a
                    regs.d = b
                    mem.asm { OR(D) }
                },
                "OR E" to TestCase(
                    cycles = 4,
                    size = 1,
                    result = { regs.a },
                ) { a, b ->
                    regs.a = a
                    regs.e = b
                    mem.asm { OR(E) }
                },
                "OR H" to TestCase(
                    cycles = 4,
                    size = 1,
                    result = { regs.a },
                ) { a, b ->
                    regs.a = a
                    regs.h = b
                    mem.asm { OR(H) }
                },
                "OR L" to TestCase(
                    cycles = 4,
                    size = 1,
                    result = { regs.a },
                ) { a, b ->
                    regs.a = a
                    regs.l = b
                    mem.asm { OR(L) }
                },
                "OR (HL)" to TestCase(
                    cycles = 7,
                    size = 1,
                    result = { regs.a },
                ) { a, b ->
                    regs.hl = 0x8000u
                    regs.a = a
                    bus.write(0x8000u, b)
                    mem.asm { OR(!HL) }
                },
            )) { (cycles, size, sameOperand, result, prepare) -> behavesLike { a: UByte, b: UByte, _ ->
                prepare(a, b)
                whenProcessorRuns()
                expect(cycles, pc = size.toUShort()) {
                    if (sameOperand) { result() shouldBe (a or a) }
                    else { result() shouldBe (a or b) }

                    expectFlags { flag -> when(flag) {
                        Flag.C, Flag.N, Flag.H -> flagIsReset(flag)
                        Flag.PV -> flagIsSetOn(flag, hasEvenParity(result()))
                        Flag.Z -> flagIsSetOn(flag, isZero(result()))
                        Flag.S -> flagIsSetOn(flag, isNegative(result()))
                        Flag.F3, Flag.F5 -> flagCopiedFrom(flag, result())
                    }}
                }
            }}
        }

        context("SBC 8-bit") {
            data class TestCase(
                val cycles: Int,
                val size: Int,
                val sameOperand: Boolean = false,
                val result: suspend ProcessorBehavior.() -> UByte,
                val prepare: suspend ProcessorBehavior.(UByte, UByte) -> Unit,
            )

            withData(mapOf(
                "SBC A" to TestCase(
                    cycles = 4,
                    size = 1,
                    sameOperand = true,
                    result = { regs.a },
                ) { a, _ ->
                    regs.a = a
                    mem.asm { SBC(A) }
                },
                "SBC B" to TestCase(
                    cycles = 4,
                    size = 1,
                    result = { regs.a },
                ) { a, b ->
                    regs.a = a
                    regs.b = b
                    mem.asm { SBC(B) }
                },
                "SBC C" to TestCase(
                    cycles = 4,
                    size = 1,
                    result = { regs.a },
                ) { a, b ->
                    regs.a = a
                    regs.c = b
                    mem.asm { SBC(C) }
                },
                "SBC D" to TestCase(
                    cycles = 4,
                    size = 1,
                    result = { regs.a },
                ) { a, b ->
                    regs.a = a
                    regs.d = b
                    mem.asm { SBC(D) }
                },
                "SBC E" to TestCase(
                    cycles = 4,
                    size = 1,
                    result = { regs.a },
                ) { a, b ->
                    regs.a = a
                    regs.e = b
                    mem.asm { SBC(E) }
                },
                "SBC H" to TestCase(
                    cycles = 4,
                    size = 1,
                    result = { regs.a },
                ) { a, b ->
                    regs.a = a
                    regs.h = b
                    mem.asm { SBC(H) }
                },
                "SBC L" to TestCase(
                    cycles = 4,
                    size = 1,
                    result = { regs.a },
                ) { a, b ->
                    regs.a = a
                    regs.l = b
                    mem.asm { SBC(L) }
                },
                "SBC (HL)" to TestCase(
                    cycles = 7,
                    size = 1,
                    result = { regs.a },
                ) { a, b ->
                    regs.hl = 0x8000u
                    regs.a = a
                    bus.write(0x8000u, b)
                    mem.asm { SBC(!HL) }
                },
            )) { (cycles, size, sameOperand, result, prepare) -> behavesLike { a: UByte, b: UByte, prevFlags ->
                prepare(a, b)
                whenProcessorRuns()
                expect(cycles, pc = size.toUShort()) {
                    var expected = if (sameOperand) { (a - a).toUByte() } else { (a - b).toUByte() }
                    if (Flag.C.isSet(prevFlags)) {
                        expected--
                    }

                    result() shouldBe expected

                    expectFlags { flag -> when(flag) {
                        Flag.C -> flagIsSetOn(flag, borrow(a, result()))
                        Flag.N -> flagIsSet(flag)
                        Flag.PV -> flagIsSetOn(flag, underflow(a, result()))
                        Flag.H -> flagIsSetOn(flag, halfBorrow(a, result()))
                        Flag.Z -> flagIsSetOn(flag, isZero(result()))
                        Flag.S -> flagIsSetOn(flag, isNegative(result()))
                        Flag.F3, Flag.F5 -> flagCopiedFrom(flag, result())
                    }}
                }
            }}
        }

        context("SUB 8-bit") {
            data class TestCase(
                val cycles: Int,
                val size: Int,
                val sameOperand: Boolean = false,
                val result: suspend ProcessorBehavior.() -> UByte,
                val prepare: suspend ProcessorBehavior.(UByte, UByte) -> Unit,
            )

            withData(mapOf(
                "SUB A" to TestCase(
                    cycles = 4,
                    size = 1,
                    sameOperand = true,
                    result = { regs.a },
                ) { a, _ ->
                    regs.a = a
                    mem.asm { SUB(A) }
                },
                "SUB B" to TestCase(
                    cycles = 4,
                    size = 1,
                    result = { regs.a },
                ) { a, b ->
                    regs.a = a
                    regs.b = b
                    mem.asm { SUB(B) }
                },
                "SUB C" to TestCase(
                    cycles = 4,
                    size = 1,
                    result = { regs.a },
                ) { a, b ->
                    regs.a = a
                    regs.c = b
                    mem.asm { SUB(C) }
                },
                "SUB D" to TestCase(
                    cycles = 4,
                    size = 1,
                    result = { regs.a },
                ) { a, b ->
                    regs.a = a
                    regs.d = b
                    mem.asm { SUB(D) }
                },
                "SUB E" to TestCase(
                    cycles = 4,
                    size = 1,
                    result = { regs.a },
                ) { a, b ->
                    regs.a = a
                    regs.e = b
                    mem.asm { SUB(E) }
                },
                "SUB H" to TestCase(
                    cycles = 4,
                    size = 1,
                    result = { regs.a },
                ) { a, b ->
                    regs.a = a
                    regs.h = b
                    mem.asm { SUB(H) }
                },
                "SUB L" to TestCase(
                    cycles = 4,
                    size = 1,
                    result = { regs.a },
                ) { a, b ->
                    regs.a = a
                    regs.l = b
                    mem.asm { SUB(L) }
                },
                "SUB (HL)" to TestCase(
                    cycles = 7,
                    size = 1,
                    result = { regs.a },
                ) { a, b ->
                    regs.hl = 0x8000u
                    regs.a = a
                    bus.write(0x8000u, b)
                    mem.asm { SUB(!HL) }
                },
            )) { (cycles, size, sameOperand, result, prepare) -> behavesLike { a: UByte, b: UByte, _ ->
                prepare(a, b)
                whenProcessorRuns()
                expect(cycles, pc = size.toUShort()) {
                    if (sameOperand) { result() shouldBe (a - a).toUByte() }
                    else { result() shouldBe (a - b).toUByte() }

                    expectFlags { flag -> when(flag) {
                        Flag.C -> flagIsSetOn(flag, borrow(a, result()))
                        Flag.N -> flagIsSet(flag)
                        Flag.PV -> flagIsSetOn(flag, underflow(a, result()))
                        Flag.H -> flagIsSetOn(flag, halfBorrow(a, result()))
                        Flag.Z -> flagIsSetOn(flag, isZero(result()))
                        Flag.S -> flagIsSetOn(flag, isNegative(result()))
                        Flag.F3, Flag.F5 -> flagCopiedFrom(flag, result())
                    }}
                }
            }}
        }

        context("XOR 8-bit") {
            data class TestCase(
                val cycles: Int,
                val size: Int,
                val sameOperand: Boolean = false,
                val result: suspend ProcessorBehavior.() -> UByte,
                val prepare: suspend ProcessorBehavior.(UByte, UByte) -> Unit,
            )

            withData(mapOf(
                "XOR A" to TestCase(
                    cycles = 4,
                    size = 1,
                    sameOperand = true,
                    result = { regs.a },
                ) { a, _ ->
                    regs.a = a
                    mem.asm { XOR(A) }
                },
                "XOR B" to TestCase(
                    cycles = 4,
                    size = 1,
                    result = { regs.a },
                ) { a, b ->
                    regs.a = a
                    regs.b = b
                    mem.asm { XOR(B) }
                },
                "XOR C" to TestCase(
                    cycles = 4,
                    size = 1,
                    result = { regs.a },
                ) { a, b ->
                    regs.a = a
                    regs.c = b
                    mem.asm { XOR(C) }
                },
                "XOR D" to TestCase(
                    cycles = 4,
                    size = 1,
                    result = { regs.a },
                ) { a, b ->
                    regs.a = a
                    regs.d = b
                    mem.asm { XOR(D) }
                },
                "XOR E" to TestCase(
                    cycles = 4,
                    size = 1,
                    result = { regs.a },
                ) { a, b ->
                    regs.a = a
                    regs.e = b
                    mem.asm { XOR(E) }
                },
                "XOR H" to TestCase(
                    cycles = 4,
                    size = 1,
                    result = { regs.a },
                ) { a, b ->
                    regs.a = a
                    regs.h = b
                    mem.asm { XOR(H) }
                },
                "XOR L" to TestCase(
                    cycles = 4,
                    size = 1,
                    result = { regs.a },
                ) { a, b ->
                    regs.a = a
                    regs.l = b
                    mem.asm { XOR(L) }
                },
                "XOR (HL)" to TestCase(
                    cycles = 7,
                    size = 1,
                    result = { regs.a },
                ) { a, b ->
                    regs.hl = 0x8000u
                    regs.a = a
                    bus.write(0x8000u, b)
                    mem.asm { XOR(!HL) }
                },
            )) { (cycles, size, sameOperand, result, prepare) -> behavesLike { a: UByte, b: UByte, _ ->
                prepare(a, b)
                whenProcessorRuns()
                expect(cycles, pc = size.toUShort()) {
                    if (sameOperand) { result() shouldBe (a xor a).toUByte() }
                    else { result() shouldBe (a xor b).toUByte() }

                    expectFlags { flag -> when(flag) {
                        Flag.C, Flag.N, Flag.H -> flagIsReset(flag)
                        Flag.PV -> flagIsSetOn(flag, hasEvenParity(result()))
                        Flag.Z -> flagIsSetOn(flag, isZero(result()))
                        Flag.S -> flagIsSetOn(flag, isNegative(result()))
                        Flag.F3, Flag.F5 -> flagCopiedFrom(flag, result())
                    }}
                }
            }}
        }
    }

    context("Rotate and shift") {
        test("RLA") { behavesLike { value: UByte, prevFlags ->
            val inCarry = Flag.C.isSet(prevFlags)
            given { regs.a = value }
            whenProcessorRuns { RLA }
            val (xval, outCarry) = value.rotateLeft(inCarry)
            expectRotate(xval, outCarry, prevFlags)
        }}

        test("RLCA") { behavesLike { value: UByte, prevFlags ->
            given { regs.a = value }
            whenProcessorRuns { RLCA }
            val (xval, carry) = value.rotateLeft()
            expectRotate(xval, carry, prevFlags)
        }}

        test("RRA") { behavesLike { value: UByte, prevFlags ->
            val inCarry = Flag.C.isSet(prevFlags)
            given { regs.a = value }
            whenProcessorRuns { RRA }
            val (xval, outCarry) = value.rotateRight(inCarry)
            expectRotate(xval, outCarry, prevFlags)
        }}

        test("RRCA") { behavesLike { value: UByte, prevFlags ->
            given { regs.a = value }
            whenProcessorRuns { RRCA }
            val (xval, carry) = value.rotateRight()
            expectRotate(xval, carry, prevFlags)
        }}
    }

    context("Jump, call and return") {
        context("Relative jump") {
            data class TestCase(
                val cond: ProcessorBehavior.() -> Boolean,
                val prepare: ProcessorBehavior.(Byte) -> Unit,
            )

            withData(mapOf(
                "JR N" to TestCase(cond = { true }) {
                    mem.asm { JR(it)}
                },
                "JR N, N" to TestCase(cond = { regs.f.bit(6) }) {
                    mem.asm { JR(Z, it)}
                },
                "JR NZ, N" to TestCase(cond = { !regs.f.bit(6) }) {
                    mem.asm { JR(NZ, it)}
                },
                "JR C, N" to TestCase(cond = { regs.f.bit(0) }) {
                    mem.asm { JR(C, it)}
                },
                "JR NC, N" to TestCase(cond = { !regs.f.bit(0) }) {
                    mem.asm { JR(NC, it)}
                },
            )) { (cond, prepare) -> behavesLike { n: Byte, prevFlags ->
                prepare(n)
                whenProcessorRuns()
                if (cond()) {
                    expect(cycles = 12, pc = 0x0000.toUShort().increment(n), prevFlags)
                } else {
                    expect(cycles = 7, pc = 0x0002u, prevFlags)
                }
            }}
        }

        test("DJNZ") { behavesLike { value: UByte, prevFlags ->
            given { regs.b = value }
            whenProcessorRuns { DJNZ(0x42) }
            if (value == 1u.toUByte()) {
                expect(cycles = 8, pc = 0x0002u, prevFlags) {
                    regs.b shouldBe 0x00u
                }
            } else {
                expect(cycles = 13, pc = 0x0042u, prevFlags) {
                    regs.b shouldBe value.dec()
                }
            }
        }}
    }

    context("Load and exchange") {
        context("LD 8-bit") {
            data class TestCase(
                val cycles: Int,
                val size: Int,
                val result: suspend ProcessorBehavior.(UByte) -> UByte,
                val prepare: suspend ProcessorBehavior.(UByte) -> Unit,
            )

            withData(
                mapOf(
                    "LD A, N" to TestCase(
                        cycles = 7,
                        size = 2,
                        result = { regs.a })
                    {
                        mem.asm { LD(A, it) }
                    },
                    "LD B, N" to TestCase(
                        cycles = 7,
                        size = 2,
                        result = { regs.b })
                    {
                        mem.asm { LD(B, it) }
                    },
                    "LD C, N" to TestCase(
                        cycles = 7,
                        size = 2,
                        result = { regs.c })
                    {
                        mem.asm { LD(C, it) }
                    },
                    "LD D, N" to TestCase(
                        cycles = 7,
                        size = 2,
                        result = { regs.d })
                    {
                        mem.asm { LD(D, it) }
                    },
                    "LD E, N" to TestCase(
                        cycles = 7,
                        size = 2,
                        result = { regs.e })
                    {
                        mem.asm { LD(E, it) }
                    },
                    "LD H, N" to TestCase(
                        cycles = 7,
                        size = 2,
                        result = { regs.h })
                    {
                        mem.asm { LD(H, it) }
                    },
                    "LD L, N" to TestCase(
                        cycles = 7,
                        size = 2,
                        result = { regs.l })
                    {
                        mem.asm { LD(L, it) }
                    },
                    "LD A, A" to TestCase(
                        cycles = 4,
                        size = 1,
                        result = { regs.a })
                    {
                        regs.a = it
                        mem.asm { LD(A, A) }
                    },
                    "LD A, B" to TestCase(
                        cycles = 4,
                        size = 1,
                        result = { regs.a })
                    {
                        regs.b = it
                        mem.asm { LD(A, B) }
                    },
                    "LD A, C" to TestCase(
                        cycles = 4,
                        size = 1,
                        result = { regs.a })
                    {
                        regs.c = it
                        mem.asm { LD(A, C) }
                    },
                    "LD A, D" to TestCase(
                        cycles = 4,
                        size = 1,
                        result = { regs.a })
                    {
                        regs.d = it
                        mem.asm { LD(A, D) }
                    },
                    "LD A, E" to TestCase(
                        cycles = 4,
                        size = 1,
                        result = { regs.a })
                    {
                        regs.e = it
                        mem.asm { LD(A, E) }
                    },
                    "LD A, H" to TestCase(
                        cycles = 4,
                        size = 1,
                        result = { regs.a })
                    {
                        regs.h = it
                        mem.asm { LD(A, H) }
                    },
                    "LD A, L" to TestCase(
                        cycles = 4,
                        size = 1,
                        result = { regs.a })
                    {
                        regs.l = it
                        mem.asm { LD(A, L) }
                    },
                    "LD A, (HL)" to TestCase(
                        cycles = 7,
                        size = 1,
                        result = { regs.a })
                    {
                        regs.hl = 0x8000u
                        bus.write(0x8000u, it)
                        mem.asm { LD(A, !HL) }
                    },
                    "LD B, A" to TestCase(
                        cycles = 4,
                        size = 1,
                        result = { regs.b })
                    {
                        regs.a = it
                        mem.asm { LD(B, A) }
                    },
                    "LD B, B" to TestCase(
                        cycles = 4,
                        size = 1,
                        result = { regs.b })
                    {
                        regs.b = it
                        mem.asm { LD(B, B) }
                    },
                    "LD B, C" to TestCase(
                        cycles = 4,
                        size = 1,
                        result = { regs.b })
                    {
                        regs.c = it
                        mem.asm { LD(B, C) }
                    },
                    "LD B, D" to TestCase(
                        cycles = 4,
                        size = 1,
                        result = { regs.b })
                    {
                        regs.d = it
                        mem.asm { LD(B, D) }
                    },
                    "LD B, E" to TestCase(
                        cycles = 4,
                        size = 1,
                        result = { regs.b })
                    {
                        regs.e = it
                        mem.asm { LD(B, E) }
                    },
                    "LD B, H" to TestCase(
                        cycles = 4,
                        size = 1,
                        result = { regs.b })
                    {
                        regs.h = it
                        mem.asm { LD(B, H) }
                    },
                    "LD B, L" to TestCase(
                        cycles = 4,
                        size = 1,
                        result = { regs.b })
                    {
                        regs.l = it
                        mem.asm { LD(B, L) }
                    },
                    "LD B, (HL)" to TestCase(
                        cycles = 7,
                        size = 1,
                        result = { regs.b })
                    {
                        regs.hl = 0x8000u
                        bus.write(0x8000u, it)
                        mem.asm { LD(B, !HL) }
                    },
                    "LD C, A" to TestCase(
                        cycles = 4,
                        size = 1,
                        result = { regs.c })
                    {
                        regs.a = it
                        mem.asm { LD(C, A) }
                    },
                    "LD C, B" to TestCase(
                        cycles = 4,
                        size = 1,
                        result = { regs.c })
                    {
                        regs.b = it
                        mem.asm { LD(C, B) }
                    },
                    "LD C, C" to TestCase(
                        cycles = 4,
                        size = 1,
                        result = { regs.c })
                    {
                        regs.c = it
                        mem.asm { LD(C, C) }
                    },
                    "LD C, D" to TestCase(
                        cycles = 4,
                        size = 1,
                        result = { regs.c })
                    {
                        regs.d = it
                        mem.asm { LD(C, D) }
                    },
                    "LD C, E" to TestCase(
                        cycles = 4,
                        size = 1,
                        result = { regs.c })
                    {
                        regs.e = it
                        mem.asm { LD(C, E) }
                    },
                    "LD C, H" to TestCase(
                        cycles = 4,
                        size = 1,
                        result = { regs.c })
                    {
                        regs.h = it
                        mem.asm { LD(C, H) }
                    },
                    "LD C, L" to TestCase(
                        cycles = 4,
                        size = 1,
                        result = { regs.c })
                    {
                        regs.l = it
                        mem.asm { LD(C, L) }
                    },
                    "LD C, (HL)" to TestCase(
                        cycles = 7,
                        size = 1,
                        result = { regs.c })
                    {
                        regs.hl = 0x8000u
                        bus.write(0x8000u, it)
                        mem.asm { LD(C, !HL) }
                    },
                    "LD D, A" to TestCase(
                        cycles = 4,
                        size = 1,
                        result = { regs.d })
                    {
                        regs.a = it
                        mem.asm { LD(D, A) }
                    },
                    "LD D, B" to TestCase(
                        cycles = 4,
                        size = 1,
                        result = { regs.d })
                    {
                        regs.b = it
                        mem.asm { LD(D, B) }
                    },
                    "LD D, C" to TestCase(
                        cycles = 4,
                        size = 1,
                        result = { regs.d })
                    {
                        regs.c = it
                        mem.asm { LD(D, C) }
                    },
                    "LD D, D" to TestCase(
                        cycles = 4,
                        size = 1,
                        result = { regs.d })
                    {
                        regs.d = it
                        mem.asm { LD(D, D) }
                    },
                    "LD D, E" to TestCase(
                        cycles = 4,
                        size = 1,
                        result = { regs.d })
                    {
                        regs.e = it
                        mem.asm { LD(D, E) }
                    },
                    "LD D, H" to TestCase(
                        cycles = 4,
                        size = 1,
                        result = { regs.d })
                    {
                        regs.h = it
                        mem.asm { LD(D, H) }
                    },
                    "LD D, L" to TestCase(
                        cycles = 4,
                        size = 1,
                        result = { regs.d })
                    {
                        regs.l = it
                        mem.asm { LD(D, L) }
                    },
                    "LD D, (HL)" to TestCase(
                        cycles = 7,
                        size = 1,
                        result = { regs.d })
                    {
                        regs.hl = 0x8000u
                        bus.write(0x8000u, it)
                        mem.asm { LD(D, !HL) }
                    },
                    "LD E, A" to TestCase(
                        cycles = 4,
                        size = 1,
                        result = { regs.e })
                    {
                        regs.a = it
                        mem.asm { LD(E, A) }
                    },
                    "LD E, B" to TestCase(
                        cycles = 4,
                        size = 1,
                        result = { regs.e })
                    {
                        regs.b = it
                        mem.asm { LD(E, B) }
                    },
                    "LD E, C" to TestCase(
                        cycles = 4,
                        size = 1,
                        result = { regs.e })
                    {
                        regs.c = it
                        mem.asm { LD(E, C) }
                    },
                    "LD E, D" to TestCase(
                        cycles = 4,
                        size = 1,
                        result = { regs.e })
                    {
                        regs.d = it
                        mem.asm { LD(E, D) }
                    },
                    "LD E, E" to TestCase(
                        cycles = 4,
                        size = 1,
                        result = { regs.e })
                    {
                        regs.e = it
                        mem.asm { LD(E, E) }
                    },
                    "LD E, H" to TestCase(
                        cycles = 4,
                        size = 1,
                        result = { regs.e })
                    {
                        regs.h = it
                        mem.asm { LD(E, H) }
                    },
                    "LD E, L" to TestCase(
                        cycles = 4,
                        size = 1,
                        result = { regs.e })
                    {
                        regs.l = it
                        mem.asm { LD(E, L) }
                    },
                    "LD E, (HL)" to TestCase(
                        cycles = 7,
                        size = 1,
                        result = { regs.e })
                    {
                        regs.hl = 0x8000u
                        bus.write(0x8000u, it)
                        mem.asm { LD(E, !HL) }
                    },
                    "LD H, A" to TestCase(
                        cycles = 4,
                        size = 1,
                        result = { regs.h })
                    {
                        regs.a = it
                        mem.asm { LD(H, A) }
                    },
                    "LD H, B" to TestCase(
                        cycles = 4,
                        size = 1,
                        result = { regs.h })
                    {
                        regs.b = it
                        mem.asm { LD(H, B) }
                    },
                    "LD H, C" to TestCase(
                        cycles = 4,
                        size = 1,
                        result = { regs.h })
                    {
                        regs.c = it
                        mem.asm { LD(H, C) }
                    },
                    "LD H, D" to TestCase(
                        cycles = 4,
                        size = 1,
                        result = { regs.h })
                    {
                        regs.d = it
                        mem.asm { LD(H, D) }
                    },
                    "LD H, E" to TestCase(
                        cycles = 4,
                        size = 1,
                        result = { regs.h })
                    {
                        regs.e = it
                        mem.asm { LD(H, E) }
                    },
                    "LD H, H" to TestCase(
                        cycles = 4,
                        size = 1,
                        result = { regs.h })
                    {
                        regs.h = it
                        mem.asm { LD(H, H) }
                    },
                    "LD H, L" to TestCase(
                        cycles = 4,
                        size = 1,
                        result = { regs.h })
                    {
                        regs.l = it
                        mem.asm { LD(H, L) }
                    },
                    "LD H, (HL)" to TestCase(
                        cycles = 7,
                        size = 1,
                        result = { regs.h })
                    {
                        regs.hl = 0x8000u
                        bus.write(0x8000u, it)
                        mem.asm { LD(H, !HL) }
                    },
                    "LD L, A" to TestCase(
                        cycles = 4,
                        size = 1,
                        result = { regs.l })
                    {
                        regs.a = it
                        mem.asm { LD(L, A) }
                    },
                    "LD L, B" to TestCase(
                        cycles = 4,
                        size = 1,
                        result = { regs.l })
                    {
                        regs.b = it
                        mem.asm { LD(L, B) }
                    },
                    "LD L, C" to TestCase(
                        cycles = 4,
                        size = 1,
                        result = { regs.l })
                    {
                        regs.c = it
                        mem.asm { LD(L, C) }
                    },
                    "LD L, D" to TestCase(
                        cycles = 4,
                        size = 1,
                        result = { regs.l })
                    {
                        regs.d = it
                        mem.asm { LD(L, D) }
                    },
                    "LD L, E" to TestCase(
                        cycles = 4,
                        size = 1,
                        result = { regs.l })
                    {
                        regs.e = it
                        mem.asm { LD(L, E) }
                    },
                    "LD L, H" to TestCase(
                        cycles = 4,
                        size = 1,
                        result = { regs.l })
                    {
                        regs.h = it
                        mem.asm { LD(L, H) }
                    },
                    "LD L, L" to TestCase(
                        cycles = 4,
                        size = 1,
                        result = { regs.l })
                    {
                        regs.l = it
                        mem.asm { LD(L, L) }
                    },
                    "LD L, (HL)" to TestCase(
                        cycles = 7,
                        size = 1,
                        result = { regs.l })
                    {
                        regs.hl = 0x8000u
                        bus.write(0x8000u, it)
                        mem.asm { LD(L, !HL) }
                    },
                    "LD (BC), A" to TestCase(
                        cycles = 7,
                        size = 1,
                        result = { bus.read(0x8000u) }
                    ) {
                        regs.a = it
                        regs.bc = 0x8000u
                        mem.asm { LD(!BC, A) }
                    },
                    "LD (DE), A" to TestCase(
                        cycles = 7,
                        size = 1,
                        result = { bus.read(0x8000u) }
                    ) {
                        regs.a = it
                        regs.de = 0x8000u
                        mem.asm { LD(!DE, A) }
                    },
                    "LD A, (BC)" to TestCase(
                        cycles = 7,
                        size = 1,
                        result = { regs.a },
                    ) {
                        regs.bc = 0x8000u
                        mem[0x8000] = it.toByte()
                        mem.asm { LD(A, !BC) }
                    },
                    "LD A, (DE)" to TestCase(
                        cycles = 7,
                        size = 1,
                        result = { regs.a },
                    ) {
                        regs.de = 0x8000u
                        mem[0x8000] = it.toByte()
                        mem.asm { LD(A, !DE) }
                    },
                    "LD A, (NN)" to TestCase(
                        cycles = 13,
                        size = 3,
                        result = { regs.a },
                    ) {
                        mem[0x8000] = it.toByte()
                        mem.asm { LD(A, !0x8000u) }
                    },
                    "LD (NN), A" to TestCase(
                        cycles = 13,
                        size = 3,
                        result = { bus.read(0x8000u) },
                    ) {
                        regs.a = it
                        mem.asm { LD(!0x8000u, A) }
                    },
                    "LD (HL), N" to TestCase(
                        cycles = 10,
                        size = 2,
                        result = { bus.read(0x8000u) },
                    ) {
                        regs.hl = 0x8000u
                        mem.asm { LD(!HL, it) }
                    },
                    "LD (HL), A" to TestCase(
                        cycles = 7,
                        size = 1,
                        result = { bus.read(0x8000u) })
                    {
                        regs.a = it
                        regs.hl = 0x8000u
                        mem.asm { LD(!HL, A) }
                    },
                    "LD (HL), B" to TestCase(
                        cycles = 7,
                        size = 1,
                        result = { bus.read(0x8000u) })
                    {
                        regs.b = it
                        regs.hl = 0x8000u
                        mem.asm { LD(!HL, B) }
                    },
                    "LD (HL), C" to TestCase(
                        cycles = 7,
                        size = 1,
                        result = { bus.read(0x8000u) })
                    {
                        regs.c = it
                        regs.hl = 0x8000u
                        mem.asm { LD(!HL, C) }
                    },
                    "LD (HL), D" to TestCase(
                        cycles = 7,
                        size = 1,
                        result = { bus.read(0x8000u) })
                    {
                        regs.d = it
                        regs.hl = 0x8000u
                        mem.asm { LD(!HL, D) }
                    },
                    "LD (HL), E" to TestCase(
                        cycles = 7,
                        size = 1,
                        result = { bus.read(0x8000u) })
                    {
                        regs.e = it
                        regs.hl = 0x8000u
                        mem.asm { LD(!HL, E) }
                    },
                    "LD (HL), H" to TestCase(
                        cycles = 7,
                        size = 1,
                        result = { bus.read(0x8080u.toUShort().setHigh(it)) })
                    {
                        regs.hl = 0x8080u
                        regs.h = it
                        mem.asm { LD(!HL, H) }
                    },
                    "LD (HL), L" to TestCase(
                        cycles = 7,
                        size = 1,
                        result = { bus.read(0x8080u.toUShort().setLow(it)) })
                    {
                        regs.hl = 0x8080u
                        regs.l = it
                        mem.asm { LD(!HL, L) }
                    },
                )
            ) { (cycles, size, result, prepare) -> behavesLike { value: UByte, prevFlags ->
                prepare(value)
                whenProcessorRuns()
                expect(cycles, pc = size.toUShort(), prevFlags) {
                    result(value) shouldBe value
                }
            } }
        }

        context("LD 16-bit") {
            data class TestCase(
                val cycles: Int,
                val size: Int,
                val result: suspend ProcessorBehavior.() -> UShort,
                val prepare: suspend ProcessorBehavior.(UShort) -> Unit,
            )

            withData(
                mapOf(
                    "LD BC, NN" to TestCase(
                        cycles = 10,
                        size = 3,
                        result = { regs.bc },
                    ) {
                        mem.asm { LD (BC, it) }
                    },
                    "LD DE, NN" to TestCase(
                        cycles = 10,
                        size = 3,
                        result = { regs.de },
                    ) {
                        mem.asm { LD(DE, it) }
                    },
                    "LD HL, NN" to TestCase(
                        cycles = 10,
                        size = 3,
                        result = { regs.hl },
                    ) {
                        mem.asm { LD(HL, it) }
                    },
                    "LD SP, NN" to TestCase(
                        cycles = 10,
                        size = 3,
                        result = { regs.sp },
                    ) {
                        mem.asm { LD(SP, it) }
                    },
                    "LD (NN), HL" to TestCase(
                        cycles = 16,
                        size = 3,
                        result = { bus.readWord(0x8000u) },
                    ) {
                        regs.hl = it
                        mem.asm {
                            LD(!0x8000u.toUShort(), HL)
                        }
                    },
                    "LD HL, (NN)" to TestCase(
                        cycles = 16,
                        size = 3,
                        result = { regs.hl },
                    ) {
                        bus.writeWord(0x8000u, it)
                        mem.asm {
                            LD(HL, !0x8000u.toUShort())
                        }
                    },
                )
            ) { (cycles, size, result, prepare) -> behavesLike { value: UShort, prevFlags ->
                prepare(value)
                whenProcessorRuns()
                expect(cycles, pc = size.toUShort(), prevFlags) { result() shouldBe value }
            } }
        }

        test("EX AF, AF'") { behavesLike {
            given {
                regs.af = 0xABCDu
                regs.`af'` = 0x1234u
            }
            whenProcessorRuns { EX(AF, `AF'`) }
            expect(cycles = 4, pc = 0x0001u) {
                regs.af shouldBe 0x1234u
                regs.`af'` shouldBe 0xABCDu
            }
        }}
    }
})


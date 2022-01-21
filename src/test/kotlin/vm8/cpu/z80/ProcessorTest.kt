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
            given(a)
            whenProcessorRuns { CCF }
            expect(cycles = 4, pc = 0x0001u)
            expectFlags { flag -> when(flag) {
                Flag.C -> flagNotCopiedFrom(flag, prevFlags)
                Flag.N -> flagIsReset(flag)
                Flag.H -> flagCopiedFrom(flag, prevFlags, copiedFromFlag = Flag.C)
                Flag.F3, Flag.F5 -> flagCopiedFrom(flag, regs.a)
                else -> flagCopiedFrom(flag, prevFlags)
            }}
        }}

        test("SCF") { behavesLike { a: UByte, prevFlags ->
            given(a)
            whenProcessorRuns { SCF }
            expect(cycles = 4, pc = 0x0001u)
            expectFlags { flag -> when(flag) {
                Flag.C -> flagIsSet(flag)
                Flag.N, Flag.H -> flagIsReset(flag)
                Flag.F3, Flag.F5 -> flagCopiedFrom(flag, regs.a)
                else -> flagCopiedFrom(flag, prevFlags)
            }}

        }}

        test("DI") { behavesLike { prevFlags ->
            given(intEnabled = true)
            whenProcessorRuns { DI }
            expect(
                cycles = 4,
                pc = 0x0001u,
                flags = prevFlags,
                intEnabled = false,
            )
        }}

        test("Non-maskable interrupts") { behavesLike { prevFlags ->
            given(pc = 0x8000u, sp = 0xF000u, nmi = true)
            givenCode(org = 0x8000u) { HALT }
            whenProcessorRuns()
            expect(
                cycles = 11,
                intEnabled = false,
                pc = 0x0066u,
                flags = prevFlags,
            )
            expectMemoryWord(regs.sp, 0x8000u)
            expectPushedWord(0xF000u,0x8000u)
        }}

        context("Maskable interrupts") {
            data class TestCase(val prepare: ProcessorBehavior.() -> Unit, val isr: UShort, val cycles: Int)

            withData(mapOf(
                "Mode 0" to TestCase(
                    prepare = {
                        given(
                            im = IntMode.Zero,
                            intAckData = OpCodes.`RST 0x08`.toUByte(),
                        )
                    },
                    isr = 0x0008u,
                    cycles = 13,
                ),
                "Mode 1" to TestCase(
                    prepare = {
                        given(im = IntMode.One)
                    },
                    isr = 0x0038u,
                    cycles = 13,
                ),
                "Mode 2" to TestCase(
                    prepare = {
                        given(im = IntMode.Two, i = 0x01u, intAckData = 0x30u)
                    },
                    isr = 0x0130u,
                    cycles = 19,
                ),
            )) { (prepare, isr, cycles) -> behavesLike { prevFlags ->
                prepare()
                givenCode(0x8000u) {
                    EI
                    HALT
                }
                givenCode(isr) { HALT }
                given(pc = 0x8000u, sp = 0xF000u, int = true)

                // Repeat 3 times to execute EI, HALT once, and then accept the interrupt.
                // Please remember that interruptions are not accepted on the next instruction after EI.
                // That's why we have to execute HALT at least once.
                repeat(3) { whenProcessorRuns() }

                // Expect cycles = cycles + 8 cause main program at 0x8000 requires 8 cycles before int is accepted
                expect(
                    cycles = cycles + 8,
                    intEnabled = false,
                    pc = isr,
                    sp = 0xEFFEu,
                    flags = prevFlags,
                )
                expectPushedWord(0xF000u,0x8001u)
            }}
        }
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
                "ADC A, N" to TestCase(
                    cycles = 7,
                    size = 2,
                    result = { regs.a },
                ) { a, b ->
                    regs.a = a
                    givenCode { ADC(A, b) }
                },
                "ADC A, A" to TestCase(
                    cycles = 4,
                    size = 1,
                    sameOperand = true,
                    result = { regs.a },
                ) { a, _ ->
                    regs.a = a
                    givenCode { ADC(A, A) }
                },
                "ADC A, B" to TestCase(
                    cycles = 4,
                    size = 1,
                    result = { regs.a },
                ) { a, b ->
                    regs.a = a
                    regs.b = b
                    givenCode { ADC(A, B) }
                },
                "ADC A, C" to TestCase(
                    cycles = 4,
                    size = 1,
                    result = { regs.a },
                ) { a, b ->
                    regs.a = a
                    regs.c = b
                    givenCode { ADC(A, C) }
                },
                "ADC A, D" to TestCase(
                    cycles = 4,
                    size = 1,
                    result = { regs.a },
                ) { a, b ->
                    regs.a = a
                    regs.d = b
                    givenCode { ADC(A, D) }
                },
                "ADC A, E" to TestCase(
                    cycles = 4,
                    size = 1,
                    result = { regs.a },
                ) { a, b ->
                    regs.a = a
                    regs.e = b
                    givenCode { ADC(A, E) }
                },
                "ADC A, H" to TestCase(
                    cycles = 4,
                    size = 1,
                    result = { regs.a },
                ) { a, b ->
                    regs.a = a
                    regs.h = b
                    givenCode { ADC(A, H) }
                },
                "ADC A, L" to TestCase(
                    cycles = 4,
                    size = 1,
                    result = { regs.a },
                ) { a, b ->
                    regs.a = a
                    regs.l = b
                    givenCode { ADC(A, L) }
                },
                "ADC A, (HL)" to TestCase(
                    cycles = 7,
                    size = 1,
                    result = { regs.a },
                ) { a, b ->
                    regs.hl = 0x8000u
                    regs.a = a
                    bus.memWriteByte(0x8000u, b)
                    givenCode { ADC(A, !HL) }
                },
            )) { (cycles, size, sameOperand, result, prepare) -> behavesLike { a: UByte, b: UByte, prevFlags ->
                prepare(a, b)
                whenProcessorRuns()
                expect(cycles = cycles, pc = size.toUShort())
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
                "ADD A, N" to TestCase(
                    cycles = 7,
                    size = 2,
                    result = { regs.a },
                ) { a, b ->
                    regs.a = a
                    givenCode { ADD(A, b) }
                },
                "ADD A, A" to TestCase(
                    cycles = 4,
                    size = 1,
                    sameOperand = true,
                    result = { regs.a },
                ) { a, _ ->
                    regs.a = a
                    givenCode { ADD(A, A) }
                },
                "ADD A, B" to TestCase(
                    cycles = 4,
                    size = 1,
                    result = { regs.a },
                ) { a, b ->
                    regs.a = a
                    regs.b = b
                    givenCode { ADD(A, B) }
                },
                "ADD A, C" to TestCase(
                    cycles = 4,
                    size = 1,
                    result = { regs.a },
                ) { a, b ->
                    regs.a = a
                    regs.c = b
                    givenCode { ADD(A, C) }
                },
                "ADD A, D" to TestCase(
                    cycles = 4,
                    size = 1,
                    result = { regs.a },
                ) { a, b ->
                    regs.a = a
                    regs.d = b
                    givenCode { ADD(A, D) }
                },
                "ADD A, E" to TestCase(
                    cycles = 4,
                    size = 1,
                    result = { regs.a },
                ) { a, b ->
                    regs.a = a
                    regs.e = b
                    givenCode { ADD(A, E) }
                },
                "ADD A, H" to TestCase(
                    cycles = 4,
                    size = 1,
                    result = { regs.a },
                ) { a, b ->
                    regs.a = a
                    regs.h = b
                    givenCode { ADD(A, H) }
                },
                "ADD A, L" to TestCase(
                    cycles = 4,
                    size = 1,
                    result = { regs.a },
                ) { a, b ->
                    regs.a = a
                    regs.l = b
                    givenCode { ADD(A, L) }
                },
                "ADD A, (HL)" to TestCase(
                    cycles = 7,
                    size = 1,
                    result = { regs.a },
                ) { a, b ->
                    regs.hl = 0x8000u
                    regs.a = a
                    bus.memWriteByte(0x8000u, b)
                    givenCode { ADD(A, !HL) }
                },
            )) { (cycles, size, sameOperand, result, prepare) -> behavesLike { a: UByte, b: UByte, _ ->
                prepare(a, b)
                whenProcessorRuns()
                expect(cycles = cycles, pc = size.toUShort())
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
                    givenCode { ADD(HL, BC) }
                },
                "ADD HL, DE" to TestCase(
                    cycles = 11,
                    size = 1,
                    result = { regs.hl },
                ) { a, b ->
                    regs.hl = a
                    regs.de = b
                    givenCode { ADD(HL, DE) }
                },
                "ADD HL, HL" to TestCase(
                    cycles = 11,
                    size = 1,
                    sameOperand = true,
                    result = { regs.hl },
                ) { a, _ ->
                    regs.hl = a
                    givenCode { ADD(HL, HL) }
                },
                "ADD HL, SP" to TestCase(
                    cycles = 11,
                    size = 1,
                    result = { regs.hl },
                ) { a, b ->
                    regs.hl = a
                    regs.sp = b
                    givenCode { ADD(HL, SP) }
                },
            )) { (cycles, size, sameOperand, result, prepare) -> behavesLike { a: UShort, b: UShort, prevFlags ->
                prepare(a, b)
                whenProcessorRuns()
                expect(cycles = cycles, pc = size.toUShort())
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
                "AND N" to TestCase(
                    cycles = 7,
                    size = 2,
                    result = { regs.a },
                ) { a, b ->
                    regs.a = a
                    givenCode { AND(b) }
                },
                "AND A" to TestCase(
                    cycles = 4,
                    size = 1,
                    sameOperand = true,
                    result = { regs.a },
                ) { a, _ ->
                    regs.a = a
                    givenCode { AND(A) }
                },
                "AND B" to TestCase(
                    cycles = 4,
                    size = 1,
                    result = { regs.a },
                ) { a, b ->
                    regs.a = a
                    regs.b = b
                    givenCode { AND(B) }
                },
                "AND C" to TestCase(
                    cycles = 4,
                    size = 1,
                    result = { regs.a },
                ) { a, b ->
                    regs.a = a
                    regs.c = b
                    givenCode { AND(C) }
                },
                "AND D" to TestCase(
                    cycles = 4,
                    size = 1,
                    result = { regs.a },
                ) { a, b ->
                    regs.a = a
                    regs.d = b
                    givenCode { AND(D) }
                },
                "AND E" to TestCase(
                    cycles = 4,
                    size = 1,
                    result = { regs.a },
                ) { a, b ->
                    regs.a = a
                    regs.e = b
                    givenCode { AND(E) }
                },
                "AND H" to TestCase(
                    cycles = 4,
                    size = 1,
                    result = { regs.a },
                ) { a, b ->
                    regs.a = a
                    regs.h = b
                    givenCode { AND(H) }
                },
                "AND L" to TestCase(
                    cycles = 4,
                    size = 1,
                    result = { regs.a },
                ) { a, b ->
                    regs.a = a
                    regs.l = b
                    givenCode { AND(L) }
                },
                "AND (HL)" to TestCase(
                    cycles = 7,
                    size = 1,
                    result = { regs.a },
                ) { a, b ->
                    regs.hl = 0x8000u
                    regs.a = a
                    bus.memWriteByte(0x8000u, b)
                    givenCode { AND(!HL) }
                },
            )) { (cycles, size, sameOperand, result, prepare) -> behavesLike { a: UByte, b: UByte, _ ->
                prepare(a, b)
                whenProcessorRuns()
                expect(cycles = cycles, pc = size.toUShort())
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
            }}
        }

        context("CP") {
            data class TestCase(
                val cycles: Int,
                val size: Int,
                val sameOperand: Boolean = false,
                val prepare: suspend ProcessorBehavior.(UByte, UByte) -> Unit,
            )

            withData(mapOf(
                "CP N" to TestCase(
                    cycles = 7,
                    size = 2,
                ) { a, b ->
                    regs.a = a
                    givenCode { CP(b) }
                },
                "CP A" to TestCase(
                    cycles = 4,
                    size = 1,
                    sameOperand = true,
                ) { a, _ ->
                    regs.a = a
                    givenCode { CP(A) }
                },
                "CP B" to TestCase(
                    cycles = 4,
                    size = 1,
                ) { a, b ->
                    regs.a = a
                    regs.b = b
                    givenCode { CP(B) }
                },
                "CP C" to TestCase(
                    cycles = 4,
                    size = 1,
                ) { a, b ->
                    regs.a = a
                    regs.c = b
                    givenCode { CP(C) }
                },
                "CP D" to TestCase(
                    cycles = 4,
                    size = 1,
                ) { a, b ->
                    regs.a = a
                    regs.d = b
                    givenCode { CP(D) }
                },
                "CP E" to TestCase(
                    cycles = 4,
                    size = 1,
                ) { a, b ->
                    regs.a = a
                    regs.e = b
                    givenCode { CP(E) }
                },
                "CP H" to TestCase(
                    cycles = 4,
                    size = 1,
                ) { a, b ->
                    regs.a = a
                    regs.h = b
                    givenCode { CP(H) }
                },
                "CP L" to TestCase(
                    cycles = 4,
                    size = 1,
                ) { a, b ->
                    regs.a = a
                    regs.l = b
                    givenCode { CP(L) }
                },
                "CP (HL)" to TestCase(
                    cycles = 7,
                    size = 1,
                ) { a, b ->
                    regs.hl = 0x8000u
                    regs.a = a
                    bus.memWriteByte(0x8000u, b)
                    givenCode { CP(!HL) }
                },
            )) { (cycles, size, sameOperand, prepare) -> behavesLike { a: UByte, b: UByte, _ ->
                val result = if (sameOperand) 0u else (a - b).toUByte()
                prepare(a, b)
                whenProcessorRuns()
                expect(cycles = cycles, pc = size.toUShort())
                expectFlags { flag -> when(flag) {
                    Flag.C -> flagIsSetOn(flag, borrow(a, result))
                    Flag.N -> flagIsSet(flag)
                    Flag.PV -> flagIsSetOn(flag, underflow(a, result))
                    Flag.H -> flagIsSetOn(flag, halfBorrow(a, result))
                    Flag.Z -> flagIsSetOn(flag, isZero(result))
                    Flag.S -> flagIsSetOn(flag, isNegative(result))
                    Flag.F3, Flag.F5 -> flagCopiedFrom(flag, if (sameOperand) a else b)
                }}
            }}
        }

        test("CPL") { behavesLike { a: UByte, prevFlags ->
            given { regs.a = a }
            whenProcessorRuns { CPL }
            expect(cycles = 4, pc = 0x0001u, a = a.inv())
            expectFlags { flag -> when(flag) {
                Flag.C, Flag.PV, Flag.Z, Flag.S -> flagCopiedFrom(flag, prevFlags)
                Flag.N, Flag.H -> flagIsSet(flag)
                Flag.F3, Flag.F5 -> flagCopiedFrom(flag, regs.a)
            }}
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
                expect(cycles = 4, pc = 0x0001u)
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
                        givenCode { DEC(A) }
                    },
                    "DEC B" to TestCase(
                        cycles = 4,
                        size = 1,
                        result = { regs.b },
                    ) {
                        regs.b = it
                        givenCode { DEC(B) }
                    },
                    "DEC C" to TestCase(
                        cycles = 4,
                        size = 1,
                        result = { regs.c },
                    ) {
                        regs.c = it
                        givenCode { DEC(C) }
                    },
                    "DEC D" to TestCase(
                        cycles = 4,
                        size = 1,
                        result = { regs.d },
                    ) {
                        regs.d = it
                        givenCode { DEC(D) }
                    },
                    "DEC E" to TestCase(
                        cycles = 4,
                        size = 1,
                        result = { regs.e },
                    ) {
                        regs.e = it
                        givenCode { DEC(E) }
                    },
                    "DEC H" to TestCase(
                        cycles = 4,
                        size = 1,
                        result = { regs.h },
                    ) {
                        regs.h = it
                        givenCode { DEC(H) }
                    },
                    "DEC L" to TestCase(
                        cycles = 4,
                        size = 1,
                        result = { regs.l },
                    ) {
                        regs.l = it
                        givenCode { DEC(L) }
                    },
                    "DEC (HL)" to TestCase(
                        cycles = 11,
                        size = 1,
                        result = { bus.memReadByte(0x8000u) },
                    ) {
                        regs.hl = 0x8000u
                        bus.memWriteByte(0x8000u, it)
                        givenCode { DEC(!HL) }
                    },
                )
            ) { (cycles, size, result, prepare) -> behavesLike { value: UByte, prevFlags ->
                prepare(value)
                whenProcessorRuns()

                expect(cycles = cycles, pc = size.toUShort())
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
                    givenCode { DEC(BC) }
                },
                "DEC DE" to TestCase(
                    cycles = 6,
                    size = 1,
                    result = { regs.de }
                ) {
                    regs.de = it
                    givenCode { DEC(DE) }
                },
                "DEC HL" to TestCase(
                    cycles = 6,
                    size = 1,
                    result = { regs.hl }
                ) {
                    regs.hl = it
                    givenCode { DEC(HL) }
                },
                "DEC SP" to TestCase(
                    cycles = 6,
                    size = 1,
                    result = { regs.sp }
                ) {
                    regs.sp = it
                    givenCode { DEC(SP) }
                },
            )) { (cycles, size, result, prepare) -> behavesLike { value: UShort, prevFlags ->
                prepare(value)
                whenProcessorRuns()
                expect(cycles = cycles, pc = size.toUShort(), flags = prevFlags)
                result() shouldBe value.dec()
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
                        givenCode { INC(A) }
                    },
                    "INC B" to TestCase(
                        cycles = 4,
                        size = 1,
                        result = { regs.b }
                    ) {
                        regs.b = it
                        givenCode { INC(B) }
                    },
                    "INC C" to TestCase(
                        cycles = 4,
                        size = 1,
                        result = { regs.c }
                    ) {
                        regs.c = it
                        givenCode { INC(C) }
                    },
                    "INC D" to TestCase(
                        cycles = 4,
                        size = 1,
                        result = { regs.d }
                    ) {
                        regs.d = it
                        givenCode { INC(D) }
                    },
                    "INC E" to TestCase(
                        cycles = 4,
                        size = 1,
                        result = { regs.e }
                    ) {
                        regs.e = it
                        givenCode { INC(E) }
                    },
                    "INC H" to TestCase(
                        cycles = 4,
                        size = 1,
                        result = { regs.h }
                    ) {
                        regs.h = it
                        givenCode { INC(H) }
                    },
                    "INC L" to TestCase(
                        cycles = 4,
                        size = 1,
                        result = { regs.l }
                    ) {
                        regs.l = it
                        givenCode { INC(L) }
                    },
                    "INC (HL)" to TestCase(
                        cycles = 11,
                        size = 1,
                        result = { bus.memReadByte(0x8000u) }
                    ) {
                        bus.memWriteByte(0x8000u, it)
                        regs.hl = 0x8000u
                        givenCode { INC(!HL) }
                    },
                )
            ) { (cycles, size, result, prepare) -> behavesLike { value: UByte, prevFlags ->
                prepare(value)
                whenProcessorRuns()

                expect(cycles = cycles, pc = size.toUShort())
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
                    givenCode { INC(BC) }
                },
                "INC DE" to TestCase(
                    cycles = 6,
                    size = 1,
                    result = { regs.de }
                ) {
                    regs.de = it
                    givenCode { INC(DE) }
                },
                "INC HL" to TestCase(
                    cycles = 6,
                    size = 1,
                    result = { regs.hl }
                ) {
                    regs.hl = it
                    givenCode { INC(HL) }
                },
                "INC SP" to TestCase(
                    cycles = 6,
                    size = 1,
                    result = { regs.sp }
                ) {
                    regs.sp = it
                    givenCode { INC(SP) }
                },
            )) { (cycles, size, result, prepare) -> behavesLike { value: UShort, prevFlags ->
                prepare(value)
                whenProcessorRuns()
                expect(cycles = cycles, pc = size.toUShort(), flags = prevFlags)
                result() shouldBe value.inc()
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
                "OR N" to TestCase(
                    cycles = 7,
                    size = 2,
                    result = { regs.a },
                ) { a, b ->
                    regs.a = a
                    givenCode { OR(b) }
                },
                "OR A" to TestCase(
                    cycles = 4,
                    size = 1,
                    sameOperand = true,
                    result = { regs.a },
                ) { a, _ ->
                    regs.a = a
                    givenCode { OR(A) }
                },
                "OR B" to TestCase(
                    cycles = 4,
                    size = 1,
                    result = { regs.a },
                ) { a, b ->
                    regs.a = a
                    regs.b = b
                    givenCode { OR(B) }
                },
                "OR C" to TestCase(
                    cycles = 4,
                    size = 1,
                    result = { regs.a },
                ) { a, b ->
                    regs.a = a
                    regs.c = b
                    givenCode { OR(C) }
                },
                "OR D" to TestCase(
                    cycles = 4,
                    size = 1,
                    result = { regs.a },
                ) { a, b ->
                    regs.a = a
                    regs.d = b
                    givenCode { OR(D) }
                },
                "OR E" to TestCase(
                    cycles = 4,
                    size = 1,
                    result = { regs.a },
                ) { a, b ->
                    regs.a = a
                    regs.e = b
                    givenCode { OR(E) }
                },
                "OR H" to TestCase(
                    cycles = 4,
                    size = 1,
                    result = { regs.a },
                ) { a, b ->
                    regs.a = a
                    regs.h = b
                    givenCode { OR(H) }
                },
                "OR L" to TestCase(
                    cycles = 4,
                    size = 1,
                    result = { regs.a },
                ) { a, b ->
                    regs.a = a
                    regs.l = b
                    givenCode { OR(L) }
                },
                "OR (HL)" to TestCase(
                    cycles = 7,
                    size = 1,
                    result = { regs.a },
                ) { a, b ->
                    regs.hl = 0x8000u
                    regs.a = a
                    bus.memWriteByte(0x8000u, b)
                    givenCode { OR(!HL) }
                },
            )) { (cycles, size, sameOperand, result, prepare) -> behavesLike { a: UByte, b: UByte, _ ->
                prepare(a, b)
                whenProcessorRuns()
                expect(cycles = cycles, pc = size.toUShort())
                if (sameOperand) { result() shouldBe (a or a) }
                else { result() shouldBe (a or b) }

                expectFlags { flag -> when(flag) {
                    Flag.C, Flag.N, Flag.H -> flagIsReset(flag)
                    Flag.PV -> flagIsSetOn(flag, hasEvenParity(result()))
                    Flag.Z -> flagIsSetOn(flag, isZero(result()))
                    Flag.S -> flagIsSetOn(flag, isNegative(result()))
                    Flag.F3, Flag.F5 -> flagCopiedFrom(flag, result())
                }}
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
                    givenCode { SBC(A) }
                },
                "SBC B" to TestCase(
                    cycles = 4,
                    size = 1,
                    result = { regs.a },
                ) { a, b ->
                    regs.a = a
                    regs.b = b
                    givenCode { SBC(B) }
                },
                "SBC C" to TestCase(
                    cycles = 4,
                    size = 1,
                    result = { regs.a },
                ) { a, b ->
                    regs.a = a
                    regs.c = b
                    givenCode { SBC(C) }
                },
                "SBC D" to TestCase(
                    cycles = 4,
                    size = 1,
                    result = { regs.a },
                ) { a, b ->
                    regs.a = a
                    regs.d = b
                    givenCode { SBC(D) }
                },
                "SBC E" to TestCase(
                    cycles = 4,
                    size = 1,
                    result = { regs.a },
                ) { a, b ->
                    regs.a = a
                    regs.e = b
                    givenCode { SBC(E) }
                },
                "SBC H" to TestCase(
                    cycles = 4,
                    size = 1,
                    result = { regs.a },
                ) { a, b ->
                    regs.a = a
                    regs.h = b
                    givenCode { SBC(H) }
                },
                "SBC L" to TestCase(
                    cycles = 4,
                    size = 1,
                    result = { regs.a },
                ) { a, b ->
                    regs.a = a
                    regs.l = b
                    givenCode { SBC(L) }
                },
                "SBC (HL)" to TestCase(
                    cycles = 7,
                    size = 1,
                    result = { regs.a },
                ) { a, b ->
                    regs.hl = 0x8000u
                    regs.a = a
                    bus.memWriteByte(0x8000u, b)
                    givenCode { SBC(!HL) }
                },
            )) { (cycles, size, sameOperand, result, prepare) -> behavesLike { a: UByte, b: UByte, prevFlags ->
                prepare(a, b)
                whenProcessorRuns()
                expect(cycles = cycles, pc = size.toUShort())
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
                    givenCode { SUB(A) }
                },
                "SUB B" to TestCase(
                    cycles = 4,
                    size = 1,
                    result = { regs.a },
                ) { a, b ->
                    regs.a = a
                    regs.b = b
                    givenCode { SUB(B) }
                },
                "SUB C" to TestCase(
                    cycles = 4,
                    size = 1,
                    result = { regs.a },
                ) { a, b ->
                    regs.a = a
                    regs.c = b
                    givenCode { SUB(C) }
                },
                "SUB D" to TestCase(
                    cycles = 4,
                    size = 1,
                    result = { regs.a },
                ) { a, b ->
                    regs.a = a
                    regs.d = b
                    givenCode { SUB(D) }
                },
                "SUB E" to TestCase(
                    cycles = 4,
                    size = 1,
                    result = { regs.a },
                ) { a, b ->
                    regs.a = a
                    regs.e = b
                    givenCode { SUB(E) }
                },
                "SUB H" to TestCase(
                    cycles = 4,
                    size = 1,
                    result = { regs.a },
                ) { a, b ->
                    regs.a = a
                    regs.h = b
                    givenCode { SUB(H) }
                },
                "SUB L" to TestCase(
                    cycles = 4,
                    size = 1,
                    result = { regs.a },
                ) { a, b ->
                    regs.a = a
                    regs.l = b
                    givenCode { SUB(L) }
                },
                "SUB (HL)" to TestCase(
                    cycles = 7,
                    size = 1,
                    result = { regs.a },
                ) { a, b ->
                    regs.hl = 0x8000u
                    regs.a = a
                    bus.memWriteByte(0x8000u, b)
                    givenCode { SUB(!HL) }
                },
            )) { (cycles, size, sameOperand, result, prepare) -> behavesLike { a: UByte, b: UByte, _ ->
                prepare(a, b)
                whenProcessorRuns()
                expect(cycles = cycles, pc = size.toUShort())
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
                "XOR N" to TestCase(
                    cycles = 7,
                    size = 2,
                    result = { regs.a },
                ) { a, b ->
                    regs.a = a
                    givenCode { XOR(b) }
                },
                "XOR A" to TestCase(
                    cycles = 4,
                    size = 1,
                    sameOperand = true,
                    result = { regs.a },
                ) { a, _ ->
                    regs.a = a
                    givenCode { XOR(A) }
                },
                "XOR B" to TestCase(
                    cycles = 4,
                    size = 1,
                    result = { regs.a },
                ) { a, b ->
                    regs.a = a
                    regs.b = b
                    givenCode { XOR(B) }
                },
                "XOR C" to TestCase(
                    cycles = 4,
                    size = 1,
                    result = { regs.a },
                ) { a, b ->
                    regs.a = a
                    regs.c = b
                    givenCode { XOR(C) }
                },
                "XOR D" to TestCase(
                    cycles = 4,
                    size = 1,
                    result = { regs.a },
                ) { a, b ->
                    regs.a = a
                    regs.d = b
                    givenCode { XOR(D) }
                },
                "XOR E" to TestCase(
                    cycles = 4,
                    size = 1,
                    result = { regs.a },
                ) { a, b ->
                    regs.a = a
                    regs.e = b
                    givenCode { XOR(E) }
                },
                "XOR H" to TestCase(
                    cycles = 4,
                    size = 1,
                    result = { regs.a },
                ) { a, b ->
                    regs.a = a
                    regs.h = b
                    givenCode { XOR(H) }
                },
                "XOR L" to TestCase(
                    cycles = 4,
                    size = 1,
                    result = { regs.a },
                ) { a, b ->
                    regs.a = a
                    regs.l = b
                    givenCode { XOR(L) }
                },
                "XOR (HL)" to TestCase(
                    cycles = 7,
                    size = 1,
                    result = { regs.a },
                ) { a, b ->
                    regs.hl = 0x8000u
                    regs.a = a
                    bus.memWriteByte(0x8000u, b)
                    givenCode { XOR(!HL) }
                },
            )) { (cycles, size, sameOperand, result, prepare) -> behavesLike { a: UByte, b: UByte, _ ->
                prepare(a, b)
                whenProcessorRuns()
                expect(cycles = cycles, pc = size.toUShort())
                if (sameOperand) { result() shouldBe (a xor a).toUByte() }
                else { result() shouldBe (a xor b).toUByte() }

                expectFlags { flag -> when(flag) {
                    Flag.C, Flag.N, Flag.H -> flagIsReset(flag)
                    Flag.PV -> flagIsSetOn(flag, hasEvenParity(result()))
                    Flag.Z -> flagIsSetOn(flag, isZero(result()))
                    Flag.S -> flagIsSetOn(flag, isNegative(result()))
                    Flag.F3, Flag.F5 -> flagCopiedFrom(flag, result())
                }}
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
        context("Absolute jump") {
            data class TestCase(
                val cycles: Int,
                val size: Int,
                val cond: ProcessorBehavior.() -> Boolean,
                val prepare: suspend ProcessorBehavior.(UShort) -> Unit,
            )

            withData(mapOf(
                "JP NN" to TestCase(cycles = 10, size = 3, cond = { true }) {
                    givenCode { JP(it) }
                },
                "JP Z, NN" to TestCase(cycles = 10, size = 3, cond = { regs.f.bit(6) }) {
                    givenCode { JP(Z, it) }
                },
                "JP NZ, NN" to TestCase(cycles = 10, size = 3, cond = { !regs.f.bit(6) }) {
                    givenCode { JP(NZ, it) }
                },
                "JP C, NN" to TestCase(cycles = 10, size = 3, cond = { regs.f.bit(0) }) {
                    givenCode { JP(C, it) }
                },
                "JP NC, NN" to TestCase(cycles = 10, size = 3, cond = { !regs.f.bit(0) }) {
                    givenCode { JP(NC, it) }
                },
                "JP PE, NN" to TestCase(cycles = 10, size = 3, cond = { regs.f.bit(2) }) {
                    givenCode { JP(PE, it) }
                },
                "JP PO, NN" to TestCase(cycles = 10, size = 3, cond = { !regs.f.bit(2) }) {
                    givenCode { JP(PO, it) }
                },
                "JP P, NN" to TestCase(cycles = 10, size = 3, cond = { !regs.f.bit(7) }) {
                    givenCode { JP(P, it) }
                },
                "JP M, NN" to TestCase(cycles = 10, size = 3, cond = { regs.f.bit(7) }) {
                    givenCode { JP(M, it) }
                },
                "JP (HL)" to TestCase(cycles = 4, size = 1, cond = { true }) {
                    given(hl = it)
                    givenCode { JP(!HL) }
                },
            )) { (cycles, size, cond, prepare) -> behavesLike { dest: UShort, prevFlags ->
                prepare(dest)
                whenProcessorRuns()
                expect(
                    cycles = cycles,
                    flags = prevFlags,
                    pc = if (cond()) dest else size.toUShort()
                )
            }}
        }

        context("Relative jump") {
            data class TestCase(
                val cond: ProcessorBehavior.() -> Boolean,
                val prepare: ProcessorBehavior.(Byte) -> Unit,
            )

            withData(mapOf(
                "JR N" to TestCase(cond = { true }) {
                    givenCode { JR(it)}
                },
                "JR N, N" to TestCase(cond = { regs.f.bit(6) }) {
                    givenCode { JR(Z, it)}
                },
                "JR NZ, N" to TestCase(cond = { !regs.f.bit(6) }) {
                    givenCode { JR(NZ, it)}
                },
                "JR C, N" to TestCase(cond = { regs.f.bit(0) }) {
                    givenCode { JR(C, it)}
                },
                "JR NC, N" to TestCase(cond = { !regs.f.bit(0) }) {
                    givenCode { JR(NC, it)}
                },
            )) { (cond, prepare) -> behavesLike { n: Byte, prevFlags ->
                prepare(n)
                whenProcessorRuns()
                expect(
                    cycles = if (cond()) 12 else 7,
                    flags = prevFlags,
                    pc = if (cond()) 0x0000.toUShort().increment(n) else 0x0002u,
                )
            }}
        }

        context("Call") {
            data class TestCase(
                val cond: ProcessorBehavior.() -> Boolean,
                val inst: Assembler.(dest: UShort) -> Unit,
            )

            withData(mapOf(
                "CALL NN" to TestCase(
                    cond = { true },
                    inst = { CALL(it) }
                ),
                "CALL NZ, NN" to TestCase(
                    cond = { FlagsPredicate.NZ.evaluate(regs.f) },
                    inst = { CALL(NZ, it) }
                ),
                "CALL Z, NN" to TestCase(
                    cond = { FlagsPredicate.Z.evaluate(regs.f) },
                    inst = { CALL(Z, it) }
                ),
                "CALL NC, NN" to TestCase(
                    cond = { FlagsPredicate.NC.evaluate(regs.f) },
                    inst = { CALL(NC, it) }
                ),
                "CALL C, NN" to TestCase(
                    cond = { FlagsPredicate.C.evaluate(regs.f) },
                    inst = { CALL(C, it) }
                ),
                "CALL PO, NN" to TestCase(
                    cond = { FlagsPredicate.PO.evaluate(regs.f) },
                    inst = { CALL(PO, it) }
                ),
                "CALL PE, NN" to TestCase(
                    cond = { FlagsPredicate.PE.evaluate(regs.f) },
                    inst = { CALL(PE, it) }
                ),
                "CALL P, NN" to TestCase(
                    cond = { FlagsPredicate.P.evaluate(regs.f) },
                    inst = { CALL(P, it) }
                ),
                "CALL M, NN" to TestCase(
                    cond = { FlagsPredicate.M.evaluate(regs.f) },
                    inst = { CALL(M, it) }
                ),
            )) { (cond, inst) -> behavesLike { dest: UShort, prevFlags ->
                given(pc = 0x8000u, sp = 0xFFFFu)
                givenCode(org = 0x8000u) { inst(dest) }

                whenProcessorRuns()

                expect(
                    cycles = if (cond()) 17 else 10,
                    flags = prevFlags,
                    pc = if (cond()) dest else 0x8003u,
                )
            }}
        }

        context("Return") {
            data class TestCase(
                val jcycles: Int,
                val cond: ProcessorBehavior.() -> Boolean,
                val prepare: suspend ProcessorBehavior.(Byte) -> Unit,
            )

            withData(mapOf(
                "RET" to TestCase(jcycles = 10, cond = { true }) {
                    regs.sp = 0xFFFEu
                    givenMemoryWord(0xFFFEu, 0x8000u)
                    givenCode { RET }
                },
                "RET NC" to TestCase(jcycles = 11, cond = { !regs.f.bit(0) }) {
                    regs.sp = 0xFFFEu
                    givenMemoryWord(0xFFFEu, 0x8000u)
                    givenCode { RET(NC) }
                },
                "RET C" to TestCase(jcycles = 11, cond = { regs.f.bit(0) }) {
                    regs.sp = 0xFFFEu
                    givenMemoryWord(0xFFFEu, 0x8000u)
                    givenCode { RET(C) }
                },
                "RET PO" to TestCase(jcycles = 11, cond = { !regs.f.bit(2) }) {
                    regs.sp = 0xFFFEu
                    givenMemoryWord(0xFFFEu, 0x8000u)
                    givenCode { RET(PO) }
                },
                "RET PE" to TestCase(jcycles = 11, cond = { regs.f.bit(2) }) {
                    regs.sp = 0xFFFEu
                    givenMemoryWord(0xFFFEu, 0x8000u)
                    givenCode { RET(PE) }
                },
                "RET NZ" to TestCase(jcycles = 11, cond = { !regs.f.bit(6) }) {
                    regs.sp = 0xFFFEu
                    givenMemoryWord(0xFFFEu, 0x8000u)
                    givenCode { RET(NZ) }
                },
                "RET Z" to TestCase(jcycles = 11, cond = { regs.f.bit(6) }) {
                    regs.sp = 0xFFFEu
                    givenMemoryWord(0xFFFEu, 0x8000u)
                    givenCode { RET(Z) }
                },
                "RET P" to TestCase(jcycles = 11, cond = { !regs.f.bit(7) }) {
                    regs.sp = 0xFFFEu
                    givenMemoryWord(0xFFFEu, 0x8000u)
                    givenCode { RET(P) }
                },
                "RET M" to TestCase(jcycles = 11, cond = { regs.f.bit(7) }) {
                    regs.sp = 0xFFFEu
                    givenMemoryWord(0xFFFEu, 0x8000u)
                    givenCode { RET(M) }
                },
            )) { (jcycles, cond, prepare) -> behavesLike { n: Byte, prevFlags ->
                prepare(n)
                whenProcessorRuns()
                expect(
                    cycles = if (cond()) jcycles else 5,
                    pc = if (cond()) 0x8000u else 0x0001u,
                    flags = prevFlags,
                    sp = if (cond()) 0x0000u else 0xFFFEu,
                )
            }}
        }

        test("DJNZ") { behavesLike { value: UByte, prevFlags ->
            given(b = value)
            whenProcessorRuns { DJNZ(0x42) }
            expect(
                b = value.dec(),
                cycles = if (regs.b.isZero()) 8 else 13,
                pc = if (regs.b.isZero()) 0x0002u else 0x0042u,
                flags = prevFlags,
            )
        }}

        context("Reset") {
            data class TestCase(val vector: UShort, val inst: Assembler.() -> Unit)

            withData(mapOf(
                "0x00" to TestCase(vector = 0x0000u, inst = { RST(0x00) }),
                "0x08" to TestCase(vector = 0x0008u, inst = { RST(0x08) }),
                "0x10" to TestCase(vector = 0x0010u, inst = { RST(0x10) }),
                "0x18" to TestCase(vector = 0x0018u, inst = { RST(0x18) }),
                "0x20" to TestCase(vector = 0x0020u, inst = { RST(0x20) }),
                "0x28" to TestCase(vector = 0x0028u, inst = { RST(0x28) }),
                "0x30" to TestCase(vector = 0x0030u, inst = { RST(0x30) }),
                "0x38" to TestCase(vector = 0x0038u, inst = { RST(0x38) }),
            )) { (vector, inst) -> behavesLike { prevFlags ->
                given { regs.pc = 0x8000u}
                whenProcessorRuns(0x8000u) { inst() }
                expect(cycles = 11, pc = vector, flags = prevFlags)
            }}
        }
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
                        givenCode { LD(A, it) }
                    },
                    "LD B, N" to TestCase(
                        cycles = 7,
                        size = 2,
                        result = { regs.b })
                    {
                        givenCode { LD(B, it) }
                    },
                    "LD C, N" to TestCase(
                        cycles = 7,
                        size = 2,
                        result = { regs.c })
                    {
                        givenCode { LD(C, it) }
                    },
                    "LD D, N" to TestCase(
                        cycles = 7,
                        size = 2,
                        result = { regs.d })
                    {
                        givenCode { LD(D, it) }
                    },
                    "LD E, N" to TestCase(
                        cycles = 7,
                        size = 2,
                        result = { regs.e })
                    {
                        givenCode { LD(E, it) }
                    },
                    "LD H, N" to TestCase(
                        cycles = 7,
                        size = 2,
                        result = { regs.h })
                    {
                        givenCode { LD(H, it) }
                    },
                    "LD L, N" to TestCase(
                        cycles = 7,
                        size = 2,
                        result = { regs.l })
                    {
                        givenCode { LD(L, it) }
                    },
                    "LD A, A" to TestCase(
                        cycles = 4,
                        size = 1,
                        result = { regs.a })
                    {
                        regs.a = it
                        givenCode { LD(A, A) }
                    },
                    "LD A, B" to TestCase(
                        cycles = 4,
                        size = 1,
                        result = { regs.a })
                    {
                        regs.b = it
                        givenCode { LD(A, B) }
                    },
                    "LD A, C" to TestCase(
                        cycles = 4,
                        size = 1,
                        result = { regs.a })
                    {
                        regs.c = it
                        givenCode { LD(A, C) }
                    },
                    "LD A, D" to TestCase(
                        cycles = 4,
                        size = 1,
                        result = { regs.a })
                    {
                        regs.d = it
                        givenCode { LD(A, D) }
                    },
                    "LD A, E" to TestCase(
                        cycles = 4,
                        size = 1,
                        result = { regs.a })
                    {
                        regs.e = it
                        givenCode { LD(A, E) }
                    },
                    "LD A, H" to TestCase(
                        cycles = 4,
                        size = 1,
                        result = { regs.a })
                    {
                        regs.h = it
                        givenCode { LD(A, H) }
                    },
                    "LD A, L" to TestCase(
                        cycles = 4,
                        size = 1,
                        result = { regs.a })
                    {
                        regs.l = it
                        givenCode { LD(A, L) }
                    },
                    "LD A, (HL)" to TestCase(
                        cycles = 7,
                        size = 1,
                        result = { regs.a })
                    {
                        regs.hl = 0x8000u
                        bus.memWriteByte(0x8000u, it)
                        givenCode { LD(A, !HL) }
                    },
                    "LD B, A" to TestCase(
                        cycles = 4,
                        size = 1,
                        result = { regs.b })
                    {
                        regs.a = it
                        givenCode { LD(B, A) }
                    },
                    "LD B, B" to TestCase(
                        cycles = 4,
                        size = 1,
                        result = { regs.b })
                    {
                        regs.b = it
                        givenCode { LD(B, B) }
                    },
                    "LD B, C" to TestCase(
                        cycles = 4,
                        size = 1,
                        result = { regs.b })
                    {
                        regs.c = it
                        givenCode { LD(B, C) }
                    },
                    "LD B, D" to TestCase(
                        cycles = 4,
                        size = 1,
                        result = { regs.b })
                    {
                        regs.d = it
                        givenCode { LD(B, D) }
                    },
                    "LD B, E" to TestCase(
                        cycles = 4,
                        size = 1,
                        result = { regs.b })
                    {
                        regs.e = it
                        givenCode { LD(B, E) }
                    },
                    "LD B, H" to TestCase(
                        cycles = 4,
                        size = 1,
                        result = { regs.b })
                    {
                        regs.h = it
                        givenCode { LD(B, H) }
                    },
                    "LD B, L" to TestCase(
                        cycles = 4,
                        size = 1,
                        result = { regs.b })
                    {
                        regs.l = it
                        givenCode { LD(B, L) }
                    },
                    "LD B, (HL)" to TestCase(
                        cycles = 7,
                        size = 1,
                        result = { regs.b })
                    {
                        regs.hl = 0x8000u
                        bus.memWriteByte(0x8000u, it)
                        givenCode { LD(B, !HL) }
                    },
                    "LD C, A" to TestCase(
                        cycles = 4,
                        size = 1,
                        result = { regs.c })
                    {
                        regs.a = it
                        givenCode { LD(C, A) }
                    },
                    "LD C, B" to TestCase(
                        cycles = 4,
                        size = 1,
                        result = { regs.c })
                    {
                        regs.b = it
                        givenCode { LD(C, B) }
                    },
                    "LD C, C" to TestCase(
                        cycles = 4,
                        size = 1,
                        result = { regs.c })
                    {
                        regs.c = it
                        givenCode { LD(C, C) }
                    },
                    "LD C, D" to TestCase(
                        cycles = 4,
                        size = 1,
                        result = { regs.c })
                    {
                        regs.d = it
                        givenCode { LD(C, D) }
                    },
                    "LD C, E" to TestCase(
                        cycles = 4,
                        size = 1,
                        result = { regs.c })
                    {
                        regs.e = it
                        givenCode { LD(C, E) }
                    },
                    "LD C, H" to TestCase(
                        cycles = 4,
                        size = 1,
                        result = { regs.c })
                    {
                        regs.h = it
                        givenCode { LD(C, H) }
                    },
                    "LD C, L" to TestCase(
                        cycles = 4,
                        size = 1,
                        result = { regs.c })
                    {
                        regs.l = it
                        givenCode { LD(C, L) }
                    },
                    "LD C, (HL)" to TestCase(
                        cycles = 7,
                        size = 1,
                        result = { regs.c })
                    {
                        regs.hl = 0x8000u
                        bus.memWriteByte(0x8000u, it)
                        givenCode { LD(C, !HL) }
                    },
                    "LD D, A" to TestCase(
                        cycles = 4,
                        size = 1,
                        result = { regs.d })
                    {
                        regs.a = it
                        givenCode { LD(D, A) }
                    },
                    "LD D, B" to TestCase(
                        cycles = 4,
                        size = 1,
                        result = { regs.d })
                    {
                        regs.b = it
                        givenCode { LD(D, B) }
                    },
                    "LD D, C" to TestCase(
                        cycles = 4,
                        size = 1,
                        result = { regs.d })
                    {
                        regs.c = it
                        givenCode { LD(D, C) }
                    },
                    "LD D, D" to TestCase(
                        cycles = 4,
                        size = 1,
                        result = { regs.d })
                    {
                        regs.d = it
                        givenCode { LD(D, D) }
                    },
                    "LD D, E" to TestCase(
                        cycles = 4,
                        size = 1,
                        result = { regs.d })
                    {
                        regs.e = it
                        givenCode { LD(D, E) }
                    },
                    "LD D, H" to TestCase(
                        cycles = 4,
                        size = 1,
                        result = { regs.d })
                    {
                        regs.h = it
                        givenCode { LD(D, H) }
                    },
                    "LD D, L" to TestCase(
                        cycles = 4,
                        size = 1,
                        result = { regs.d })
                    {
                        regs.l = it
                        givenCode { LD(D, L) }
                    },
                    "LD D, (HL)" to TestCase(
                        cycles = 7,
                        size = 1,
                        result = { regs.d })
                    {
                        regs.hl = 0x8000u
                        bus.memWriteByte(0x8000u, it)
                        givenCode { LD(D, !HL) }
                    },
                    "LD E, A" to TestCase(
                        cycles = 4,
                        size = 1,
                        result = { regs.e })
                    {
                        regs.a = it
                        givenCode { LD(E, A) }
                    },
                    "LD E, B" to TestCase(
                        cycles = 4,
                        size = 1,
                        result = { regs.e })
                    {
                        regs.b = it
                        givenCode { LD(E, B) }
                    },
                    "LD E, C" to TestCase(
                        cycles = 4,
                        size = 1,
                        result = { regs.e })
                    {
                        regs.c = it
                        givenCode { LD(E, C) }
                    },
                    "LD E, D" to TestCase(
                        cycles = 4,
                        size = 1,
                        result = { regs.e })
                    {
                        regs.d = it
                        givenCode { LD(E, D) }
                    },
                    "LD E, E" to TestCase(
                        cycles = 4,
                        size = 1,
                        result = { regs.e })
                    {
                        regs.e = it
                        givenCode { LD(E, E) }
                    },
                    "LD E, H" to TestCase(
                        cycles = 4,
                        size = 1,
                        result = { regs.e })
                    {
                        regs.h = it
                        givenCode { LD(E, H) }
                    },
                    "LD E, L" to TestCase(
                        cycles = 4,
                        size = 1,
                        result = { regs.e })
                    {
                        regs.l = it
                        givenCode { LD(E, L) }
                    },
                    "LD E, (HL)" to TestCase(
                        cycles = 7,
                        size = 1,
                        result = { regs.e })
                    {
                        regs.hl = 0x8000u
                        bus.memWriteByte(0x8000u, it)
                        givenCode { LD(E, !HL) }
                    },
                    "LD H, A" to TestCase(
                        cycles = 4,
                        size = 1,
                        result = { regs.h })
                    {
                        regs.a = it
                        givenCode { LD(H, A) }
                    },
                    "LD H, B" to TestCase(
                        cycles = 4,
                        size = 1,
                        result = { regs.h })
                    {
                        regs.b = it
                        givenCode { LD(H, B) }
                    },
                    "LD H, C" to TestCase(
                        cycles = 4,
                        size = 1,
                        result = { regs.h })
                    {
                        regs.c = it
                        givenCode { LD(H, C) }
                    },
                    "LD H, D" to TestCase(
                        cycles = 4,
                        size = 1,
                        result = { regs.h })
                    {
                        regs.d = it
                        givenCode { LD(H, D) }
                    },
                    "LD H, E" to TestCase(
                        cycles = 4,
                        size = 1,
                        result = { regs.h })
                    {
                        regs.e = it
                        givenCode { LD(H, E) }
                    },
                    "LD H, H" to TestCase(
                        cycles = 4,
                        size = 1,
                        result = { regs.h })
                    {
                        regs.h = it
                        givenCode { LD(H, H) }
                    },
                    "LD H, L" to TestCase(
                        cycles = 4,
                        size = 1,
                        result = { regs.h })
                    {
                        regs.l = it
                        givenCode { LD(H, L) }
                    },
                    "LD H, (HL)" to TestCase(
                        cycles = 7,
                        size = 1,
                        result = { regs.h })
                    {
                        regs.hl = 0x8000u
                        bus.memWriteByte(0x8000u, it)
                        givenCode { LD(H, !HL) }
                    },
                    "LD L, A" to TestCase(
                        cycles = 4,
                        size = 1,
                        result = { regs.l })
                    {
                        regs.a = it
                        givenCode { LD(L, A) }
                    },
                    "LD L, B" to TestCase(
                        cycles = 4,
                        size = 1,
                        result = { regs.l })
                    {
                        regs.b = it
                        givenCode { LD(L, B) }
                    },
                    "LD L, C" to TestCase(
                        cycles = 4,
                        size = 1,
                        result = { regs.l })
                    {
                        regs.c = it
                        givenCode { LD(L, C) }
                    },
                    "LD L, D" to TestCase(
                        cycles = 4,
                        size = 1,
                        result = { regs.l })
                    {
                        regs.d = it
                        givenCode { LD(L, D) }
                    },
                    "LD L, E" to TestCase(
                        cycles = 4,
                        size = 1,
                        result = { regs.l })
                    {
                        regs.e = it
                        givenCode { LD(L, E) }
                    },
                    "LD L, H" to TestCase(
                        cycles = 4,
                        size = 1,
                        result = { regs.l })
                    {
                        regs.h = it
                        givenCode { LD(L, H) }
                    },
                    "LD L, L" to TestCase(
                        cycles = 4,
                        size = 1,
                        result = { regs.l })
                    {
                        regs.l = it
                        givenCode { LD(L, L) }
                    },
                    "LD L, (HL)" to TestCase(
                        cycles = 7,
                        size = 1,
                        result = { regs.l })
                    {
                        regs.hl = 0x8000u
                        bus.memWriteByte(0x8000u, it)
                        givenCode { LD(L, !HL) }
                    },
                    "LD (BC), A" to TestCase(
                        cycles = 7,
                        size = 1,
                        result = { bus.memReadByte(0x8000u) }
                    ) {
                        regs.a = it
                        regs.bc = 0x8000u
                        givenCode { LD(!BC, A) }
                    },
                    "LD (DE), A" to TestCase(
                        cycles = 7,
                        size = 1,
                        result = { bus.memReadByte(0x8000u) }
                    ) {
                        regs.a = it
                        regs.de = 0x8000u
                        givenCode { LD(!DE, A) }
                    },
                    "LD A, (BC)" to TestCase(
                        cycles = 7,
                        size = 1,
                        result = { regs.a },
                    ) {
                        regs.bc = 0x8000u
                        mem[0x8000] = it.toByte()
                        givenCode { LD(A, !BC) }
                    },
                    "LD A, (DE)" to TestCase(
                        cycles = 7,
                        size = 1,
                        result = { regs.a },
                    ) {
                        regs.de = 0x8000u
                        mem[0x8000] = it.toByte()
                        givenCode { LD(A, !DE) }
                    },
                    "LD A, (NN)" to TestCase(
                        cycles = 13,
                        size = 3,
                        result = { regs.a },
                    ) {
                        mem[0x8000] = it.toByte()
                        givenCode { LD(A, !0x8000u) }
                    },
                    "LD (NN), A" to TestCase(
                        cycles = 13,
                        size = 3,
                        result = { bus.memReadByte(0x8000u) },
                    ) {
                        regs.a = it
                        givenCode { LD(!0x8000u, A) }
                    },
                    "LD (HL), N" to TestCase(
                        cycles = 10,
                        size = 2,
                        result = { bus.memReadByte(0x8000u) },
                    ) {
                        regs.hl = 0x8000u
                        givenCode { LD(!HL, it) }
                    },
                    "LD (HL), A" to TestCase(
                        cycles = 7,
                        size = 1,
                        result = { bus.memReadByte(0x8000u) })
                    {
                        regs.a = it
                        regs.hl = 0x8000u
                        givenCode { LD(!HL, A) }
                    },
                    "LD (HL), B" to TestCase(
                        cycles = 7,
                        size = 1,
                        result = { bus.memReadByte(0x8000u) })
                    {
                        regs.b = it
                        regs.hl = 0x8000u
                        givenCode { LD(!HL, B) }
                    },
                    "LD (HL), C" to TestCase(
                        cycles = 7,
                        size = 1,
                        result = { bus.memReadByte(0x8000u) })
                    {
                        regs.c = it
                        regs.hl = 0x8000u
                        givenCode { LD(!HL, C) }
                    },
                    "LD (HL), D" to TestCase(
                        cycles = 7,
                        size = 1,
                        result = { bus.memReadByte(0x8000u) })
                    {
                        regs.d = it
                        regs.hl = 0x8000u
                        givenCode { LD(!HL, D) }
                    },
                    "LD (HL), E" to TestCase(
                        cycles = 7,
                        size = 1,
                        result = { bus.memReadByte(0x8000u) })
                    {
                        regs.e = it
                        regs.hl = 0x8000u
                        givenCode { LD(!HL, E) }
                    },
                    "LD (HL), H" to TestCase(
                        cycles = 7,
                        size = 1,
                        result = { bus.memReadByte(0x8080u.toUShort().setHigh(it)) })
                    {
                        regs.hl = 0x8080u
                        regs.h = it
                        givenCode { LD(!HL, H) }
                    },
                    "LD (HL), L" to TestCase(
                        cycles = 7,
                        size = 1,
                        result = { bus.memReadByte(0x8080u.toUShort().setLow(it)) })
                    {
                        regs.hl = 0x8080u
                        regs.l = it
                        givenCode { LD(!HL, L) }
                    },
                )
            ) { (cycles, size, result, prepare) -> behavesLike { value: UByte, prevFlags ->
                prepare(value)
                whenProcessorRuns()
                expect(cycles = cycles, pc = size.toUShort(), flags = prevFlags)
                result(value) shouldBe value
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
                        givenCode { LD (BC, it) }
                    },
                    "LD DE, NN" to TestCase(
                        cycles = 10,
                        size = 3,
                        result = { regs.de },
                    ) {
                        givenCode { LD(DE, it) }
                    },
                    "LD HL, NN" to TestCase(
                        cycles = 10,
                        size = 3,
                        result = { regs.hl },
                    ) {
                        givenCode { LD(HL, it) }
                    },
                    "LD SP, NN" to TestCase(
                        cycles = 10,
                        size = 3,
                        result = { regs.sp },
                    ) {
                        givenCode { LD(SP, it) }
                    },
                    "LD (NN), HL" to TestCase(
                        cycles = 16,
                        size = 3,
                        result = { bus.memReadWord(0x8000u) },
                    ) {
                        regs.hl = it
                        givenCode {
                            LD(!0x8000u.toUShort(), HL)
                        }
                    },
                    "LD HL, (NN)" to TestCase(
                        cycles = 16,
                        size = 3,
                        result = { regs.hl },
                    ) {
                        givenMemoryWord(0x8000u, it)
                        givenCode {
                            LD(HL, !0x8000u.toUShort())
                        }
                    },
                    "LD SP, HL" to TestCase(
                        cycles = 6,
                        size = 1,
                        result = { regs.sp },
                    ) {
                        given(hl = it)
                        givenCode { LD(SP, HL) }
                    },
                )
            ) { (cycles, size, result, prepare) -> behavesLike { value: UShort, prevFlags ->
                prepare(value)
                whenProcessorRuns()
                expect(cycles = cycles, pc = size.toUShort(), flags = prevFlags)
                result() shouldBe value
            } }
        }

        context("EX") {
            data class TestCase(
                val cycles: Int,
                val size: Int,
                val affectsFlags: Boolean = false,
                val result: suspend ProcessorBehavior.() -> Pair<UShort, UShort>,
                val prepare: suspend ProcessorBehavior.(UShort, UShort) -> Unit,
            )

            withData(mapOf(
                "EX AF, AF'" to TestCase(
                    cycles = 4,
                    size = 1,
                    affectsFlags = true,
                    result = { Pair(regs.af, regs.`af'`) }
                ) { a, b ->
                    regs.af = a
                    regs.`af'` = b
                    givenCode { EX(AF, `AF'`) }
                },
                "EX (SP), HL'" to TestCase(
                    cycles = 19,
                    size = 1,
                    result = { Pair(bus.memReadWord(0x8000u), regs.hl) }
                ) { a, b ->
                    regs.sp = 0x8000u
                    givenMemoryWord(0x8000u, a)
                    regs.hl = b
                    givenCode { EX(!SP, HL) }
                },
                "EX DE, HL'" to TestCase(
                    cycles = 4,
                    size = 1,
                    result = { Pair(regs.de, regs.hl) }
                ) { a, b ->
                    regs.de = a
                    regs.hl = b
                    givenCode { EX(DE, HL) }
                },
            )) { (cycles, size, affectsFlags, result, prepare) -> behavesLike { a: UShort, b: UShort, prevFlags ->
                prepare(a, b)
                whenProcessorRuns()
                expect(cycles = cycles, pc = size.toUShort())
                if (!affectsFlags) {
                    expect(flags = prevFlags)
                }
                val (resA, resB) = result()
                resA shouldBe b
                resB shouldBe a
            }}
        }

        test("EXX") { behavesLike { prevFlags ->
            given(
                bc = 0x0101u,
                de = 0x0202u,
                hl = 0x0303u,
                `bc'` = 0x0a0au,
                `de'` = 0x0b0bu,
                `hl'` = 0x0c0cu,
            )
            givenCode { EXX }
            whenProcessorRuns()
            expect(
                cycles = 4,
                pc = 0x0001u,
                flags = prevFlags,
                bc = 0x0a0au,
                de = 0x0b0bu,
                hl = 0x0c0cu,
                `bc'` = 0x0101u,
                `de'` = 0x0202u,
                `hl'` = 0x0303u,
            )
        }}

        context("POP") {
            data class TestCase(
                val cycles: Int,
                val size: Int,
                val touchFlags: Boolean = false,
                val result: suspend ProcessorBehavior.() -> UShort,
                val prepare: suspend ProcessorBehavior.(UShort) -> Unit,
            )

            withData(
                mapOf(
                    "POP BC" to TestCase(
                        cycles = 10,
                        size = 1,
                        result = { regs.bc },
                    ) {
                        regs.sp = 0xFFFEu
                        givenMemoryWord(0xFFFEu, it)
                        givenCode { POP (BC) }
                    },
                    "POP DE" to TestCase(
                        cycles = 10,
                        size = 1,
                        result = { regs.de },
                    ) {
                        regs.sp = 0xFFFEu
                        givenMemoryWord(0xFFFEu, it)
                        givenCode { POP (DE) }
                    },
                    "POP HL" to TestCase(
                        cycles = 10,
                        size = 1,
                        result = { regs.hl },
                    ) {
                        regs.sp = 0xFFFEu
                        givenMemoryWord(0xFFFEu, it)
                        givenCode { POP (HL) }
                    },
                    "POP AF" to TestCase(
                        cycles = 10,
                        size = 1,
                        touchFlags = true,
                        result = { regs.af },
                    ) {
                        regs.sp = 0xFFFEu
                        givenMemoryWord(0xFFFEu, it)
                        givenCode { POP (AF) }
                    },
                )
            ) { (cycles, size, touchFlags, result, prepare) -> behavesLike { value: UShort, prevFlags ->
                prepare(value)
                whenProcessorRuns()
                expect(cycles = cycles, pc = size.toUShort(), sp = 0x0000u)
                if (!touchFlags) {
                    expect(flags = prevFlags)
                }
                result() shouldBe value
            }}
        }
        
        context("PUSH") {
            data class TestCase(
                val cycles: Int,
                val size: Int,
                val touchFlags: Boolean = false,
                val prepare: suspend ProcessorBehavior.(UShort) -> Unit,
            )

            withData(
                mapOf(
                    "PUSH BC" to TestCase(
                        cycles = 11,
                        size = 1,
                    ) {
                        regs.bc = it
                        givenCode { PUSH (BC) }
                    },
                    "PUSH DE" to TestCase(
                        cycles = 11,
                        size = 1,
                    ) {
                        regs.de = it
                        givenCode { PUSH (DE) }
                    },
                    "PUSH HL" to TestCase(
                        cycles = 11,
                        size = 1,
                    ) {
                        regs.hl = it
                        givenCode { PUSH (HL) }
                    },
                    "PUSH AF" to TestCase(
                        cycles = 11,
                        size = 1,
                        touchFlags = true,
                    ) {
                        regs.af = it
                        givenCode { PUSH (AF) }
                    },
                )
            ) { (cycles, size, touchFlags, prepare) -> behavesLike { value: UShort, prevFlags ->
                given(sp = 0xFFFFu)
                prepare(value)
                whenProcessorRuns()
                expect(cycles = cycles, pc = size.toUShort(), sp = 0xFFFDu)
                if (!touchFlags) {
                    expect(flags = prevFlags)
                }

                expectPushedWord(0xFFFFu, value)
            }}
        }
    }

    context("Input and output") {
        data class TestCase(
            val cycles: Int,
            val size: Int,
            val result: suspend ProcessorBehavior.() -> UByte,
            val prepare: suspend ProcessorBehavior.(UByte) -> Unit
        )

        withData(mapOf(
            "IN A, (N)" to TestCase(
                cycles = 11,
                size = 2,
                result = { regs.a },
                prepare = {
                    bus.ioWriteByte(0x42u, it)
                    givenCode { IN(A, !0x42u.toUByte()) }
                },
            ),
            "OUT (N), A" to TestCase(
                cycles = 11,
                size = 2,
                result = { bus.ioReadByte(0x42u) },
                prepare = {
                    regs.a = it
                    givenCode { OUT(!0x42u.toUByte(), A) }
                },
            ),
        )) { (cycles, size, result, prepare) -> behavesLike { value: UByte, prevFlags ->
            prepare(value)
            whenProcessorRuns()
            expect(cycles = cycles, pc = size.toUShort(), flags = prevFlags)
            result() shouldBe value
        }}
    }
})


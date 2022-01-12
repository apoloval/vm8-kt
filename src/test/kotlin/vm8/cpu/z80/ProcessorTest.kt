package vm8.cpu.z80

import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.property.Arb
import vm8.cpu.z80.Assembler.*
import vm8.data.*

class ProcessorTest : FunSpec({

    context("General purpose arithmetic and CPU control") {
        test("NOP") { behavesLike { prevFlags ->
            whenProcessorRuns { NOP }
            expect(cycles = 4, pc = 0x0001u, flags = prevFlags)
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
                val result: suspend ProcessorBehavior.() -> UByte,
                val prepare: suspend ProcessorBehavior.(UByte) -> Unit,
            )

            withData(
                mapOf(
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
                )
            ) { (cycles, size, result, prepare) -> behavesLike { value: UByte, prevFlags ->
                prepare(value)
                whenProcessorRuns()
                expect(cycles, pc = size.toUShort(), prevFlags) {
                    result() shouldBe value
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


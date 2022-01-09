package vm8.cpu.z80

import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe
import vm8.data.*

class ProcessorTest : FunSpec({

    context("General purpose arithmetic and CPU control") {
        test("NOP") { behavesLike { flags ->
            whenProcessorRuns { NOP }
            expect(cycles = 4, pc = 0x0001u, flags)
        }}
    }

    context("Arithmetic and logic") {
        context("ADD 16-bit") {
            data class TestCase(
                val cycles: Int,
                val size: Int,
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
            )) { (cycles, size, result, prepare) -> behavesLike { a: UShort, b: UShort, flags ->
                prepare(a, b)
                whenProcessorRuns()
                expect(cycles, pc = size.toUShort()) {
                    result() shouldBe (a + b).toUShort()

                    regs.f.bit(0) shouldBe flagActiveOnCarry(a, result(), 0xFFFF)
                    regs.f.bit(1) shouldBe false
                    regs.f.bit(2) shouldBe flags.bit(2)
                    regs.f.bit(3) shouldBe result().high().bit(3)
                    regs.f.bit(4) shouldBe flagActiveOnCarry(a, result(), 0x0FFF)
                    regs.f.bit(5) shouldBe result().high().bit(5)
                    regs.f.bit(6) shouldBe flags.bit(6)
                    regs.f.bit(7) shouldBe flags.bit(7)
                }
            }}
        }

        context("DEC 8-bit") {
            data class TestCase(
                val cycles: Int,
                val size: Int,
                val result: ProcessorBehavior.() -> UByte,
                val prepare: ProcessorBehavior.(UByte) -> Unit
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
                )
            ) { (cycles, size, result, prepare) -> behavesLike { value: UByte, flags ->
                prepare(value)
                whenProcessorRuns()

                expect(cycles, pc = size.toUShort()) {
                    result() shouldBe value.dec()
                    regs.f.bit(0) shouldBe flags.bit(0)
                    regs.f.bit(1) shouldBe true
                    regs.f.bit(2) shouldBe flagActiveOnUnderflow(value, result())
                    regs.f.bit(3) shouldBe result().bit(3)
                    regs.f.bit(4) shouldBe flagActiveOnBorrow(value, result(), mask = 0x0F)
                    regs.f.bit(5) shouldBe result().bit(5)
                    regs.f.bit(6) shouldBe result().isZero()
                    regs.f.bit(7) shouldBe result().isNegative()
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
            )) { (cycles, size, result, prepare) -> behavesLike { value: UShort, flags ->
                prepare(value)
                whenProcessorRuns()
                expect(cycles, pc = size.toUShort(), flags) {
                    result() shouldBe value.dec()
                }
            }}
        }

        context("INC 8-bit") {
            data class TestCase(
                val cycles: Int,
                val size: Int,
                val result: ProcessorBehavior.() -> UByte,
                val prepare: ProcessorBehavior.(UByte) -> Unit
            )

            withData(
                mapOf(
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
                )
            ) { (cycles, size, result, prepare) -> behavesLike { value: UByte, flags ->
                prepare(value)
                whenProcessorRuns()

                expect(cycles, pc = size.toUShort()) {
                    result() shouldBe value.inc()
                    regs.f.bit(0) shouldBe flags.bit(0)
                    regs.f.bit(1) shouldBe false
                    regs.f.bit(2) shouldBe flagActiveOnOverflow(value, result())
                    regs.f.bit(3) shouldBe result().bit(3)
                    regs.f.bit(4) shouldBe flagActiveOnCarry(value, result(), mask = 0x0F)
                    regs.f.bit(5) shouldBe result().bit(5)
                    regs.f.bit(6) shouldBe result().isZero()
                    regs.f.bit(7) shouldBe result().isNegative()
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
            )) { (cycles, size, result, prepare) -> behavesLike { value: UShort, flags ->
                prepare(value)
                whenProcessorRuns()
                expect(cycles, pc = size.toUShort(), flags) {
                    result() shouldBe value.inc()
                }
            }}
        }
    }

    context("Rotate and shift") {
        test("RLA") { behavesLike { value: UByte, flags ->
            val inCarry = Flag.C.isSet(flags)
            given { regs.a = value }
            whenProcessorRuns { RLA }
            val (xval, outCarry) = value.rotateLeft(inCarry)
            expectRotate(xval, outCarry, flags)
        }}

        test("RLCA") { behavesLike { value: UByte, flags ->
            given { regs.a = value }
            whenProcessorRuns { RLCA }
            val (xval, carry) = value.rotateLeft()
            expectRotate(xval, carry, flags)
        }}

        test("RRA") { behavesLike { value: UByte, flags ->
            val inCarry = Flag.C.isSet(flags)
            given { regs.a = value }
            whenProcessorRuns { RRA }
            val (xval, outCarry) = value.rotateRight(inCarry)
            expectRotate(xval, outCarry, flags)
        }}

        test("RRCA") { behavesLike { value: UByte, flags ->
            given { regs.a = value }
            whenProcessorRuns { RRCA }
            val (xval, carry) = value.rotateRight()
            expectRotate(xval, carry, flags)
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
                "JR NZ, N" to TestCase(cond = { !regs.f.bit(6) }) {
                    mem.asm { JR(NZ, it)}
                },
            )) { (cond, prepare) -> behavesLike { n: Byte, flags ->
                prepare(n)
                whenProcessorRuns()
                if (cond()) {
                    expect(cycles = 12, pc = 0x0000.toUShort().increment(n), flags)
                } else {
                    expect(cycles = 7, pc = 0x0002u, flags)
                }
            }}
        }

        test("DJNZ") { behavesLike { value: UByte, flags ->
            given { regs.b = value }
            whenProcessorRuns { DJNZ(0x42) }
            if (value == 1u.toUByte()) {
                expect(cycles = 8, pc = 0x0002u, flags) {
                    regs.b shouldBe 0x00u
                }
            } else {
                expect(cycles = 13, pc = 0x0042u, flags) {
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
                val result: ProcessorBehavior.() -> UByte,
                val prepare: ProcessorBehavior.(UByte) -> Unit,
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
                    "LD (BC), A" to TestCase(
                        cycles = 7,
                        size = 1,
                        result = { mem[0x8000].toUByte() }
                    ) {
                        regs.a = it
                        regs.bc = 0x8000u
                        mem.asm { LD(!BC, A) }
                    },
                    "LD (DE), A" to TestCase(
                        cycles = 7,
                        size = 1,
                        result = { mem[0x8000].toUByte() }
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
                )
            ) { (cycles, size, result, prepare) -> behavesLike { value: UByte, flags ->
                prepare(value)
                whenProcessorRuns()
                expect(cycles, pc = size.toUShort(), flags) {
                    result() shouldBe value
                }
            } }
        }

        context("LD 16-bit") {
            data class TestCase(
                val cycles: Int,
                val size: Int,
                val result: suspend ProcessorBehavior.() -> UShort,
                val prepare: ProcessorBehavior.(UShort) -> Unit,
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
                )
            ) { (cycles, size, result, prepare) -> behavesLike { value: UShort, flags ->
                given { prepare(value) }
                whenProcessorRuns()
                expect(cycles, pc = size.toUShort(), flags) { result() shouldBe value }
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


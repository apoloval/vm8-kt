package vm8.cpu.z80

import io.kotest.core.spec.style.*
import io.kotest.matchers.*

import vm8.cpu.z80.*

internal class AssemblerTest : FunSpec({

    test("DB directive") {
        val bytes = asm {
            DB(0x12)
            DB(0x34)
            DB(0xAB)
            DB(0xCD)
        }
        bytes[0x0000] shouldBe 0x12.toByte()
        bytes[0x0001] shouldBe 0x34.toByte()
        bytes[0x0002] shouldBe 0xAB.toByte()
        bytes[0x0003] shouldBe 0xCD.toByte()
    }

    test("DW directive") {
        val bytes = asm {
            DW(0x1234)
            DW(0xABCD)
        }
        bytes[0x0000] shouldBe 0x34.toByte()
        bytes[0x0001] shouldBe 0x12.toByte()
        bytes[0x0002] shouldBe 0xCD.toByte()
        bytes[0x0003] shouldBe 0xAB.toByte()
    }

    test("label directive") {
        val bytes = asm {
            DB(0x12)
            LABEL("foobar")
            DB(0x34)
            DW(+"foobar")
        }
        bytes[0x0000] shouldBe 0x12.toByte()
        bytes[0x0001] shouldBe 0x34.toByte()
        bytes[0x0002] shouldBe 0x01.toByte()
        bytes[0x0003] shouldBe 0x00.toByte()
    }

    test("NOP instruction") {
        val bytes = asm {
            DB(0xFF)
            NOP
            DB(0xFF)
        }
        bytes[0x0000] shouldBe 0xFF.toByte()
        bytes[0x0001] shouldBe 0x00.toByte()
        bytes[0x0002] shouldBe 0xFF.toByte()
    }

    test("DEC for 8-bit register") {
        val bytes = asm {
            DEC(A)
            DEC(B)
            DEC(C)
            DEC(D)
            DEC(H)
            DEC(L)
        }
        bytes[0x0000] shouldBe 0x3D.toByte()
        bytes[0x0001] shouldBe 0x05.toByte()
        bytes[0x0002] shouldBe 0x0D.toByte()
        bytes[0x0003] shouldBe 0x15.toByte()
        bytes[0x0004] shouldBe 0x25.toByte()
        bytes[0x0005] shouldBe 0x2D.toByte()
    }

    test("INC for 8-bit register") {
        val bytes = asm {
            INC(A)
            INC(B)
            INC(C)
            INC(D)
            INC(H)
            INC(L)
        }
        bytes[0x0000] shouldBe 0x3C.toByte()
        bytes[0x0001] shouldBe 0x04.toByte()
        bytes[0x0002] shouldBe 0x0C.toByte()
        bytes[0x0003] shouldBe 0x14.toByte()
        bytes[0x0004] shouldBe 0x24.toByte()
        bytes[0x0005] shouldBe 0x2C.toByte()
    }

    test("JP NN") {
        val bytes = asm {
            JP(0xABCD)
        }
        bytes[0x0000] shouldBe 0xC3.toByte()
        bytes[0x0001] shouldBe 0xCD.toByte()
        bytes[0x0002] shouldBe 0xAB.toByte()
    }
})

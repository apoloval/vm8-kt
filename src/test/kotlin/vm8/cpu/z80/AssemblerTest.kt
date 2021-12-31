package vm8.cpu.z80

import io.kotest.core.spec.style.*
import io.kotest.matchers.*

import vm8.cpu.z80.*

internal class AssemblerTest : FunSpec({

    val bytes = ByteArray(64*1024)

    test("DB directive") {
        bytes.asm {
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
        bytes.asm {
            DW(0x1234)
            DW(0xABCD)
        }
        bytes[0x0000] shouldBe 0x34.toByte()
        bytes[0x0001] shouldBe 0x12.toByte()
        bytes[0x0002] shouldBe 0xCD.toByte()
        bytes[0x0003] shouldBe 0xAB.toByte()
    }

    test("label directive") {
        bytes.asm {
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

    test("DEC for 8-bit register") {
        bytes.asm {
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

    test("EX") {
        bytes.asm {
            EX(AF, `AF'`)
        }
        bytes[0x0000] shouldBe 0x08.toByte()
    }

    test("INC for 8-bit register") {
        bytes.asm {
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
        bytes.asm {
            JP(0xABCD)
        }
        bytes[0x0000] shouldBe 0xC3.toByte()
        bytes[0x0001] shouldBe 0xCD.toByte()
        bytes[0x0002] shouldBe 0xAB.toByte()
    }

    test("NOP instruction") {
        bytes.asm {
            DB(0xFF)
            NOP
            DB(0xFF)
        }
        bytes[0x0000] shouldBe 0xFF.toByte()
        bytes[0x0001] shouldBe 0x00.toByte()
        bytes[0x0002] shouldBe 0xFF.toByte()
    }

    test("RLCA instruction") {
        bytes.asm {
            RLCA
        }
        bytes[0x0000] shouldBe 0x07.toByte()
    }
})

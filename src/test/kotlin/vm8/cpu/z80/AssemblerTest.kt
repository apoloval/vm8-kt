package vm8.cpu.z80

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class AssemblerTest : FunSpec({
    context("Symbol relocation") {
        test("Word from symbol") {
            val buffer = ByteArray(1024)
            buffer.asm {
                /* 0x0000 */ JP("foobar")
                /* 0x0003 */ NOP
                /* 0x0004 */ +"foobar"
                /* 0x0004 */ NOP
            }
            buffer[0x0001] shouldBe 0x04
            buffer[0x0002] shouldBe 0x00
        }

        context("Byte distance to symbol") {
            test("Positive") {
                val buffer = ByteArray(1024)
                buffer.asm {
                    /* 0x0000 */ JR("foobar")
                    /* 0x0002 */ NOP
                    /* 0x0003 */ +"foobar"
                    /* 0x0003 */ NOP
                }
                buffer[0x0001] shouldBe 3
            }
            test("Negative") {
                val buffer = ByteArray(1024)
                buffer.asm {
                    /* 0x0000 */ +"foobar"
                    /* 0x0000 */ NOP
                    /* 0x0001 */ NOP
                    /* 0x0002 */ JR("foobar")
                }
                buffer[0x0003] shouldBe -2
            }
        }
    }
})
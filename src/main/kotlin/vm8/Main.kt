@file:OptIn(ExperimentalUnsignedTypes::class)
@file:Suppress("EXPERIMENTAL_IS_NOT_ENABLED")

package vm8

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import vm8.cpu.z80.Assembler.*
import vm8.cpu.z80.MinimalSystem
import vm8.cpu.z80.Processor
import vm8.cpu.z80.asm
import java.text.NumberFormat
import kotlin.system.measureTimeMillis

const val TOTAL_INST = 300_000_000

fun main() {
    val sys = MinimalSystem()
    sys.memory.asm {
            DI
        +"MAIN"
            LD(HL, "STR1")
            LD(DE, "STR2")
            CALL("CMPSTRLEN")
            JR("MAIN")
        +"STR1"
            DB(*"Hello".encodeToByteArray().toUByteArray())
            DB(0)
        +"STR2"
            DB(*"Goodbye".encodeToByteArray().toUByteArray())
            DB(0)
        +"CMPSTRLEN"
            PUSH(HL)
            PUSH(DE)
            EX(DE, HL)
            CALL("STRLEN")
            LD(B, C)

            EX(DE, HL)
            CALL("STRLEN")
            LD(A, B)
            CP(C)
            POP(DE)
            POP(HL)
            RET
        +"STRLEN"
            LD(B, 0u)
        +"STRLENLOOP"
            LD(A, !HL)
            CP(0u)
            RET(Z)
            INC(HL)
            INC(B)
            JR("STRLENLOOP")
    }

    val cpu = Processor(sys)

    val elapsed = measureTimeMillis {
        runBlocking {
            launch {
                repeat (TOTAL_INST) {
                    cpu.run()
                }
            }
        }
    }
    println("Executed ${NumberFormat.getInstance().format(cpu.cycles)} cycles in $elapsed ms: ${cpu.cycles / (1000* elapsed)} Mhz")
}

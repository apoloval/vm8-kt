package vm8.cpu.z80

import io.kotest.assertions.*
import io.kotest.core.spec.style.*
import io.kotest.data.row
import io.kotest.data.forAll as forAllData
import io.kotest.matchers.*
import io.kotest.property.*
import io.kotest.property.arbitrary.boolean

import vm8.data.*

class ProcessorTest : FunSpec({

    test("NOP") { behavesLike { flags ->
        whenProcessorRuns { NOP }
        expect(cycles = 4, pc = Word(0x0001), flags)
    }}

    test("LD BC, NN") { behavesLike { flags ->
        whenProcessorRuns { LD(BC, 0xABCD) }
        expect(cycles = 10, pc = Word(0x0003), flags) { regs.bc shouldBe Word(0xABCD) }
    }}

    test("LD (BC), A") { behavesLike { value: Octet, flags ->
        given { 
            regs.a = value 
            regs.bc = Word(0x8000)
        }
        whenProcessorRuns { LD(!BC, A) }
        expect(cycles = 7, pc = Word(0x0001), flags) {
            mem[0x8000] shouldBe value.toByte()
        }
    }}

    test("INC BC") { behavesLike { value: Word, flags ->
        given { regs.bc = value }
        whenProcessorRuns { INC(BC) }
        expect(cycles = 6, pc = Word(0x0001), flags) {
            regs.bc shouldBe value.inc()
        }
    }}

    test("INC B") { behavesLike { value: Octet, flags ->
        given { regs.b = value }
        whenProcessorRuns { INC(B) }
        expectInc(value, regs.b, flags)
    }}

    test("DEC B") { behavesLike { value: Octet, flags ->
        given { regs.b = value }
        whenProcessorRuns { DEC(B) }
        expectDec(value, regs.b, flags)
    }}

    test("LD B, N") { behavesLike { flags ->
        whenProcessorRuns { LD(B, 0x42) }
        expect(cycles = 7, pc = Word(0x0002), flags) {
            regs.b shouldBe Octet(0x42)
        }
    }}

    test("RLCA") { behavesLike { value: Octet, flags ->
        given { regs.a = value }
        whenProcessorRuns { RLCA }
        var (xval, carry) = value.rotateLeft()
        expectRotate(xval, carry, flags)
    }}

    test("EX AF, AF'") { behavesLike { _ -> 
        given {
            regs.af = Word(0xABCD)
            regs.`af'` = Word(0x1234)
        }
        whenProcessorRuns { EX(AF, `AF'`) }
        expect(cycles = 4, pc = Word(0x0001)) {
            regs.af shouldBe Word(0x1234)
            regs.`af'` shouldBe Word(0xABCD)
        }
    }}
})

suspend fun behavesLike(f: suspend ProcessorBehavior.() -> Unit) {
    val b = ProcessorBehavior()
    b.f()
}

suspend fun behavesLike(f: suspend ProcessorBehavior.(flags: Octet) -> Unit) {
    checkAll(OctetArb) { flags -> 
        val b = ProcessorBehavior()
        b.cpu.regs.f = flags
        b.f(flags)
    }
}

inline suspend fun<reified T> behavesLike(crossinline f: suspend ProcessorBehavior.(a: T, flags: Octet) -> Unit) {
    val gen = generatorForClass<T>(T::class)
    checkAll(gen, OctetArb) { a, flags -> 
        val b = ProcessorBehavior()
        b.cpu.regs.f = flags
        b.f(a, flags)
    }
}

class ProcessorBehavior {
    val sys = MinimalSystem()
    val cpu = Processor(sys)

    val regs by cpu::regs
    val mem by sys::memory

    var givenCycles: Int = 0

    suspend fun whenProcessorRuns(org: Int = 0x0000, f: Assembler.() -> Unit) {
        sys.memory.asm(org, f)
        givenCycles = cpu.run()
    }

    fun given(f: ProcessorBehavior.() -> Unit) {
        this.f()
    }

    fun expect(cycles: Int? = null, pc: Word? = null, flags: Octet? = null, f: ProcessorBehavior.() -> Unit = {}) {
        if (cycles != null)
            givenCycles shouldBe cycles
        if (pc != null) 
            cpu.regs.pc shouldBe pc
        if (flags != null)
            cpu.regs.f shouldBe flags
        this.f()
    }

    fun expectInc(from: Octet, to: Octet, flags: Octet) = expect(cycles = 4, pc = Word(0x0001)) {
        to shouldBe from.inc()

        regs.f.bit(0) shouldBe flags.bit(0)
        regs.f.bit(1) shouldBe false
        regs.f.bit(2) shouldBe flagActiveOnOverflow(from, to)
        regs.f.bit(3) shouldBe to.bit(3)
        regs.f.bit(4) shouldBe flagActiveOnCarry(from, to, mask = 0x0F)
        regs.f.bit(5) shouldBe to.bit(5)
        regs.f.bit(6) shouldBe to.isZero()
        regs.f.bit(7) shouldBe to.isNegative()
    }

    fun expectDec(from: Octet, to: Octet, flags: Octet) = expect(cycles = 4, pc = Word(0x0001)) {
        to shouldBe from.dec()

        regs.f.bit(0) shouldBe flags.bit(0)
        regs.f.bit(1) shouldBe true
        regs.f.bit(2) shouldBe flagActiveOnUnderflow(from, to)
        regs.f.bit(3) shouldBe to.bit(3)
        regs.f.bit(4) shouldBe flagActiveOnBorrow(from, to, mask = 0x0F)
        regs.f.bit(5) shouldBe to.bit(5)
        regs.f.bit(6) shouldBe to.isZero()
        regs.f.bit(7) shouldBe to.isNegative()
    }

    fun expectRotate(xval: Octet, carry: Boolean, flags: Octet) = expect(cycles = 4, pc = Word(0x0001)) {
        regs.a shouldBe xval

        regs.f.bit(0) shouldBe carry
        regs.f.bit(1) shouldBe false
        regs.f.bit(2) shouldBe flags.bit(2)
        regs.f.bit(3) shouldBe xval.bit(3)
        regs.f.bit(4) shouldBe false
        regs.f.bit(5) shouldBe xval.bit(5)
        regs.f.bit(6) shouldBe flags.bit(6)
        regs.f.bit(7) shouldBe flags.bit(7)
    }

    fun flagActiveOnOverflow(a: Int, c: Int): Boolean {
        val b = c - a
        return ((a xor b xor 0x80) and (b xor c) and 0x80) != 0
    }
    
    fun flagActiveOnOverflow(a: Octet, b: Octet) = flagActiveOnOverflow(a.toInt(), b.toInt())

    fun flagActiveOnUnderflow(a: Int, c: Int): Boolean {
        val b = a - c
        return ((a xor b) and ((a xor c) and 0x80)) != 0
    }
    
    fun flagActiveOnUnderflow(a: Octet, b: Octet) = flagActiveOnUnderflow(a.toInt(), b.toInt())

    fun flagActiveOnCarry(a: Int, c: Int, mask: Int): Boolean = (a and mask) > (c and mask)

    fun flagActiveOnCarry(a: Octet, c: Octet, mask: Int): Boolean = flagActiveOnCarry(a.toInt(), c.toInt(), mask)

    fun flagActiveOnBorrow(a: Int, c: Int, mask: Int): Boolean = (a and mask) < (c and mask)

    fun flagActiveOnBorrow(a: Octet, c: Octet, mask: Int): Boolean = flagActiveOnBorrow(a.toInt(), c.toInt(), mask)
}
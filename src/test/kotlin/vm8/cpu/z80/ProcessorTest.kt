package vm8.cpu.z80

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.checkAll
import vm8.data.*

class ProcessorTest : FunSpec({

    test("NOP") { behavesLike { flags ->
        whenProcessorRuns { NOP }
        expect(cycles = 4, pc = 0x0001u, flags)
    }}

    test("LD BC, NN") { behavesLike { flags ->
        whenProcessorRuns { LD(BC, 0xABCD) }
        expect(cycles = 10, pc = 0x0003u, flags) { regs.bc shouldBe 0xABCDu }
    }}

    test("LD (BC), A") { behavesLike { value: UByte, flags ->
        given { 
            regs.a = value 
            regs.bc = 0x8000u
        }
        whenProcessorRuns { LD(!BC, A) }
        expect(cycles = 7, pc = 0x0001u, flags) {
            mem[0x8000] shouldBe value.toByte()
        }
    }}

    test("INC BC") { behavesLike { value: UShort, flags ->
        given { regs.bc = value }
        whenProcessorRuns { INC(BC) }
        expect(cycles = 6, pc = 0x0001u, flags) {
            regs.bc shouldBe value.inc()
        }
    }}

    test("INC B") { behavesLike { value: UByte, flags ->
        given { regs.b = value }
        whenProcessorRuns { INC(B) }
        expectInc(value, regs.b, flags)
    }}

    test("DEC B") { behavesLike { value: UByte, flags ->
        given { regs.b = value }
        whenProcessorRuns { DEC(B) }
        expectDec(value, regs.b, flags)
    }}

    test("LD B, N") { behavesLike { flags ->
        whenProcessorRuns { LD(B, 0x42) }
        expect(cycles = 7, pc = 0x0002u, flags) {
            regs.b shouldBe 0x42u
        }
    }}

    test("RLCA") { behavesLike { value: UByte, flags ->
        given { regs.a = value }
        whenProcessorRuns { RLCA }
        var (xval, carry) = value.rotateLeft()
        expectRotate(xval, carry, flags)
    }}

    test("EX AF, AF'") { behavesLike { _ -> 
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

    test("ADD HL, BC") { behavesLike { a: UShort, b: UShort, flags ->
        given {
            regs.hl = a
            regs.bc = b
        }
        whenProcessorRuns { ADD(HL, BC) }
        expectAdd16(regs.hl, a, b, flags)
    }}

    test("LD A, (BC)") { behavesLike { flags -> 
        given {
            regs.bc = 0xABCDu
            mem[0xABCD] = 0x42.toByte()
        }
        whenProcessorRuns { LD(A, !BC) }
        expect(cycles = 7, pc = 0x0001u, flags) {
            regs.a shouldBe 0x42u
        }
    }}

    test("DEC BC") { behavesLike { value: UShort, flags -> 
        given { regs.bc = value }
        whenProcessorRuns { DEC(BC) }
        expect(cycles = 6, pc = 0x0001u, flags) {
            regs.bc shouldBe value.dec()
        }
    }}

    test("INC C") { behavesLike { value: UByte, flags -> 
        given { regs.c = value }
        whenProcessorRuns { INC(C) }
        expectInc(value, regs.c, flags)
    }}

    test("DEC C") { behavesLike { value: UByte, flags -> 
        given { regs.c = value }
        whenProcessorRuns { DEC(C) }
        expectDec(value, regs.c, flags)
    }}

    test("LD C, N") { behavesLike { flags ->
        whenProcessorRuns { LD(C, 0x42) }
        expect(cycles = 7, pc = 0x0002u, flags) {
            regs.c shouldBe 0x42u
        }
    }}

    test("RRCA") { behavesLike { value: UByte, flags ->
        given { regs.a = value }
        whenProcessorRuns { RRCA }
        var (xval, carry) = value.rotateRight()
        expectRotate(xval, carry, flags)
    }}

})

suspend fun behavesLike(f: suspend ProcessorBehavior.() -> Unit) {
    val b = ProcessorBehavior()
    b.f()
}

suspend fun behavesLike(f: suspend ProcessorBehavior.(flags: UByte) -> Unit) {
    checkAll<UByte> { flags ->
        val b = ProcessorBehavior()
        b.cpu.regs.f = flags
        b.f(flags)
    }
}

inline suspend fun<reified T> behavesLike(crossinline f: suspend ProcessorBehavior.(a: T, flags: UByte) -> Unit) {
    checkAll<T, UByte> { a, flags ->
        val b = ProcessorBehavior()
        b.cpu.regs.f = flags
        b.f(a, flags)
    }
}

inline suspend fun<reified T1, reified T2> behavesLike(crossinline f: suspend ProcessorBehavior.(a: T1, b: T2, flags: UByte) -> Unit) {
    checkAll<T1, T2, UByte> { a, b, flags ->
        val behav = ProcessorBehavior()
        behav.cpu.regs.f = flags
        behav.f(a, b, flags)
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

    fun expect(cycles: Int? = null, pc: Addr? = null, flags: UByte? = null, f: ProcessorBehavior.() -> Unit = {}) {
        if (cycles != null)
            givenCycles shouldBe cycles
        if (pc != null) 
            cpu.regs.pc shouldBe pc
        if (flags != null)
            cpu.regs.f shouldBe flags
        this.f()
    }

    fun expectAdd16(xval: UShort, a: UShort, b: UShort, flags: UByte) = expect(cycles = 11, pc = 0x0001u) {
        xval shouldBe (a + b).toUShort()

        regs.f.bit(0) shouldBe flagActiveOnCarry(a, xval, 0xFFFF)
        regs.f.bit(1) shouldBe false
        regs.f.bit(2) shouldBe flags.bit(2)
        regs.f.bit(3) shouldBe xval.high().bit(3)
        regs.f.bit(4) shouldBe flagActiveOnCarry(a, xval, 0x0FFF)
        regs.f.bit(5) shouldBe xval.high().bit(5)
        regs.f.bit(6) shouldBe flags.bit(6)
        regs.f.bit(7) shouldBe flags.bit(7)
    }

    fun expectInc(from: UByte, to: UByte, flags: UByte) = expect(cycles = 4, pc = 0x0001u) {
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

    fun expectDec(from: UByte, to: UByte, flags: UByte) = expect(cycles = 4, pc = 0x0001u) {
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

    fun expectRotate(xval: UByte, carry: Boolean, flags: UByte) = expect(cycles = 4, pc = 0x0001u) {
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
    
    fun flagActiveOnOverflow(a: UByte, b: UByte) = flagActiveOnOverflow(a.toInt(), b.toInt())

    fun flagActiveOnUnderflow(a: Int, c: Int): Boolean {
        val b = a - c
        return ((a xor b) and ((a xor c) and 0x80)) != 0
    }
    
    fun flagActiveOnUnderflow(a: UByte, b: UByte) = flagActiveOnUnderflow(a.toInt(), b.toInt())

    fun flagActiveOnCarry(a: Int, c: Int, mask: Int): Boolean = (a and mask) > (c and mask)

    fun flagActiveOnCarry(a: UByte, c: UByte, mask: Int): Boolean = flagActiveOnCarry(a.toInt(), c.toInt(), mask)

    fun flagActiveOnCarry(a: UShort, c: UShort, mask: Int): Boolean = flagActiveOnCarry(a.toInt(), c.toInt(), mask)

    fun flagActiveOnBorrow(a: Int, c: Int, mask: Int): Boolean = (a and mask) < (c and mask)

    fun flagActiveOnBorrow(a: UByte, c: UByte, mask: Int): Boolean = flagActiveOnBorrow(a.toInt(), c.toInt(), mask)
}
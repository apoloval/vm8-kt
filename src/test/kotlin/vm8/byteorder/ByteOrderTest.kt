package vm8.byteorder

import io.kotest.core.spec.style.*
import io.kotest.matchers.*

import vm8.byteorder.*
import vm8.data.*

class ByteOrderTest : FunSpec({
    test("decode word") {
        ByteOrder.BIG_ENDIAN.decode(Octet(0xAB), Octet(0xCD)) shouldBe Word(0xABCD)
        ByteOrder.LITTLE_ENDIAN.decode(Octet(0xAB), Octet(0xCD)) shouldBe Word(0xCDAB)
    }

    test("encode word") {
        ByteOrder.BIG_ENDIAN.encode(Word(0xABCD)) shouldBe Pair(Octet(0xAB), Octet(0xCD))
        ByteOrder.LITTLE_ENDIAN.encode(Word(0xABCD)) shouldBe Pair(Octet(0xCD), Octet(0xAB))
    }
})

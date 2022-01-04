package vm8.byteorder

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class ByteOrderTest : FunSpec({
    test("decode word") {
        ByteOrder.BIG_ENDIAN.decode(0xABu, 0xCDu) shouldBe 0xABCDu.toUShort()
        ByteOrder.LITTLE_ENDIAN.decode(0xABu, 0xCDu) shouldBe 0xCDABu.toUShort()
    }

    test("encode word") {
        ByteOrder.BIG_ENDIAN.encode(0xABCDu) shouldBe Pair(0xABu.toUByte(), 0xCDu.toUByte())
        ByteOrder.LITTLE_ENDIAN.encode(0xABCDu) shouldBe Pair(0xCDu.toUByte(), 0xABu.toUByte())
    }
})

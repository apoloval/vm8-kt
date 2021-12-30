package vm8.data

@JvmInline
value class Octet(private val value: Int) {
    operator fun inc(): Octet = Octet(value.inc() and 0xFF)

    operator fun dec(): Octet = Octet(value.dec() and 0xFF)

    fun bit(n: Int): Boolean = (value shr n) and 0x01 > 0

    fun areBitsSet(mask: Int): Boolean = (value and 0xFF and mask) > 0

    fun bitSet(mask: Int): Octet = Octet((value or mask) and 0xFF)

    fun bitClear(mask: Int): Octet = Octet((value and mask.inv()) and 0xFF)

    fun bitToggle(mask: Int): Octet = Octet((value xor mask) and 0xFF)

    fun rotateLeft(): Pair<Octet, Boolean> = Pair(
        Octet((value shl 1) or (value ushr 7) and 0xFF), 
        value and 0x80 > 0,
    )

    fun rotateLeft(carry: Boolean): Pair<Octet, Boolean> = Pair(
        Octet((value shl 1) or (if (carry) 0x01 else 0x00) and 0xFF), 
        value and 0x80 > 0,
    )

    fun isZero(): Boolean = (value and 0xFF == 0)

    fun isNegative(): Boolean = (value and 0x80 > 0)

    fun parity(): Boolean = (value and 0xFF).countOneBits() % 2 == 0
    
    fun toByte(): Byte = value.toByte()

    fun toInt(): Int = value and 0xFF
}

/**
 * Convert a regular byte into an octet.
 */
fun Byte.toOctet(): Octet = Octet(this.toInt() and 0xFF)
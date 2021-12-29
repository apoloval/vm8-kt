package vm8.data

@JvmInline
value class Octet(private val value: Int) {
    operator fun inc(): Octet = Octet(value.inc() and 0xFF)

    operator fun dec(): Octet = Octet(value.dec() and 0xFF)

    fun bitSet(mask: Int): Octet = Octet((value or mask) and 0xFF)

    fun bitClear(mask: Int): Octet = Octet((value and mask.inv()) and 0xFF)

    fun bitToggle(mask: Int): Octet = Octet((value xor mask) and 0xFF)

    fun shiftLeft(): Pair<Octet, Boolean> = Pair(Octet((value shl 1) and 0xFF), value and 0x80 > 0)
    
    fun toByte(): Byte = value.toByte()
    fun toInt(): Int = value and 0xFF
}
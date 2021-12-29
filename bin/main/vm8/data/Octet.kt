package vm8.data

@JvmInline
value class Octet(private val value: Int) {
    operator fun inc(): Octet = Octet(value.inc() and 0xFF)

    operator fun dec(): Octet = Octet(value.dec() and 0xFF)
    
    fun toByte(): Byte = value.toByte()
    fun toInt(): Int = value and 0xFF
}
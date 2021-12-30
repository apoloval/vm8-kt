package vm8.data

@JvmInline
value class Word(private val value: Int) {
    override fun toString(): String = "0x${Integer.toHexString(value and 0xFFFF)}"

    operator fun inc(): Word = Word(value.inc() and 0xFFFF)

    operator fun plus(v: Word): Word = plus(v.value)

    operator fun plus(v: Int): Word = Word((value + v) and 0xFFFF)

    operator fun dec(): Word = Word(value.inc() and 0xFFFF)

    fun bit(n: Int): Boolean = (value shr n) and 0x01 > 0

    fun low(): Octet = Octet(((value ushr 0) and 0xFF))

    fun high(): Octet = Octet(((value ushr 8) and 0xFF))

    fun setLow(v: Octet): Word { 
        val v0 = v.toInt() and 0xFF
        val v1 = this.value.toInt() and 0xFF00
        return Word(v1 or v0)
    }
    
    fun setHigh(v: Octet): Word { 
        val v0 = this.value.toInt() and 0xFF
        val v1 = (v.toInt() and 0xFF) shl 8
        return Word(v1 or v0)
    }

    fun isNegative(): Boolean = (value and 0x8000 > 0)
    
    fun toInt(): Int = value.toInt() and 0xFFFF    
}

fun Short.toWord(): Word = this.toInt().toWord()

fun Int.toWord(): Word = Word(this and 0xFFFF)

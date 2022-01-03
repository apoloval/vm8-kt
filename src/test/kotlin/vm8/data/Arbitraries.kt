package vm8.data

import io.kotest.mpp.bestName
import io.kotest.property.*
import io.kotest.property.arbitrary.*
import kotlin.reflect.KClass

val OctetArb = arbitrary { Octet(it.random.nextInt() and 0xFF) }

val WordArb = arbitrary { Word(it.random.nextInt() and 0xFFFF) }

@Suppress("UNCHECKED_CAST")
fun<A> generatorForClass(kclass: KClass<*>): Arb<A> {
    return when (kclass.bestName()) {
        "vm8.data.Octet" -> OctetArb as Arb<A>
        "vm8.data.Word" -> WordArb as Arb<A>
        else -> throw IllegalArgumentException("no generator found for $kclass")
    }
}

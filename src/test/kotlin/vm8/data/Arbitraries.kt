package vm8.data

import io.kotest.property.arbitrary.*
import kotlin.random.nextInt

val OctetArb = arbitrary { Octet(it.random.nextInt() and 0xFF) }

val WordArb = arbitrary { Word(it.random.nextInt() and 0xFFFF) }

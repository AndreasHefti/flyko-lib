package com.inari.firefly.util.indexed

import com.inari.util.indexed.AbstractIndexed
import com.inari.util.indexed.Indexer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

class IndexerTest {

    @Test
    fun testSimpleIndexedObject() {
        assertEquals(
            "SimpleIndexedObject : 0",
            Indexer.dump("SimpleIndexedObject")
        )

        val o1 = SimpleIndexedObject()
        val o2 = SimpleIndexedObject()
        val o3 = SimpleIndexedObject()

        assertEquals("SimpleIndexedObject : 3", Indexer.dump("SimpleIndexedObject")
        )
        assertTrue(o1.index == 0)
        assertTrue(o2.index == 1)
        assertTrue(o3.index == 2)
        assertTrue(o1.indexedTypeName == "SimpleIndexedObject")

        o1.dispose()
        assertEquals("SimpleIndexedObject : 2", Indexer.dump("SimpleIndexedObject")
        )
        assertEquals(o1.index, -1)

        val o4 = SimpleIndexedObject()
        assertTrue(o4.index == 0)

        o1.applyNew()
        assertTrue(o1.index == 3)
        try {
            o1.applyNew()
            fail("Exception expected here")
        } catch (e: Exception) {
            assertEquals("Index already applied: 3", e.message)
        }
        assertTrue(o1.index == 3)
        assertEquals("SimpleIndexedObject : 4", Indexer.dump("SimpleIndexedObject")
        )
    }

    @Test
    fun testSimpleIndexedType() {
        assertEquals("SimpleIndexedType : 0", Indexer.dump("SimpleIndexedType")
        )

        val a1 = SITA()
        assertEquals("SimpleIndexedType : 1", Indexer.dump("SimpleIndexedType")
        )
        assertTrue(SITA.index ==  0)
        assertTrue(SITA.indexedTypeName == "SimpleIndexedType")

        val a2 = SITA()
        assertEquals("SimpleIndexedType : 1", Indexer.dump("SimpleIndexedType")
        )
        assertTrue(SITA.index == 0)

        val b1 = SITB()
        assertEquals("SimpleIndexedType : 2", Indexer.dump("SimpleIndexedType")
        )
        assertTrue(SITB.index == 1)
        assertEquals("SimpleIndexedType", SITB.indexedTypeName)

        val c1 = SITC()
        assertEquals("SimpleIndexedType : 3", Indexer.dump("SimpleIndexedType")
        )
        assertTrue(SITC.index == 2)
        assertEquals("SimpleIndexedType", SITC.indexedTypeName)



        SITC.dispose()
        assertEquals("SimpleIndexedType : 2", Indexer.dump("SimpleIndexedType")
        )
        assertTrue(SITC.index == -1)
        assertEquals("SimpleIndexedType", SITC.indexedTypeName)

    }

    @Test
    fun testIndexedTypeAndObject() {
        assertEquals("IndexedType : 0", Indexer.dump("IndexedType")
        )

        val a1 = IOATA()
        assertEquals("IndexedType : 1", Indexer.dump("IndexedType")
        )
        assertEquals("Object:IOATA : 1", Indexer.dump("Object:IOATA")
        )
        assertTrue(a1.index == 0)
        assertTrue(IOATA.index == 0)

        val a2 = IOATA()
        assertEquals("IndexedType : 1", Indexer.dump("IndexedType")
        )
        assertEquals("Object:IOATA : 2", Indexer.dump("Object:IOATA")
        )
        assertTrue(a2.index == 1)
        assertTrue(IOATA.index == 0)

        val b1 = IOATB()
        assertEquals("IndexedType : 2", Indexer.dump("IndexedType")
        )
        assertEquals("Object:IOATA : 2", Indexer.dump("Object:IOATA")
        )
        assertEquals("Object:IOATB : 1", Indexer.dump("Object:IOATB")
        )
        assertTrue(b1.index == 0)
        assertTrue(IOATB.index == 1)

        val b2 = IOATB()
        val b3 = IOATB()
        val c1 = IOATC()
        val c2 = IOATC()
        assertEquals("IndexedType : 3", Indexer.dump("IndexedType")
        )

        assertEquals("Object:IOATA : 2", Indexer.dump("Object:IOATA")
        )

        assertEquals("Object:IOATB : 3", Indexer.dump("Object:IOATB")
        )

        assertEquals("Object:IOATC : 2", Indexer.dump("Object:IOATC")
        )
    }

}

class SimpleIndexedObject : AbstractIndexed("SimpleIndexedObject") {
    fun dispose() = disposeIndex()
    fun applyNew() = applyNewIndex()
}


abstract class SimpleIndexedType {
    abstract class TypeIndex(subTypeName: String) : AbstractIndexed("SimpleIndexedType") {
        fun dispose() = disposeIndex()
        fun applyNew() = applyNewIndex()
    }
}
class SITA : SimpleIndexedType() {
    companion object : TypeIndex("SITA")
}
class SITB : SimpleIndexedType() {
    companion object : TypeIndex("SITB")
}
class SITC : SimpleIndexedType() {
    companion object : TypeIndex("SITC")
}

abstract class IndexedObjectAndType(
    objectIndexerName: String
) : AbstractIndexed(objectIndexerName) {
    fun dispose() = disposeIndex()
    fun applyNew() = applyNewIndex()

    abstract class TypeIndex(subTypeName: String) : AbstractIndexed("IndexedType") {
        fun dispose() = disposeIndex()
        fun applyNew() = applyNewIndex()
    }
}

class IOATA : IndexedObjectAndType("Object:IOATA") {
    companion object : TypeIndex("IOATA")
}
class IOATB : IndexedObjectAndType("Object:IOATB") {
    companion object : TypeIndex("IOATB")
}
class IOATC : IndexedObjectAndType("Object:IOATC") {
    companion object : TypeIndex("IOATC")
}





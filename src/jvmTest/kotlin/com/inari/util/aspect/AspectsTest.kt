

import com.inari.util.aspect.IndexedAspectType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue


class AspectsTest {

    @Test
    fun testAspects() {
        val aspectGroup = IndexedAspectType("TestAspect1")
        aspectGroup.createAspect("TestAspect1")

        val aspect1 = aspectGroup.createAspect("aspect1")
        val aspect2 = aspectGroup.createAspect("aspect2")
        val aspect3 = aspectGroup.createAspect("aspect3")
        val aspect5 = aspectGroup.createAspect("aspect5")
        val aspect10 = aspectGroup.createAspect("aspect10")
        val aspect50 = aspectGroup.createAspect("aspect50")

        val aspects1 = aspectGroup.createAspects()
        val aspects2 = aspectGroup.createAspects()
        val aspects3 = aspectGroup.createAspects()

        assertEquals("Aspects [aspectType=TestAspect1 {}]", aspects1.toString())
        assertEquals("Aspects [aspectType=TestAspect1 {}]", aspects2.toString())
        assertEquals("Aspects [aspectType=TestAspect1 {}]", aspects3.toString())
        assertEquals("0", aspects1.size.toString())
        assertEquals("0", aspects2.size.toString())
        assertEquals("0", aspects3.size.toString())

        aspects1 + aspect1
        aspects1 + aspect3
        aspects1 + aspect5

        aspects2 + aspect2
        aspects2 + aspect10

        aspects3 + aspect50

        assertEquals("Aspects [aspectType=TestAspect1 {aspect1, aspect3, aspect5}]", aspects1.toString())
        assertEquals("Aspects [aspectType=TestAspect1 {aspect2, aspect10}]", aspects2.toString())
        assertEquals("Aspects [aspectType=TestAspect1 {aspect50}]", aspects3.toString())
    }


    @Test
    fun testInclude() {
        val aspectGroup = IndexedAspectType("TestAspect2")

        val aspect1 = aspectGroup.createAspects()
        val aspect2 = aspectGroup.createAspects()
        val aspect3 = aspectGroup.createAspects()
        val aspect4 = aspectGroup.createAspects()

        assertFalse(aspect1.include(aspect2))
        assertFalse(aspect2.include(aspect3))
        assertFalse(aspect3.include(aspect4))
        assertFalse(aspect4.include(aspect4))

        aspect1.bitSet.set(1)
        aspect1.bitSet.set(3)
        aspect1.bitSet.set(5)

        aspect2.bitSet.set(1)
        aspect2.bitSet.set(5)

        aspect3.bitSet.set(3)
        aspect3.bitSet.set(5)

        aspect4.bitSet.set(3)
        aspect4.bitSet.set(5)
        aspect4.bitSet.set(9)

        assertTrue(aspect1.include(aspect2))
        assertFalse(aspect2.include(aspect1))

        assertTrue(aspect1.include(aspect3))
        assertFalse(aspect3.include(aspect1))

        assertTrue(aspect4.include(aspect3))
        assertFalse(aspect3.include(aspect4))

        assertTrue(aspect1.include(aspect1))
    }

    @Test
    fun testExclude() {
        val aspectGroup = IndexedAspectType("TestAspect3")

        val aspect1 = aspectGroup.createAspects()
        val aspect2 = aspectGroup.createAspects()
        val aspect3 = aspectGroup.createAspects()
        val aspect4 = aspectGroup.createAspects()

        assertTrue(aspect1.exclude(aspect2))
        assertTrue(aspect2.exclude(aspect3))
        assertTrue(aspect4.exclude(aspect4))

        aspect1.bitSet.set(1)
        aspect1.bitSet.set(3)
        aspect1.bitSet.set(5)

        aspect2.bitSet.set(0)
        aspect2.bitSet.set(2)
        aspect2.bitSet.set(4)

        aspect3.bitSet.set(3)
        aspect3.bitSet.set(5)

        aspect4.bitSet.set(3)
        aspect4.bitSet.set(5)
        aspect4.bitSet.set(9)

        assertTrue(aspect1.exclude(aspect2))
        assertTrue(aspect2.exclude(aspect1))

        assertFalse(aspect1.exclude(aspect3))
        assertTrue(aspect3.exclude(aspect2))

        assertFalse(aspect1.exclude(aspect1))
    }

}

package com.inari.util.geom


import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BitMaskTest {

    @Test
    fun testInit() {
        var bitMask = BitMask(width = 10, height = 10)

        assertEquals(
            "BitMask [region=[x=0,y=0,width=10,height=10], bits=\n" +
                "0000000000\n" +
                "0000000000\n" +
                "0000000000\n" +
                "0000000000\n" +
                "0000000000\n" +
                "0000000000\n" +
                "0000000000\n" +
                "0000000000\n" +
                "0000000000\n" +
                "0000000000]",
            bitMask.toString()
        )

        bitMask = BitMask(10, 10, 10, 10)

        assertEquals(
            "BitMask [region=[x=10,y=10,width=10,height=10], bits=\n" +
                "0000000000\n" +
                "0000000000\n" +
                "0000000000\n" +
                "0000000000\n" +
                "0000000000\n" +
                "0000000000\n" +
                "0000000000\n" +
                "0000000000\n" +
                "0000000000\n" +
                "0000000000]",
            bitMask.toString()
        )

        bitMask = BitMask(Rectangle(10, 10, 10, 10))

        assertEquals(
            "BitMask [region=[x=10,y=10,width=10,height=10], bits=\n" +
                "0000000000\n" +
                "0000000000\n" +
                "0000000000\n" +
                "0000000000\n" +
                "0000000000\n" +
                "0000000000\n" +
                "0000000000\n" +
                "0000000000\n" +
                "0000000000\n" +
                "0000000000]",
            bitMask.toString()
        )

        assertTrue(bitMask.isEmpty)
    }

    @Test
    fun testReset() {
        val bitMask = BitMask(0, 0, 10, 10)

        assertEquals(
            "BitMask [region=[x=0,y=0,width=10,height=10], bits=\n"
                + "0000000000\n"
                + "0000000000\n"
                + "0000000000\n"
                + "0000000000\n"
                + "0000000000\n"
                + "0000000000\n"
                + "0000000000\n"
                + "0000000000\n"
                + "0000000000\n"
                + "0000000000]",
            bitMask.toString()
        )

        bitMask.reset(0, 0, 5, 5)
        assertEquals(
            "BitMask [region=[x=0,y=0,width=5,height=5], bits=\n"
                + "00000\n"
                + "00000\n"
                + "00000\n"
                + "00000\n"
                + "00000]",
            bitMask.toString()
        )

        bitMask.reset(Rectangle(5, 5, 5, 5))
        assertEquals(
            "BitMask [region=[x=5,y=5,width=5,height=5], bits=\n"
                + "00000\n"
                + "00000\n"
                + "00000\n"
                + "00000\n"
                + "00000]",
            bitMask.toString()
        )
    }

    @Test
    fun testSetRestBit() {
        val bitMask = BitMask(5, 5, 10, 10)

        assertEquals(
            "BitMask [region=[x=5,y=5,width=10,height=10], bits=\n" +
                "0000000000\n" +
                "0000000000\n" +
                "0000000000\n" +
                "0000000000\n" +
                "0000000000\n" +
                "0000000000\n" +
                "0000000000\n" +
                "0000000000\n" +
                "0000000000\n" +
                "0000000000]",
            bitMask.toString()
        )

        bitMask.setBit(7, 7)
        assertEquals(
            "BitMask [region=[x=5,y=5,width=10,height=10], bits=\n" +
                "0000000000\n" +
                "0000000000\n" +
                "0000000000\n" +
                "0000000000\n" +
                "0000000000\n" +
                "0000000000\n" +
                "0000000000\n" +
                "0000000100\n" +
                "0000000000\n" +
                "0000000000]",
            bitMask.toString()
        )

        bitMask.setBit(7, 7, true)
        assertEquals(
            "BitMask [region=[x=5,y=5,width=10,height=10], bits=\n" +
                "0000000000\n" +
                "0000000000\n" +
                "0010000000\n" +
                "0000000000\n" +
                "0000000000\n" +
                "0000000000\n" +
                "0000000000\n" +
                "0000000100\n" +
                "0000000000\n" +
                "0000000000]",
            bitMask.toString()
        )

        bitMask.resetBit(7, 7, true)
        assertEquals(
            "BitMask [region=[x=5,y=5,width=10,height=10], bits=\n" +
                "0000000000\n" +
                "0000000000\n" +
                "0000000000\n" +
                "0000000000\n" +
                "0000000000\n" +
                "0000000000\n" +
                "0000000000\n" +
                "0000000100\n" +
                "0000000000\n" +
                "0000000000]",
            bitMask.toString()
        )

        bitMask.setBit(5, 5, false)
        assertEquals(
            "BitMask [region=[x=5,y=5,width=10,height=10], bits=\n"
                + "0000000000\n"
                + "0000000000\n"
                + "0000000000\n"
                + "0000000000\n"
                + "0000000000\n"
                + "0000010000\n"
                + "0000000000\n"
                + "0000000100\n"
                + "0000000000\n"
                + "0000000000]",
            bitMask.toString()
        )

        bitMask.resetBit(5 * 10 + 5)
        assertEquals(
            "BitMask [region=[x=5,y=5,width=10,height=10], bits=\n"
                + "0000000000\n"
                + "0000000000\n"
                + "0000000000\n"
                + "0000000000\n"
                + "0000000000\n"
                + "0000000000\n"
                + "0000000000\n"
                + "0000000100\n"
                + "0000000000\n"
                + "0000000000]",
            bitMask.toString()
        )
    }

    @Test
    fun testGetBit() {
        val bitMask = BitMask(5, 5, 10, 10)
        bitMask.setBit(2, 2)

        assertEquals(
            "BitMask [region=[x=5,y=5,width=10,height=10], bits=\n"
                + "0000000000\n"
                + "0000000000\n"
                + "0010000000\n"
                + "0000000000\n"
                + "0000000000\n"
                + "0000000000\n"
                + "0000000000\n"
                + "0000000000\n"
                + "0000000000\n"
                + "0000000000]",
            bitMask.toString()
        )

        assertTrue(bitMask.getBit(2, 2))
        assertFalse(bitMask.getBit(2, 3))
    }

    @Test
    fun testMoveRegion() {
        val bitMask = BitMask(0, 0, 10, 10)
        assertEquals(
            "BitMask [region=[x=0,y=0,width=10,height=10], bits=\n"
                + "0000000000\n"
                + "0000000000\n"
                + "0000000000\n"
                + "0000000000\n"
                + "0000000000\n"
                + "0000000000\n"
                + "0000000000\n"
                + "0000000000\n"
                + "0000000000\n"
                + "0000000000]",
            bitMask.toString()
        )

        bitMask.moveRegion(5, 5)
        assertEquals(
            "BitMask [region=[x=5,y=5,width=10,height=10], bits=\n"
                + "0000000000\n"
                + "0000000000\n"
                + "0000000000\n"
                + "0000000000\n"
                + "0000000000\n"
                + "0000000000\n"
                + "0000000000\n"
                + "0000000000\n"
                + "0000000000\n"
                + "0000000000]",
            bitMask.toString()
        )

        bitMask.moveRegion(-10, -10)
        assertEquals(
            "BitMask [region=[x=-5,y=-5,width=10,height=10], bits=\n"
                + "0000000000\n"
                + "0000000000\n"
                + "0000000000\n"
                + "0000000000\n"
                + "0000000000\n"
                + "0000000000\n"
                + "0000000000\n"
                + "0000000000\n"
                + "0000000000\n"
                + "0000000000]",
            bitMask.toString()
        )
    }

    @Test
    fun testSetResetRegion() {
        val bitMask = BitMask(5, 5, 10, 10)

        assertEquals(
            "BitMask [region=[x=5,y=5,width=10,height=10], bits=\n" +
                "0000000000\n" +
                "0000000000\n" +
                "0000000000\n" +
                "0000000000\n" +
                "0000000000\n" +
                "0000000000\n" +
                "0000000000\n" +
                "0000000000\n" +
                "0000000000\n" +
                "0000000000]",
            bitMask.toString()
        )

        bitMask.setRegion(7, 7, 2, 2)
        assertEquals(
            "BitMask [region=[x=5,y=5,width=10,height=10], bits=\n" +
                "0000000000\n" +
                "0000000000\n" +
                "0011000000\n" +
                "0011000000\n" +
                "0000000000\n" +
                "0000000000\n" +
                "0000000000\n" +
                "0000000000\n" +
                "0000000000\n" +
                "0000000000]",
            bitMask.toString()
        )

        bitMask.setRegion(7, 7, 2, 2, false)
        assertEquals(
            "BitMask [region=[x=5,y=5,width=10,height=10], bits=\n" +
                "0000000000\n" +
                "0000000000\n" +
                "0011000000\n" +
                "0011000000\n" +
                "0000000000\n" +
                "0000000000\n" +
                "0000000000\n" +
                "0000000110\n" +
                "0000000110\n" +
                "0000000000]",
            bitMask.toString()
        )

        bitMask.setRegion(9, 10, 20, 2, true)
        assertEquals(
            "BitMask [region=[x=5,y=5,width=10,height=10], bits=\n" +
                "0000000000\n" +
                "0000000000\n" +
                "0011000000\n" +
                "0011000000\n" +
                "0000000000\n" +
                "0000111111\n" +
                "0000111111\n" +
                "0000000110\n" +
                "0000000110\n" +
                "0000000000]",
            bitMask.toString()
        )

        bitMask.resetRegion(5, 5, 20, 3)
        assertEquals(
            "BitMask [region=[x=5,y=5,width=10,height=10], bits=\n"
                + "0000000000\n"
                + "0000000000\n"
                + "0000000000\n"
                + "0011000000\n"
                + "0000000000\n"
                + "0000111111\n"
                + "0000111111\n"
                + "0000000110\n"
                + "0000000110\n"
                + "0000000000]",
            bitMask.toString()
        )

        bitMask.resetRegion(10, 10, 10, 3, true)
        assertEquals(
            "BitMask [region=[x=5,y=5,width=10,height=10], bits=\n"
                + "0000000000\n"
                + "0000000000\n"
                + "0000000000\n"
                + "0011000000\n"
                + "0000000000\n"
                + "0000100000\n"
                + "0000100000\n"
                + "0000000000\n"
                + "0000000110\n"
                + "0000000000]",
            bitMask.toString()
        )

        bitMask.resetRegion(100, 100, 10, 3, true)
        assertEquals(
            "BitMask [region=[x=5,y=5,width=10,height=10], bits=\n"
                + "0000000000\n"
                + "0000000000\n"
                + "0000000000\n"
                + "0011000000\n"
                + "0000000000\n"
                + "0000100000\n"
                + "0000100000\n"
                + "0000000000\n"
                + "0000000110\n"
                + "0000000000]",
            bitMask.toString()
        )
    }

    @Test
    fun testHashIntersection() {
        var bitMask = BitMask(5, 5, 10, 10)

        // no intersection with empty bitMask
        assertFalse(bitMask.hasIntersection(Rectangle(5, 5, 10, 10)))

        bitMask.setBit(5, 5)
        assertEquals(
            "BitMask [region=[x=5,y=5,width=10,height=10], bits=\n"
                + "0000000000\n"
                + "0000000000\n"
                + "0000000000\n"
                + "0000000000\n"
                + "0000000000\n"
                + "0000010000\n"
                + "0000000000\n"
                + "0000000000\n"
                + "0000000000\n"
                + "0000000000]",
            bitMask.toString()
        )

        assertTrue(bitMask.hasIntersection(Rectangle(5, 5, 10, 10)))
        assertTrue(bitMask.hasIntersection(Rectangle(1, 1, 10, 10)))
        assertFalse(bitMask.hasIntersection(Rectangle(0, 1, 10, 10)))
        assertFalse(bitMask.hasIntersection(Rectangle(1, 0, 10, 10)))

        assertTrue(bitMask.hasIntersection(Rectangle(10, 10, 10, 10)))
        assertFalse(bitMask.hasIntersection(Rectangle(11, 10, 10, 10)))
        assertFalse(bitMask.hasIntersection(Rectangle(10, 11, 10, 10)))

        bitMask = BitMask(5, 5, 2, 2)
        bitMask.fill()
        assertEquals(
            "BitMask [region=[x=5,y=5,width=2,height=2], bits=\n"
                + "11\n"
                + "11]",
            bitMask.toString()
        )

        // boundaries x axis
        assertFalse(bitMask.hasIntersection(Rectangle(4, 6, 1, 1)))
        assertTrue(bitMask.hasIntersection(Rectangle(5, 6, 1, 1)))
        assertTrue(bitMask.hasIntersection(Rectangle(6, 6, 1, 1)))
        assertFalse(bitMask.hasIntersection(Rectangle(7, 6, 1, 1)))

        // boundaries y axis
        assertFalse(bitMask.hasIntersection(Rectangle(6, 4, 1, 1)))
        assertTrue(bitMask.hasIntersection(Rectangle(6, 5, 1, 1)))
        assertTrue(bitMask.hasIntersection(Rectangle(6, 6, 1, 1)))
        assertFalse(bitMask.hasIntersection(Rectangle(6, 7, 1, 1)))
    }

    @Test
    fun testAnd() {
        val mask1 = BitMask(0, 0, 10, 10)
        val mask2 = BitMask(5, 5, 10, 10)
        mask1.fill()
        mask2.fill()

        mask1.and(mask2)
        assertEquals(
            "BitMask [region=[x=0,y=0,width=10,height=10], bits=\n"
                + "0000000000\n"
                + "0000000000\n"
                + "0000000000\n"
                + "0000000000\n"
                + "0000000000\n"
                + "0000011111\n"
                + "0000011111\n"
                + "0000011111\n"
                + "0000011111\n"
                + "0000011111]",
            mask1.toString()
        )

        mask2.moveRegion(-8, -8)
        mask1.and(mask2)
        assertEquals(
            "BitMask [region=[x=0,y=0,width=10,height=10], bits=\n"
                + "0000000000\n"
                + "0000000000\n"
                + "0000000000\n"
                + "0000000000\n"
                + "0000000000\n"
                + "0000011000\n"
                + "0000011000\n"
                + "0000000000\n"
                + "0000000000\n"
                + "0000000000]",
            mask1.toString()
        )
    }

    @Test
    fun testSetOr() {
        val mask1 = BitMask(1, 1, 25, 25)
        val mask2 = BitMask(10, 10, 10, 10)
        for (i in 0..9) {
            mask2.setBit(i, i)
        }

        assertEquals(
            "BitMask [region=[x=1,y=1,width=25,height=25], bits=\n" +
                "0000000000000000000000000\n" +
                "0000000000000000000000000\n" +
                "0000000000000000000000000\n" +
                "0000000000000000000000000\n" +
                "0000000000000000000000000\n" +
                "0000000000000000000000000\n" +
                "0000000000000000000000000\n" +
                "0000000000000000000000000\n" +
                "0000000000000000000000000\n" +
                "0000000000000000000000000\n" +
                "0000000000000000000000000\n" +
                "0000000000000000000000000\n" +
                "0000000000000000000000000\n" +
                "0000000000000000000000000\n" +
                "0000000000000000000000000\n" +
                "0000000000000000000000000\n" +
                "0000000000000000000000000\n" +
                "0000000000000000000000000\n" +
                "0000000000000000000000000\n" +
                "0000000000000000000000000\n" +
                "0000000000000000000000000\n" +
                "0000000000000000000000000\n" +
                "0000000000000000000000000\n" +
                "0000000000000000000000000\n" +
                "0000000000000000000000000]",
            mask1.toString()
        )

        assertEquals(
            "BitMask [region=[x=10,y=10,width=10,height=10], bits=\n" +
                "1000000000\n" +
                "0100000000\n" +
                "0010000000\n" +
                "0001000000\n" +
                "0000100000\n" +
                "0000010000\n" +
                "0000001000\n" +
                "0000000100\n" +
                "0000000010\n" +
                "0000000001]",
            mask2.toString()
        )

        mask1.or(mask2)
        assertEquals(
            "BitMask [region=[x=1,y=1,width=25,height=25], bits=\n" +
                "0000000000000000000000000\n" +
                "0000000000000000000000000\n" +
                "0000000000000000000000000\n" +
                "0000000000000000000000000\n" +
                "0000000000000000000000000\n" +
                "0000000000000000000000000\n" +
                "0000000000000000000000000\n" +
                "0000000000000000000000000\n" +
                "0000000000000000000000000\n" +
                "0000000001000000000000000\n" +
                "0000000000100000000000000\n" +
                "0000000000010000000000000\n" +
                "0000000000001000000000000\n" +
                "0000000000000100000000000\n" +
                "0000000000000010000000000\n" +
                "0000000000000001000000000\n" +
                "0000000000000000100000000\n" +
                "0000000000000000010000000\n" +
                "0000000000000000001000000\n" +
                "0000000000000000000000000\n" +
                "0000000000000000000000000\n" +
                "0000000000000000000000000\n" +
                "0000000000000000000000000\n" +
                "0000000000000000000000000\n" +
                "0000000000000000000000000]",
            mask1.toString()
        )
        mask1.or(mask2, -12, -12)
        assertEquals(
            "BitMask [region=[x=1,y=1,width=25,height=25], bits=\n" +
                "1000000000000000000000000\n" +
                "0100000000000000000000000\n" +
                "0010000000000000000000000\n" +
                "0001000000000000000000000\n" +
                "0000100000000000000000000\n" +
                "0000010000000000000000000\n" +
                "0000001000000000000000000\n" +
                "0000000000000000000000000\n" +
                "0000000000000000000000000\n" +
                "0000000001000000000000000\n" +
                "0000000000100000000000000\n" +
                "0000000000010000000000000\n" +
                "0000000000001000000000000\n" +
                "0000000000000100000000000\n" +
                "0000000000000010000000000\n" +
                "0000000000000001000000000\n" +
                "0000000000000000100000000\n" +
                "0000000000000000010000000\n" +
                "0000000000000000001000000\n" +
                "0000000000000000000000000\n" +
                "0000000000000000000000000\n" +
                "0000000000000000000000000\n" +
                "0000000000000000000000000\n" +
                "0000000000000000000000000\n" +
                "0000000000000000000000000]",
            mask1.toString()
        )

    }

    @Test
    fun testOrCase123() {
        val mask1 = BitMask(1, 8, 6, 1)
        val mask2 = BitMask(1, 5, 5, 4)

        mask2.setBit(2, 0)
        mask2.setBit(4, 1)
        mask2.setBit(2, 2)
        mask2.setBit(3, 2)
        mask2.setBit(4, 2)
        mask2.setBit(0, 3)
        mask2.setBit(1, 3)
        mask2.setBit(2, 3)
        mask2.setBit(3, 3)
        mask2.setBit(4, 3)

        assertEquals(
            "BitMask [region=[x=1,y=5,width=5,height=4], bits=\n" +
                "00100\n" +
                "00001\n" +
                "00111\n" +
                "11111]",
            mask2.toString()
        )

        mask1.or(mask2, 1, 0)

        assertEquals(
            "BitMask [region=[x=1,y=8,width=6,height=1], bits=\n" + "011111]",
            mask1.toString()
        )
    }

    @Test
    fun testOrCase456() {
        val mask1 = BitMask(0, 0, 6, 8)
        val mask2 = BitMask(1, 6, 5, 3)

        mask2.setBit(4, 1)
        mask2.setBit(2, 2)
        mask2.setBit(3, 2)
        mask2.setBit(4, 2)

        assertEquals(
            "BitMask [region=[x=1,y=6,width=5,height=3], bits=\n" +
                "00000\n" +
                "00001\n" +
                "00111]",
            mask2.toString()
        )

        mask1.or(mask2)

        assertEquals(
            "BitMask [region=[x=0,y=0,width=6,height=8], bits=\n" +
                "000000\n" +
                "000000\n" +
                "000000\n" +
                "000000\n" +
                "000000\n" +
                "000000\n" +
                "000000\n" +
                "000001]",
            mask1.toString()
        )
    }

    @Test
    fun testCreateIntersectionMaskWithRegion() {
        val intersection = BitMask(0, 0)
        val region = Rectangle(0, 0, 8, 8)
        val mask = BitMask(0, 0, 8, 8)
        mask.fill()

        assertEquals(
            "BitMask [region=[x=0,y=0,width=8,height=8], bits=\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111]",
            mask.toString()
        )

        BitMask.createIntersectionMask(mask, region, intersection)
        assertEquals(
            "BitMask [region=[x=0,y=0,width=8,height=8], bits=\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111]",
            intersection.toString()
        )

        region.pos.x = -4
        region.pos.y = -4
        BitMask.createIntersectionMask(mask, region, intersection)
        assertEquals(
            "BitMask [region=[x=0,y=0,width=4,height=4], bits=\n" +
                "1111\n" +
                "1111\n" +
                "1111\n" +
                "1111]",
            intersection.toString()
        )

        region.pos.x = 4
        region.pos.y = 4
        BitMask.createIntersectionMask(mask, region, intersection)
        assertEquals(
            "BitMask [region=[x=4,y=4,width=4,height=4], bits=\n" +
                "1111\n" +
                "1111\n" +
                "1111\n" +
                "1111]",
            intersection.toString()
        )

        region.pos.x = 4
        region.pos.y = 4
        mask.region().pos.x = -4
        mask.region().pos.y = -4
        BitMask.createIntersectionMask(mask, region, intersection)
        assertEquals(
            "BitMask [region=[x=4,y=4,width=0,height=0], bits=\n" + "]",
            intersection.toString()
        )

        region.pos.x = 0
        region.pos.y = 0
        BitMask.createIntersectionMask(mask, region, intersection)
        assertEquals(
            "BitMask [region=[x=0,y=0,width=4,height=4], bits=\n" +
                "1111\n" +
                "1111\n" +
                "1111\n" +
                "1111]",
            intersection.toString()
        )

        region.pos.x = 10
        region.pos.y = 10
        mask.region().pos.x = 10
        mask.region().pos.y = 10
        BitMask.createIntersectionMask(mask, region, intersection)
        assertEquals(
            "BitMask [region=[x=10,y=10,width=8,height=8], bits=\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111]",
            intersection.toString()
        )
    }

    @Test
    fun testCreateIntersectionMaskWithRegionWithIntersectionAdjustOnMask() {
        val intersection = BitMask(0, 0)
        val region = Rectangle(0, 0, 8, 8)
        val mask = BitMask(0, 0, 8, 8)
        mask.fill()

        assertEquals(
            "BitMask [region=[x=0,y=0,width=8,height=8], bits=\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111]",
            mask.toString()
        )

        BitMask.createIntersectionMask(mask, region, intersection, true)
        assertEquals(
            "BitMask [region=[x=0,y=0,width=8,height=8], bits=\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111]",
            intersection.toString()
        )

        region.pos.x = -4
        region.pos.y = -4
        BitMask.createIntersectionMask(mask, region, intersection, true)
        assertEquals(
            "BitMask [region=[x=0,y=0,width=4,height=4], bits=\n" +
                "1111\n" +
                "1111\n" +
                "1111\n" +
                "1111]",
            intersection.toString()
        )

        region.pos.x = 4
        region.pos.y = 4
        BitMask.createIntersectionMask(mask, region, intersection, true)
        assertEquals(
            "BitMask [region=[x=4,y=4,width=4,height=4], bits=\n" +
                "1111\n" +
                "1111\n" +
                "1111\n" +
                "1111]",
            intersection.toString()
        )

        region.pos.x = 4
        region.pos.y = 4
        mask.region().pos.x = -4
        mask.region().pos.y = -4
        BitMask.createIntersectionMask(mask, region, intersection, true)
        assertEquals(
            "BitMask [region=[x=8,y=8,width=0,height=0], bits=\n" + "]",
            intersection.toString()
        )

        region.pos.x = 8
        region.pos.y = 8
        mask.region().pos.x = -4
        mask.region().pos.y = -4
        BitMask.createIntersectionMask(mask, region, intersection, true)
        assertEquals(
            "BitMask [region=[x=12,y=12,width=0,height=0], bits=\n" + "]",
            intersection.toString()
        )

        region.pos.x = 0
        region.pos.y = 0
        BitMask.createIntersectionMask(mask, region, intersection, true)
        assertEquals(
            "BitMask [region=[x=4,y=4,width=4,height=4], bits=\n" +
                "1111\n" +
                "1111\n" +
                "1111\n" +
                "1111]",
            intersection.toString()
        )

        region.pos.x = 10
        region.pos.y = 10
        mask.region().pos.x = 10
        mask.region().pos.y = 10
        BitMask.createIntersectionMask(mask, region, intersection, true)
        assertEquals(
            "BitMask [region=[x=0,y=0,width=8,height=8], bits=\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111]",
            intersection.toString()
        )

        region.pos.x = -10
        region.pos.y = -10
        mask.region().pos.x = -10
        mask.region().pos.y = -10
        BitMask.createIntersectionMask(mask, region, intersection, true)
        assertEquals(
            "BitMask [region=[x=0,y=0,width=8,height=8], bits=\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111]",
            intersection.toString()
        )
    }

    @Test
    fun testCreateIntersectionMaskWithRegionWithIntersectionAdjustOnRegion() {
        val intersection = BitMask(0, 0)
        val region = Rectangle(0, 0, 8, 8)
        val mask = BitMask(0, 0, 8, 8)
        mask.fill()

        assertEquals(
            "BitMask [region=[x=0,y=0,width=8,height=8], bits=\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111]",
            mask.toString()
        )

        BitMask.createIntersectionMask(region, mask, intersection, true)
        assertEquals(
            "BitMask [region=[x=0,y=0,width=8,height=8], bits=\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111]",
            intersection.toString()
        )

        region.pos.x = -4
        region.pos.y = -4
        BitMask.createIntersectionMask(region, mask, intersection, true)
        assertEquals(
            "BitMask [region=[x=4,y=4,width=4,height=4], bits=\n" +
                "1111\n" +
                "1111\n" +
                "1111\n" +
                "1111]",
            intersection.toString()
        )

        region.pos.x = 4
        region.pos.y = 4
        BitMask.createIntersectionMask(region, mask, intersection, true)
        assertEquals(
            "BitMask [region=[x=0,y=0,width=4,height=4], bits=\n" +
                "1111\n" +
                "1111\n" +
                "1111\n" +
                "1111]",
            intersection.toString()
        )

        region.pos.x = 4
        region.pos.y = 4
        mask.region().pos.x = -4
        mask.region().pos.y = -4
        BitMask.createIntersectionMask(region, mask, intersection, true)
        assertEquals(
            "BitMask [region=[x=0,y=0,width=0,height=0], bits=\n" + "]",
            intersection.toString()
        )

        region.pos.x = 8
        region.pos.y = 8
        mask.region().pos.x = -4
        mask.region().pos.y = -4
        BitMask.createIntersectionMask(region, mask, intersection, true)
        assertEquals(
            "BitMask [region=[x=0,y=0,width=0,height=0], bits=\n" + "]",
            intersection.toString()
        )

        region.pos.x = 0
        region.pos.y = 0
        BitMask.createIntersectionMask(region, mask, intersection, true)
        assertEquals(
            "BitMask [region=[x=0,y=0,width=4,height=4], bits=\n" +
                "1111\n" +
                "1111\n" +
                "1111\n" +
                "1111]",
            intersection.toString()
        )

        region.pos.x = 10
        region.pos.y = 10
        mask.region().pos.x = 10
        mask.region().pos.y = 10
        BitMask.createIntersectionMask(region, mask, intersection, true)
        assertEquals(
            "BitMask [region=[x=0,y=0,width=8,height=8], bits=\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111]",
            intersection.toString()
        )

        region.pos.x = -10
        region.pos.y = -10
        mask.region().pos.x = -10
        mask.region().pos.y = -10
        BitMask.createIntersectionMask(region, mask, intersection, true)
        assertEquals(
            "BitMask [region=[x=0,y=0,width=8,height=8], bits=\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111]",
            intersection.toString()
        )

    }

    @Test
    fun testCreateIntersectionMaskWithRegionWithOffsetOnRegion() {
        val intersection = BitMask(0, 0)
        val region = Rectangle(0, 0, 8, 8)
        val mask = BitMask(0, 0, 8, 8)
        mask.fill()

        assertEquals(
            "BitMask [region=[x=0,y=0,width=8,height=8], bits=\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111]",
            mask.toString()
        )

        BitMask.createIntersectionMask(mask, region, intersection, 0, 0, false)
        assertEquals(
            "BitMask [region=[x=0,y=0,width=8,height=8], bits=\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111]",
            intersection.toString()
        )

        BitMask.createIntersectionMask(mask, region, intersection, -4, -4, false)
        assertEquals(
            "BitMask [region=[x=0,y=0,width=4,height=4], bits=\n" +
                "1111\n" +
                "1111\n" +
                "1111\n" +
                "1111]",
            intersection.toString()
        )

        BitMask.createIntersectionMask(mask, region, intersection, 4, 4, false)
        assertEquals(
            "BitMask [region=[x=4,y=4,width=4,height=4], bits=\n" +
                "1111\n" +
                "1111\n" +
                "1111\n" +
                "1111]",
            intersection.toString()
        )

        mask.region().pos.x = -4
        mask.region().pos.y = -4
        BitMask.createIntersectionMask(mask, region, intersection, 4, 4, false)
        assertEquals(
            "BitMask [region=[x=4,y=4,width=0,height=0], bits=\n" + "]",
            intersection.toString()
        )

        BitMask.createIntersectionMask(mask, region, intersection, 0, 0, false)
        assertEquals(
            "BitMask [region=[x=0,y=0,width=4,height=4], bits=\n" +
                "1111\n" +
                "1111\n" +
                "1111\n" +
                "1111]",
            intersection.toString()
        )

        region.pos.x = 10
        region.pos.y = 10
        mask.region().pos.x = 10
        mask.region().pos.y = 10
        BitMask.createIntersectionMask(mask, region, intersection, 0, 0, false)
        assertEquals(
            "BitMask [region=[x=10,y=10,width=8,height=8], bits=\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111]",
            intersection.toString()
        )

        region.pos.x = 0
        region.pos.y = 0
        mask.region().pos.x = 0
        mask.region().pos.y = 0
        BitMask.createIntersectionMask(mask, region, intersection, 0, 0, true)
        assertEquals(
            "BitMask [region=[x=0,y=0,width=8,height=8], bits=\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111]",
            intersection.toString()
        )

        BitMask.createIntersectionMask(mask, region, intersection, -4, -4, true)
        assertEquals(
            "BitMask [region=[x=0,y=0,width=4,height=4], bits=\n" +
                "1111\n" +
                "1111\n" +
                "1111\n" +
                "1111]",
            intersection.toString()
        )

        BitMask.createIntersectionMask(mask, region, intersection, 4, 4, true)
        assertEquals(
            "BitMask [region=[x=4,y=4,width=4,height=4], bits=\n" +
                "1111\n" +
                "1111\n" +
                "1111\n" +
                "1111]",
            intersection.toString()
        )

        mask.region().pos.x = -4
        mask.region().pos.y = -4
        BitMask.createIntersectionMask(mask, region, intersection, 4, 4, true)
        assertEquals(
            "BitMask [region=[x=8,y=8,width=0,height=0], bits=\n" + "]",
            intersection.toString()
        )

        BitMask.createIntersectionMask(mask, region, intersection, 0, 0, true)
        assertEquals(
            "BitMask [region=[x=4,y=4,width=4,height=4], bits=\n" +
                "1111\n" +
                "1111\n" +
                "1111\n" +
                "1111]",
            intersection.toString()
        )

        region.pos.x = 10
        region.pos.y = 10
        mask.region().pos.x = 10
        mask.region().pos.y = 10
        BitMask.createIntersectionMask(mask, region, intersection, 0, 0, true)
        assertEquals(
            "BitMask [region=[x=0,y=0,width=8,height=8], bits=\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111]",
            intersection.toString()
        )
    }


    @Test
    fun testCreateIntersectionMaskWithRegionWithOffsetOnMask() {
        val intersection = BitMask(0, 0)
        val region = Rectangle(0, 0, 8, 8)
        val mask = BitMask(0, 0, 8, 8)
        mask.fill()

        assertEquals(
            "BitMask [region=[x=0,y=0,width=8,height=8], bits=\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111]",
            mask.toString()
        )

        BitMask.createIntersectionMask(region, mask, intersection, 0, 0, false)
        assertEquals(
            "BitMask [region=[x=0,y=0,width=8,height=8], bits=\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111]",
            intersection.toString()
        )

        BitMask.createIntersectionMask(region, mask, intersection, -4, -4, false)
        assertEquals(
            "BitMask [region=[x=0,y=0,width=4,height=4], bits=\n" +
                "1111\n" +
                "1111\n" +
                "1111\n" +
                "1111]",
            intersection.toString()
        )

        BitMask.createIntersectionMask(region, mask, intersection, 4, 4, false)
        assertEquals(
            "BitMask [region=[x=4,y=4,width=4,height=4], bits=\n" +
                "1111\n" +
                "1111\n" +
                "1111\n" +
                "1111]",
            intersection.toString()
        )

        region.pos.x = -4
        region.pos.y = -4
        BitMask.createIntersectionMask(region, mask, intersection, 4, 4, false)
        assertEquals(
            "BitMask [region=[x=4,y=4,width=0,height=0], bits=\n" + "]",
            intersection.toString()
        )

        BitMask.createIntersectionMask(region, mask, intersection, 0, 0, false)
        assertEquals(
            "BitMask [region=[x=0,y=0,width=4,height=4], bits=\n" +
                "1111\n" +
                "1111\n" +
                "1111\n" +
                "1111]",
            intersection.toString()
        )

        region.pos.x = 10
        region.pos.y = 10
        BitMask.createIntersectionMask(region, mask, intersection, 10, 10, false)
        assertEquals(
            "BitMask [region=[x=10,y=10,width=8,height=8], bits=\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111]",
            intersection.toString()
        )

        region.pos.x = 0
        region.pos.y = 0
        BitMask.createIntersectionMask(region, mask, intersection, 0, 0, true)
        assertEquals(
            "BitMask [region=[x=0,y=0,width=8,height=8], bits=\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111]",
            intersection.toString()
        )

        BitMask.createIntersectionMask(region, mask, intersection, -4, -4, true)
        assertEquals(
            "BitMask [region=[x=0,y=0,width=4,height=4], bits=\n" +
                "1111\n" +
                "1111\n" +
                "1111\n" +
                "1111]",
            intersection.toString()
        )

        BitMask.createIntersectionMask(region, mask, intersection, 4, 4, true)
        assertEquals(
            "BitMask [region=[x=4,y=4,width=4,height=4], bits=\n" +
                "1111\n" +
                "1111\n" +
                "1111\n" +
                "1111]",
            intersection.toString()
        )

        region.pos.x = -4
        region.pos.y = -4
        BitMask.createIntersectionMask(region, mask, intersection, 4, 4, true)
        assertEquals(
            "BitMask [region=[x=8,y=8,width=0,height=0], bits=\n" + "]",
            intersection.toString()
        )

        BitMask.createIntersectionMask(region, mask, intersection, 0, 0, true)
        assertEquals(
            "BitMask [region=[x=4,y=4,width=4,height=4], bits=\n" +
                "1111\n" +
                "1111\n" +
                "1111\n" +
                "1111]",
            intersection.toString()
        )

        region.pos.x = 10
        region.pos.y = 10
        BitMask.createIntersectionMask(region, mask, intersection, 10, 10, true)
        assertEquals(
            "BitMask [region=[x=0,y=0,width=8,height=8], bits=\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111]",
            intersection.toString()
        )
    }

    @Test
    fun testCreateIntersectionMask() {
        val intersection = BitMask(0, 0)
        val mask1 = BitMask(0, 0, 8, 8)
        mask1.fill()
        val mask2 = BitMask(0, 0, 8, 8)
        for (i in 0 until 8 * 8) {
            if (i % 2 > 0) {
                mask2.setBit(i)
            }
        }

        assertEquals(
            "BitMask [region=[x=0,y=0,width=8,height=8], bits=\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111\n" +
                "11111111]",
            mask1.toString()
        )
        assertEquals(
            "BitMask [region=[x=0,y=0,width=8,height=8], bits=\n" +
                "01010101\n" +
                "01010101\n" +
                "01010101\n" +
                "01010101\n" +
                "01010101\n" +
                "01010101\n" +
                "01010101\n" +
                "01010101]",
            mask2.toString()
        )

        BitMask.createIntersectionMask(mask1, mask2, intersection)
        assertEquals(
            "BitMask [region=[x=0,y=0,width=8,height=8], bits=\n" +
                "01010101\n" +
                "01010101\n" +
                "01010101\n" +
                "01010101\n" +
                "01010101\n" +
                "01010101\n" +
                "01010101\n" +
                "01010101]",
            intersection.toString()
        )

        mask2.region().pos.x = -4
        mask2.region().pos.y = -4
        BitMask.createIntersectionMask(mask1, mask2, intersection)
        assertEquals(
            "BitMask [region=[x=0,y=0,width=4,height=4], bits=\n" +
                "0101\n" +
                "0101\n" +
                "0101\n" +
                "0101]",
            intersection.toString()
        )

        mask2.region().pos.x = 4
        mask2.region().pos.y = 4
        BitMask.createIntersectionMask(mask1, mask2, intersection)
        assertEquals(
            "BitMask [region=[x=4,y=4,width=4,height=4], bits=\n" +
                "0101\n" +
                "0101\n" +
                "0101\n" +
                "0101]",
            intersection.toString()
        )

        mask2.region().pos.x = 4
        mask2.region().pos.y = 4
        mask1.region().pos.x = -4
        mask1.region().pos.y = -4
        BitMask.createIntersectionMask(mask1, mask2, intersection)
        assertEquals(
            "BitMask [region=[x=4,y=4,width=0,height=0], bits=\n" + "]",
            intersection.toString()
        )

        mask2.region().pos.x = 0
        mask2.region().pos.y = 0
        BitMask.createIntersectionMask(mask1, mask2, intersection)
        assertEquals(
            "BitMask [region=[x=0,y=0,width=4,height=4], bits=\n" +
                "0101\n" +
                "0101\n" +
                "0101\n" +
                "0101]",
            intersection.toString()
        )

        mask2.region().pos.x = 10
        mask2.region().pos.y = 10
        mask1.region().pos.x = 10
        mask1.region().pos.y = 10
        BitMask.createIntersectionMask(mask1, mask2, intersection)
        assertEquals(
            "BitMask [region=[x=10,y=10,width=8,height=8], bits=\n" +
                "01010101\n" +
                "01010101\n" +
                "01010101\n" +
                "01010101\n" +
                "01010101\n" +
                "01010101\n" +
                "01010101\n" +
                "01010101]",
            intersection.toString()
        )
    }


}

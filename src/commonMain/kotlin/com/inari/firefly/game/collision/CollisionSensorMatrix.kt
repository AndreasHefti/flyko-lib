package com.inari.firefly.game.collision

import com.inari.util.geom.BitMask
import com.inari.util.geom.Rectangle
import com.inari.util.geom.Vector2i
import kotlin.jvm.JvmField

/**
 * +......................+     +...+  = contact area (border)
 * : |        |         | :     ----   = horizontal contact sensor
 * :-#--------#---------#-:       |    = vertical contact sensor
 * : |        |         | :       #    = contact sensor point
 * : |        |         | :       o    = outlet sensor point
 * : |        |         | :     Note:    the collisions are resolved along the contact sensor border
 * :-#--------+---------#-:              And a character ideally fits into the inner rectangle,
 * : |        |         | :              The space between the sensors and the contact area border is 1 pixel
 * : |        |         | :
 * : |        |         | :
 * :-#--------#---------#-:
 * : |        |         | :
 * : |        |         | :
 * +......................+
 */
class CollisionSensorMatrix(
    @JvmField val width: Int,
    @JvmField val height: Int,
    @JvmField val gapNorth: Int = 1,
    @JvmField val gapEast: Int = 1,
    @JvmField val gapSouth: Int = 5,
    @JvmField val gapWest: Int = 1,
    @JvmField val noDistanceValue: Int = Int.MIN_VALUE
) {

    @JvmField val contactSensorLineLeft = BitMask()
    @JvmField val contactSensorLineMiddle = BitMask()
    @JvmField val contactSensorLineRight = BitMask()
    @JvmField val contactSensorLineTop = BitMask()
    @JvmField val contactSensorLineCenter = BitMask()
    @JvmField val contactSensorLineBottom = BitMask()

    @JvmField val rect = Rectangle()
    @JvmField val contactSensorPointTopLeft = Vector2i()
    @JvmField val contactSensorPointTopMiddle = Vector2i()
    @JvmField val contactSensorPointTopRight = Vector2i()
    @JvmField val contactSensorPointCenterLeft = Vector2i()
    @JvmField val contactSensorPointCenterRight = Vector2i()
    @JvmField val contactSensorPointBottomLeft = Vector2i()
    @JvmField val contactSensorPointBottomMiddle = Vector2i()
    @JvmField val contactSensorPointBottomRight = Vector2i()

    @JvmField val firstIndices = intArrayOf(-1 , -1, -1, -1, -1, -1)
    @JvmField val lastIndices = intArrayOf(-1 , -1, -1, -1, -1, -1)
    @JvmField val distances = arrayOf(
        Vector2i(), Vector2i(), Vector2i(),
        Vector2i(),             Vector2i(),
        Vector2i(), Vector2i(), Vector2i())

    init {
        val fullWidth = width + gapWest + gapEast
        val fullHeight = height + gapNorth + gapSouth
        rect(0, 0, fullWidth, fullHeight)
        val xmid = gapWest + (width / 2) - 1
        val xright = fullWidth - gapEast - 1
        val ymid = gapNorth + (height / 2) - 1
        val ybottom = fullHeight - gapSouth -1

        contactSensorLineLeft.reset(gapNorth, 0, 1, fullHeight)
        contactSensorLineMiddle.reset(xmid, 0, 1, fullHeight)
        contactSensorLineRight.reset(xright, 0, 1, fullHeight)

        contactSensorLineTop.reset(0, gapNorth, fullWidth, 1)
        contactSensorLineCenter.reset(0, ymid, fullWidth, 1)
        contactSensorLineBottom.reset(0, ybottom, fullWidth, 1)

        contactSensorPointTopLeft(gapWest, gapNorth)
        contactSensorPointTopMiddle(xmid, gapNorth)
        contactSensorPointTopRight(xright, gapNorth)

        contactSensorPointCenterLeft(gapWest, ymid)
        contactSensorPointCenterRight(xright, ymid)

        contactSensorPointBottomLeft(gapNorth, ybottom)
        contactSensorPointBottomMiddle(xmid, ybottom)
        contactSensorPointBottomRight(xright, ybottom)
    }

    fun calc(contactMask: BitMask) {
        clear()

        // first grab sensor lines
        contactSensorLineLeft.fill().and(contactMask, -contactMask.x, -contactMask.y)
        contactSensorLineMiddle.fill().and(contactMask, -contactMask.x, -contactMask.y)
        contactSensorLineRight.fill().and(contactMask, -contactMask.x, -contactMask.y)

        contactSensorLineTop.fill().and(contactMask, -contactMask.x, -contactMask.y)
        contactSensorLineCenter.fill().and(contactMask, -contactMask.x, -contactMask.y)
        contactSensorLineBottom.fill().and(contactMask, -contactMask.x, -contactMask.y)

        var i = 0
        while (i < contactSensorLineLeft.height) {
            if (contactSensorLineLeft.getBit(0, i)) {
                lastIndices[0] = i
                if (firstIndices[0] < 0) firstIndices[0] = i
            }
            if (contactSensorLineMiddle.getBit(0, i)) {
                lastIndices[1] = i
                if (firstIndices[1] < 0) firstIndices[1] = i
            }
            if (contactSensorLineRight.getBit(0, i)) {
                lastIndices[2] = i
                if (firstIndices[2] < 0) firstIndices[2] = i
            }
            i++
        }
        i = 0
        while (i < contactSensorLineTop.width) {
            if (contactSensorLineTop.getBit(i, 0)) {
                lastIndices[3] = i
                if (firstIndices[3] < 0) firstIndices[3] = i
            }
            if (contactSensorLineCenter.getBit(i, 0)) {
                lastIndices[4] = i
                if (firstIndices[4] < 0) firstIndices[4] = i
            }
            if (contactSensorLineBottom.getBit(i, 0)) {
                lastIndices[5] = i
                if (firstIndices[5] < 0) firstIndices[5] = i
            }
            i++
        }

        calcDistances()
    }

    private fun calcDistances() {
        // distances contactSensorPointTopLeft
        if (firstIndices[3] != -1 && firstIndices[3] < contactSensorPointTopLeft.x)
            distances[0].v0 = -(lastIndices[3] - contactSensorPointTopLeft.x)
        if (firstIndices[0] != -1 && firstIndices[0] < contactSensorPointTopLeft.y)
            distances[0].v1 = -(lastIndices[0] - contactSensorPointTopLeft.y)

        // distances contactSensorPointTopMiddle
        if (lastIndices[3] != -1 && lastIndices[3] < contactSensorPointTopMiddle.x)
            distances[1].v0 = contactSensorPointTopMiddle.x - lastIndices[3]
        else if (firstIndices[3] != -1 && firstIndices[3] >= contactSensorPointTopMiddle.x)
            distances[1].v0 = firstIndices[3] - contactSensorPointTopMiddle.x
        if (firstIndices[1] != -1 && firstIndices[1] < contactSensorPointTopMiddle.y)
            distances[1].v1 = -(lastIndices[1] - contactSensorPointTopMiddle.y)

        // distances contactSensorPointTopRight
        if (lastIndices[3] != -1 && lastIndices[3] > contactSensorPointTopRight.x)
            distances[2].v0 = firstIndices[3] - contactSensorPointTopRight.x
        if (firstIndices[2] != -1 && firstIndices[2] < contactSensorPointTopRight.y)
            distances[2].v1 = -(lastIndices[2] - contactSensorPointTopRight.y)




        // distances contactSensorPointCenterLeft
        if (firstIndices[4] != -1 && firstIndices[4] < contactSensorPointCenterLeft.x)
            distances[3].v0 = -(lastIndices[4] - contactSensorPointCenterLeft.x)
        if (lastIndices[0] != -1 && lastIndices[0] < contactSensorPointCenterLeft.y)
            distances[3].v1 = contactSensorPointCenterLeft.y - lastIndices[1]
        else if (firstIndices[1] != -1 && firstIndices[1] >= contactSensorPointCenterLeft.y)
            distances[3].v1 = -(contactSensorPointCenterLeft.y - firstIndices[1])

        // distances contactSensorPointCenterRight
        if (lastIndices[4] != -1 && lastIndices[4] > contactSensorPointCenterRight.x)
            distances[4].v0 = firstIndices[4] - contactSensorPointCenterRight.x
        if (lastIndices[2] != -1 && lastIndices[2] < contactSensorPointCenterRight.y)
            distances[4].v1 = contactSensorPointCenterRight.y - lastIndices[2]
        else if (firstIndices[2] != -1 && firstIndices[2] >= contactSensorPointCenterRight.y)
            distances[4].v1 = -(contactSensorPointCenterRight.y - firstIndices[2])




        // distances contactSensorPointBottomLeft
        if (firstIndices[5] != -1 && firstIndices[5] < contactSensorPointBottomLeft.x)
            distances[5].v0 = -(lastIndices[5] - contactSensorPointBottomLeft.x)
        if (lastIndices[0] != -1 && lastIndices[0] > contactSensorPointBottomLeft.y)
            distances[5].v1 = firstIndices[0] - contactSensorPointBottomLeft.y

        // distances contactSensorPointBottomMiddle
        if (lastIndices[5] != -1 && lastIndices[5] < contactSensorPointBottomMiddle.x)
            distances[6].v0 = contactSensorPointBottomMiddle.x - lastIndices[5]
        else if (firstIndices[5] != -1 && firstIndices[5] >= contactSensorPointBottomMiddle.x)
            distances[6].v0 = firstIndices[5] - contactSensorPointBottomMiddle.x
        if (lastIndices[1] != -1 && lastIndices[1] > contactSensorPointBottomMiddle.y)
            distances[6].v1 = firstIndices[1] - contactSensorPointBottomMiddle.y

        // distances contactSensorPointBottomRight
        if (lastIndices[5] != -1 && lastIndices[5] > contactSensorPointBottomRight.x)
            distances[7].v0 = firstIndices[5] - contactSensorPointBottomRight.x
        if (lastIndices[2] != -1 && lastIndices[2] > contactSensorPointBottomRight.y)
            distances[7].v1 = firstIndices[2] - contactSensorPointBottomRight.y
    }

    private fun clear() {
        for (i in 0..5) {
            firstIndices[i] = -1
            lastIndices[i] = -1
        }
        distances.forEach { d -> d(noDistanceValue, noDistanceValue) }
    }

    override fun toString(): String {
        val bitmask = BitMask(rect)
        bitmask.or(contactSensorLineLeft)
        bitmask.or(contactSensorLineMiddle)
        bitmask.or(contactSensorLineRight)
        bitmask.or(contactSensorLineTop)
        bitmask.or(contactSensorLineCenter)
        bitmask.or(contactSensorLineBottom)
        return "$bitmask\n\n" +
                "${firstIndices.asList()}\n" +
                "${lastIndices.asList()}\n\n" +
                "contactSensorPointTopLeft:$contactSensorPointTopLeft\n" +
                "contactSensorPointTopMiddle:$contactSensorPointTopMiddle\n" +
                "contactSensorPointTopRight:$contactSensorPointTopRight\n\n" +

                "contactSensorPointCenterLeft:$contactSensorPointCenterLeft\n" +
                "contactSensorPointCenterRight:$contactSensorPointCenterRight\n\n" +

                "contactSensorPointBottomLeft:$contactSensorPointBottomLeft\n" +
                "contactSensorPointBottomMiddle:$contactSensorPointBottomMiddle\n" +
                "contactSensorPointBottomRight:$contactSensorPointBottomRight\n\n" +
                "${distanceToString(distances[0])}    ${distanceToString(distances[1])}    ${distanceToString(distances[2])}\n" +
                "${distanceToString(distances[3])}                ${distanceToString(distances[4])}\n" +
                "${distanceToString(distances[5])}    ${distanceToString(distances[6])}    ${distanceToString(distances[7])}"
    }

    private fun distanceToString(distance: Vector2i) = "[${ if (distance.v0 != noDistanceValue) distance.v0 else "--" }, ${ if (distance.v1 != noDistanceValue) distance.v1 else "--" }]"
}
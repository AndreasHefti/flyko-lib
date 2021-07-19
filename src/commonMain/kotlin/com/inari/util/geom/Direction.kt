package com.inari.util.geom

/** Eight directions, build of four [Orientation], and a NONE constant
 * containing also a horizontal and vertical enum value that divides the
 * Direction into a horizontal and vertical part.
 *
 * Use this if you have a discrete direction with eight different directions,
 * for example, for input, move, or other things.
 */
enum class Direction
/** Use this to create a new Direction with specified horizontal and vertical Orientation.
 * @param horizontal horizontal [Orientation] of the new Direction
 * @param vertical vertical [Orientation] of the new Direction
 */
constructor(
    /** The horizontal orientation NONE/EAST/WEST  */
    val horizontal: Orientation,
    /** The vertical orientation NONE/NORTH/SOUTH  */
    val vertical: Orientation) {

    /** No Direction with also Horizontal.NONE and Vertical.NONE  */
    NONE(Orientation.NONE, Orientation.NONE),
    /** North Direction with Horizontal.NONE and Vertical.UP  */
    NORTH(Orientation.NONE, Orientation.NORTH),
    /** North East Direction with Horizontal.RIGHT and Vertical.UP  */
    NORTH_EAST(Orientation.EAST, Orientation.NORTH),
    /** East Direction with Horizontal.RIGHT and Vertical.NONE  */
    EAST(Orientation.EAST, Orientation.NONE),
    /** South East Direction with Horizontal.RIGHT and Vertical.DOWN  */
    SOUTH_EAST(Orientation.EAST, Orientation.SOUTH),
    /** South Direction with Horizontal.NONE and Vertical.DOWN  */
    SOUTH(Orientation.NONE, Orientation.SOUTH),
    /** South West Direction with Horizontal.LEFT and Vertical.DOWN  */
    SOUTH_WEST(Orientation.WEST, Orientation.SOUTH),
    /** West Direction with Horizontal.LEFT and Vertical.NONE  */
    WEST(Orientation.WEST, Orientation.NONE),
    /** North West Direction with Horizontal.LEFT and Vertical.UP  */
    NORTH_WEST(Orientation.WEST, Orientation.NORTH)

}

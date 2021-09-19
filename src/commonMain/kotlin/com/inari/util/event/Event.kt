package com.inari.util.event

import com.inari.util.Predicate
import com.inari.util.aspect.*
import kotlin.jvm.JvmField

/** Base implementation of an Event handled by the [IEventDispatcher].
 *
 *
 * Implement this for a specific Event type bound to a EventListener implementation (L)
 * For example if a specified Event is needed to notify specified listener(s) creation and
 * implementation of the following interfaces/classes are needed:
 *
 *
 * <pre>
 * public interface TheEventListener {
 *
 * void onEvent( TheEvent event );
 *
 * }
 *
 * public class TheEvent extends Event<TheEventListener> {
 *
 * public final String anEventAttribute;
 *
 * public TheEvent( String anEventAttribute ) {
 * super();
 * this.anEventAttribute = anEventAttribute;
 * }
 *
 * public final void notify( TheEventListener listener ) {
 * listener.onEvent( this );
 * }
 * }
</TheEventListener></pre> *
 *
 *
 * @author andreas hefti
 *
 * @param <L> The type of EventListener the is interested in the specified Event.
</L> */
abstract class Event<L> protected constructor() {

    abstract val eventType: EventType

    /** Implements the notification of specified listener.
     * For an implementation example have a look at the class documentation example
     *
     * @param listener the listener to notify.
     */
    protected abstract fun notify(listener: L)
    internal fun _notify(listener: L) = notify(listener)

    /** This is called within the IEventDispatcher after the Event has passed all its listeners and can be restored.
     * Useful for event pooling.
     * This is an empty implementation and does nothing. Override this to get notified on restore
     */
    protected fun restore() {}
    internal fun _restore() = restore()

    companion object {
        @JvmField val EVENT_ASPECTS = IndexedAspectType("EVENT_ASPECTS")
    }

     abstract class EventType(name: String) : Aspect {
         private val aspect = EVENT_ASPECTS.createAspect(name)
         override val aspectName = aspect.aspectName
         override val aspectType = aspect.aspectType
         override val aspectIndex = aspect.aspectIndex
     }

}

/** An [Event] with an [Aspect] to verify interested listeners.
 *
 *
 * Use this to notify [AspectedEventListener] listening to specified [Event] and [Aspect].
 *
 * @author andreas hefti
 *
 * @param <L> The [AspectedEventListener] implementation type
</L> */
abstract class AspectedEvent<L : AspectedEventListener> protected constructor() : Event<L>() {

    /** Get the [Aspects] for this event. Only [AspectedEventListener] that are listening to the specified [Event] and [Aspect] are
     * going to be notified on this event.
     *
     * @return The Aspect of this event
     */
    abstract val aspects: Aspects

}

/** An event listener definition which defines also a specified matching within an [Aspects] to verify if
 * the listener is interested on a certain [AspectedEvent] or not.
 *
 *
 * Use this to listen to an [AspectedEvent] that matches the [Aspects] on that the listener is interested in.
 * The listener will only get notified on a specified [AspectedEvent] if it match the [Aspects]
 * @author andreas hefti
 */
interface AspectedEventListener {

    /** Use this to implement the [Aspect] matching.
     *
     * @param aspect the [Aspects] form [AspectedEvent] to test the matching
     * @return true if the listener is interested on a AspectedEvent with specified Aspect.
     */
    fun match(aspects: Aspects): Boolean

}

/** This is just a marker interface and used to bind [PredicatedEvent] to [PredicatedEventListener].
 *
 * @author andreas hefti
 *
 * @param <L> The type of [PredicatedEventListener] to which the [PredicatedEvent] type is bound to.
</L> */
abstract class PredicatedEvent<L : PredicatedEventListener> protected constructor() : Event<L>()

/** An event listener which implements also a specified matcher for the [PredicatedEvent] that matches if
 * the listener is interested on specified [PredicatedEvent].
 *
 *
 * Use this to listen to an [PredicatedEvent] that matches the Event on that the listener is interested in.
 * The listener will only get notifies on a specified [PredicatedEvent] if the [Predicate] matches
 * @author andreas hefti
 */
interface PredicatedEventListener {

    /** Get the matcher for this listener. Only listeners that are listening to the specified [Event] and matches
     * the specified [Predicate] are going to be notified.
     *
     * @return The Matcher to check if the MatchedEventListener is interested in a specified Event
     */
    fun <L : PredicatedEventListener> getMatcher(): Predicate<PredicatedEvent<L>>

}

package com.inari.util.event

import com.inari.util.aspect.Aspect


/** An generic Event Dispatcher to register/unregister generic Listeners to listen to matching [Event].
 * A Listener interface can be created for specified needs to working with a [Event]<Listener> implementation.
 *
 *
 * Implementation and usage example:
 *
 * <pre>
 * public interface TheEventListener extends EventListener {
 *
 * void onTheEvent( TheEvent event );
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
 * listener.onTheEvent( this );
 * }
 * }
 *
 * public class MyTheEventListener implements TheEventListener {
 *
 * public void onTheEvent( TheEvent event ) {
 * ... code that process the event for this listener ...
 * }
 * }
 *
 * MyTheEventListener myListener1 = new MyTheEventListener();
 * MyTheEventListener myListener2 = new MyTheEventListener();
 * ...
 * eventDispatcher.register( TheEvent.class, myListener1 );
 * eventDispatcher.register( TheEvent.class, myListener2 );
 * ...
 * eventDispatcher.notify( new TheEvent( "Hello Moon" );
 * ...
</TheEventListener></pre> *
 *
 * @author andreas hefti
</Listener> */
interface IEventDispatcher {

    /** Register a Listener L to listen to specified type of [Event].
     *
     * @param eventTypeAspect The class type of the [Event] to listen at
     * @param listener The listener to register and that gets informed by specified [Event]
     */
    fun <L> register(eventTypeAspect: Aspect, listener: L)

    /** Unregister a specified Listener for a specified [Event] type.
     *
     * @param eventTypeAspect The [Event] (class) type
     * @param listener the listener to unregister.
     * @return true if the specified listener was unregistered or false if there was no such listener registered
     */
    fun <L> unregister(eventTypeAspect: Aspect, listener: L): Boolean

    /** Notifies all listeners that are interested on the specific type of Event within the specified [Event].
     *
     * @param event The Event to send to the listeners.
     */
    fun <L> notify(event: Event<L>)

    /** Notifies all listeners that are interested on the specific type of Event with the specified [Aspect]
     * within the specified Event.
     *
     * @param event The [AspectedEvent] to send to the [AspectedEventListener].
     */
    fun <L : AspectedEventListener> notify(event: AspectedEvent<L>)

    /** Notifies all [PredicatedEventListener] that are interested on the specific type of Event and that matches
     * the specified [PredicatedEvent], within the specified [Predicate].
     *
     * @param event The [PredicatedEvent] to send to the [PredicatedEventListener].
     */
    fun <L : PredicatedEventListener> notify(event: PredicatedEvent<L>)

}

/** This can be used as a event logging connector interface by connecting it to
 * a given IEventDispatcher implementation. The log method gets called on every
 * notify call, getting the specified event that is notifed.
 */
interface IEventLog {
    /** Logs the specified Event. Is called on notify event methods of a given [IEventDispatcher] implementation
     * @param event The [Event] that is notified
     */
    fun log(event: Event<*>)

}

package com.inari.firefly.actor

import com.inari.firefly.*
import com.inari.firefly.core.ComponentRefResolver
import com.inari.firefly.entity.EntityComponent
import com.inari.firefly.entity.EntityComponentType
import com.inari.firefly.entity.EntitySystem
import com.inari.firefly.physics.contact.ContactConstraint
import com.inari.firefly.physics.contact.EContact
import com.inari.util.IntPredicate
import com.inari.util.aspect.Aspect
import kotlin.jvm.JvmField

class EActor private constructor () : EntityComponent(EActor::class.simpleName!!) {

    @JvmField internal var encounterContactConstraintRef = -1
    @JvmField internal var hitContactConstraintRef = -1

    var category: Aspect = UNDEFINED_ACTOR_CATEGORY
        set(value) { if (ACTOR_CATEGORY_ASPECT_GROUP.typeCheck(value)) field = value else throw IllegalArgumentException() }
    var type: Aspect = UNDEFINED_ACTOR_TYPE
        set(value) { if (ACTOR_TYPE_ASPECT_GROUP.typeCheck(value)) field = value else throw IllegalArgumentException() }
    var health: Int = -1
    var maxHealth: Int = -1
    var hitPower: Int = 0
    val encounterConstraint = ComponentRefResolver(ContactConstraint) {
            index -> encounterContactConstraintRef = index
        }
    val hitConstraint = ComponentRefResolver(ContactConstraint) {
            index -> hitContactConstraintRef = index
        }

    override fun reset() {
        category = UNDEFINED_ACTOR_CATEGORY
        type = UNDEFINED_ACTOR_TYPE
        maxHealth = -1
        health = -1
        encounterContactConstraintRef = -1
        hitContactConstraintRef = -1
    }

    override fun componentType() = Companion

    companion object : EntityComponentType<EActor>(EActor::class) {



        @JvmField val ENCOUNTER_CONTACT_TYPE = CONTACT_TYPE_ASPECT_GROUP.createAspect("ENCOUNTER_CONTACT_TYPE")

        @JvmField val ANY_ENCOUNTER_CONDITION: IntPredicate = { entityId ->
                val entity = EntitySystem[entityId]
                val actor = entity[EActor]
                if (actor.encounterContactConstraintRef < 0) false
                else !entity[EContact].contactScan[actor.encounterContactConstraintRef].hasAnyContact()
        }

        @JvmField val ANY_HIT_CONDITION: IntPredicate =  { entityId ->
                val entity = EntitySystem[entityId]
                val actor = entity[EActor]
                if (actor.hitContactConstraintRef < 0) false
                else !entity[EContact].contactScan[actor.hitContactConstraintRef].hasAnyContact()
        }

        override fun createEmpty() = EActor()
    }
}
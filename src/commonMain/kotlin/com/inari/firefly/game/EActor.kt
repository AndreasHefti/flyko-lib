package com.inari.firefly.game

import com.inari.firefly.core.CReference
import com.inari.firefly.core.EntityComponent
import com.inari.firefly.core.EntityComponentBuilder
import com.inari.firefly.physics.contact.ContactConstraint
import com.inari.util.aspect.Aspect
import com.inari.util.aspect.IndexedAspectType
import kotlin.jvm.JvmField

class EActor private constructor() : EntityComponent(EActor) {

    @JvmField internal var encounterContactConstraintRef = -1
    @JvmField internal var hitContactConstraintRef = -1

    var category: Aspect = UNDEFINED_ACTOR_CATEGORY
        set(value) { if (ACTOR_CATEGORY_ASPECT_GROUP.typeCheck(value)) field = value else throw IllegalArgumentException() }
    var type: Aspect = UNDEFINED_ACTOR_TYPE
        set(value) { if (ACTOR_TYPE_ASPECT_GROUP.typeCheck(value)) field = value else throw IllegalArgumentException() }
    @JvmField var health: Int = -1
    @JvmField var maxHealth: Int = -1
    @JvmField var hitPower: Int = 0
    val encounterConstraintIndex: Int
        get() = encounterConstraint.targetKey.instanceIndex
    @JvmField val encounterConstraint = CReference(ContactConstraint)
    val hitConstraintIndex: Int
        get() = hitConstraint.targetKey.instanceIndex
    @JvmField val hitConstraint = CReference(ContactConstraint)

    override fun reset() {
        category = UNDEFINED_ACTOR_CATEGORY
        type = UNDEFINED_ACTOR_TYPE
        maxHealth = -1
        health = -1
        encounterContactConstraintRef = -1
        hitContactConstraintRef = -1
    }

    override val componentType = Companion
    companion object : EntityComponentBuilder<EActor>("EActor") {
        override fun create() = EActor()

        @JvmField val ACTOR_CATEGORY_ASPECT_GROUP = IndexedAspectType("ACTOR_CATEGORY_ASPECT_GROUP")
        @JvmField val UNDEFINED_ACTOR_CATEGORY = ACTOR_CATEGORY_ASPECT_GROUP.createAspect("UNDEFINED_ACTOR_CATEGORY")

        @JvmField val ACTOR_TYPE_ASPECT_GROUP = IndexedAspectType("ACTOR_TYPE_ASPECT_GROUP")
        @JvmField val UNDEFINED_ACTOR_TYPE = ACTOR_TYPE_ASPECT_GROUP.createAspect("UNDEFINED_ACTOR_TYPE")
    }
}
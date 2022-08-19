package com.inari.firefly.core.api

import kotlin.reflect.KClass

interface ResourceServiceAPI {

    fun loadTextResource(resourceName: String, encryption: String? = null): String

    fun writeTextResource(resourceName: String, text: String, encryption: String? = null)

    fun <T : Any> loadJSONResource(resourceName: String, type: KClass<T>, encryption: String? = null): T

    fun <T : Any> writeJSNONResource(resourceName: String, jsonObject: T, encryption: String? = null)

}

expect object ResourceServiceAPIImpl {
    fun loadTextResource(resourceName: String, encryption: String? = null): String
    fun writeTextResource(resourceName: String, text: String, encryption: String? = null)
    fun <T : Any> loadJSONResource(resourceName: String, type: KClass<T>, encryption: String? = null): T
    fun <T : Any> writeJSNONResource(resourceName: String, jsonObject: T, encryption: String? = null)
}
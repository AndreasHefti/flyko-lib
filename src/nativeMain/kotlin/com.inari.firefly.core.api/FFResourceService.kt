package com.inari.firefly.core.api

import kotlin.reflect.KClass

actual object FFResourceService : ResourceServiceAPI {

    actual override fun loadTextResource(resourceName: String, encryption: String?): String {
        throw RuntimeException("TODO")
    }

    actual override fun writeTextResource(resourceName: String, text: String, encryption: String?) {
        throw RuntimeException("TODO")
    }

    actual override fun <T : Any> loadJSONResource(resourceName: String, type: KClass<T>, encryption: String?): T {
        throw RuntimeException("TODO")
    }

    actual override fun <T : Any> writeJSNONResource(resourceName: String, jsonObject: T, encryption: String?) {
        throw RuntimeException("TODO")
    }

}
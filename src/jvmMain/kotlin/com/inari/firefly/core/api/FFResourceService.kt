package com.inari.firefly.core.api

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapter
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.File
import java.io.UnsupportedEncodingException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import kotlin.reflect.KClass

actual object  FFResourceService : ResourceServiceAPI {

    private val JSONMapper = Moshi
        .Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    actual override fun loadTextResource(resourceName: String, encryption: String?): String {
        if (encryption != null) {
            return try {
                AES.decrypt(ClassLoader.getSystemResource(resourceName).readText(), encryption)!!
            } catch (e: Exception) {
                try {
                    AES.decrypt(File(resourceName).readText(), encryption)!!
                } catch(e: Exception) {
                    throw RuntimeException("Failed to load world from resource: $resourceName", e)
                }
            }
        } else {
            return try {
                ClassLoader.getSystemResource(resourceName).readText()
            } catch (e: Exception) {
                try {
                    File(resourceName).readText()
                } catch(e: Exception) {
                    throw RuntimeException("Failed to load world from resource: $resourceName", e)
                }
            }
        }
    }

    actual override fun writeTextResource(resourceName: String, text: String, encryption: String?) {
        try {
            if (encryption != null)
                File(resourceName).writeText(AES.encrypt(text, encryption)!!)
            else
                File(resourceName).writeText(text)
        } catch (e: Exception) {
            throw RuntimeException("Failed to write to resource to File: $resourceName", e)
        }
    }

    actual override fun <T : Any> loadJSONResource(resourceName: String, type: KClass<T>, encryption: String?): T {
        try {
            val text = loadTextResource(resourceName, encryption)
            val jsonAdapter: JsonAdapter<T> = JSONMapper.adapter(type.java)
            val result = jsonAdapter.fromJson(text)
            return result!!
            //return JSONMapper.readValue(text, type.javaObjectType)
        } catch (e: Exception) {
            throw RuntimeException("Failed to load world from resource: $resourceName", e)
        }
    }

    actual override fun <T : Any> writeJSNONResource(resourceName: String, jsonObject: T, encryption: String?) {
        try {
            val jsonAdapter: JsonAdapter<T> = JSONMapper.adapter(jsonObject.javaClass)
            writeTextResource(resourceName, jsonAdapter.toJson(jsonObject), encryption)
        } catch (e: Exception) {
            throw RuntimeException("Failed to write to resource to File: $resourceName", e)
        }
    }



    internal object AES {
        private var secretKey: SecretKeySpec? = null
        private lateinit var key: ByteArray
        fun setKey(myKey: String) {
            val sha: MessageDigest?
            try {
                key = myKey.toByteArray(charset("UTF-8"))
                sha = MessageDigest.getInstance("SHA-1")
                key = sha.digest(key)
                key = key.copyOf(16)
                secretKey = SecretKeySpec(key, "AES")
            } catch (e: NoSuchAlgorithmException) {
                e.printStackTrace()
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
            }
        }

        fun encrypt(strToEncrypt: String, secret: String): String? {
            try {
                setKey(secret)
                val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
                cipher.init(Cipher.ENCRYPT_MODE, secretKey)
                return Base64.getEncoder().encodeToString(cipher.doFinal(strToEncrypt.toByteArray(charset("UTF-8"))))
            } catch (e: java.lang.Exception) {
                println("Error while encrypting: $e")
            }
            return null
        }

        fun decrypt(strToDecrypt: String?, secret: String): String? {
            try {
                setKey(secret)
                val cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING")
                cipher.init(Cipher.DECRYPT_MODE, secretKey)
                return String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)))
            } catch (e: java.lang.Exception) {
                println("Error while decrypting: $e")
            }
            return null
        }
    }

}
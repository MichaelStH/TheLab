package com.riders.thelab.core.data.utils

import kotlin.reflect.KClass


fun <T : Any> T.getClass(): KClass<T> {
    return javaClass.kotlin
}

inline fun <reified T : Any> T.getInstance(): T {
    return T::class.java.newInstance()
}

fun <T : Any> T.isSubclassOf(baseClass: KClass<*>): Boolean {
    return this::class.isSubclassOf(baseClass)
}

fun <T : Any> T.isInstance(baseClass: KClass<*>): Boolean {
    return this::class.isInstance(baseClass)
}

fun <T : Any> T.isSuperclassOf(baseClass: KClass<*>): Boolean {
    return this::class.isSuperclassOf(baseClass)
}

package org.ktapi

internal object Util {
    val classLoader: ClassLoader = Util.javaClass.classLoader
}

/**
 * This is a convenience class that adds an init function that can be called to
 * force the init block of the class to execute.
 *
 * @param blocks list of blocks of code that will be executed sequentially as part of the init
 */
open class Init(vararg blocks: () -> Any?) {
    init {
        blocks.forEach { it() }
    }

    /**
     * Ensures the init block on the object is run
     */
    fun init() = Unit
}

// String Functions
/**
 * @return the contents of the resource path in the string or null if it doesn't exist
 */
fun String.resourceAsString() = Util.classLoader.getResource(this)?.readText()

/**
 * @return true if a classpath resource exists with the path in the string
 */
fun String.resourceExists() = Util.classLoader.getResource(this) != null

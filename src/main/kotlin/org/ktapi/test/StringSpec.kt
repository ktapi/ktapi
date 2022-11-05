package org.ktapi.test

import io.kotlintest.TestCase
import io.kotlintest.TestResult
import io.kotlintest.specs.AbstractStringSpec
import io.mockk.mockkObject
import io.mockk.unmockkAll

abstract class StringSpec(body: AbstractStringSpec.() -> Unit = {}) : io.kotlintest.specs.StringSpec(body) {
    override fun beforeTest(testCase: TestCase) {
        objectMocks.forEach { mockkObject(it) }
    }

    override fun afterTest(testCase: TestCase, result: TestResult) {
        if (objectMocks.isNotEmpty()) {
            unmockkAll()
        }
    }

    open val objectMocks: List<Any> = listOf()
}
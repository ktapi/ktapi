package org.ktapi.test

import io.kotlintest.TestCase
import io.kotlintest.TestResult
import io.kotlintest.specs.AbstractStringSpec
import org.ktapi.db.Database
import org.ktorm.database.TransactionIsolation

abstract class DbStringSpec(body: AbstractStringSpec.() -> Unit = {}) : StringSpec(body) {
    init {
        Database.init()
    }

    override fun beforeTest(testCase: TestCase) {
        if (Database.readWrite.transactionManager.currentTransaction == null) {
            Database.readWrite.transactionManager.newTransaction(isolation = TransactionIsolation.READ_COMMITTED)
        }
        super.beforeTest(testCase)
    }

    override fun afterTest(testCase: TestCase, result: TestResult) {
        super.afterTest(testCase, result)
        Database.readWrite.transactionManager.currentTransaction?.rollback()
    }
}
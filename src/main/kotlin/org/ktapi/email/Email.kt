package org.ktapi.email

import org.ktapi.config

data class EmailData(val email: String, val name: String? = null)

interface Email {
    val defaultFromAddress: EmailData
        get() = EmailData(config("email.defaultFrom"), config("email.defaultFromName"))

    fun send(
        template: String,
        to: EmailData,
        data: Any? = null,
        from: EmailData = defaultFromAddress,
        subject: String? = null,
        replyTo: EmailData? = null
    )
}
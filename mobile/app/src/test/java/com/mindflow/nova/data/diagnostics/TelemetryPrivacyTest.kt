package com.mindflow.nova.data.diagnostics

import io.sentry.Breadcrumb
import io.sentry.SentryEvent
import io.sentry.protocol.Message
import io.sentry.protocol.Request
import io.sentry.protocol.SentryException
import io.sentry.protocol.User
import org.junit.Assert.*
import org.junit.Test

class TelemetryPrivacyTest {
    @Test fun privateFieldsAndExceptionMessagesAreRemoved() {
        val event = SentryEvent().apply {
            user = User().apply { email = "student@example.test"; id = "7" }
            request = Request().apply { url = "https://example.test/?token=private" }
            extras = mapOf("answer" to "private")
            tags = mapOf("fullName" to "private")
            breadcrumbs = listOf(Breadcrumb().apply { message = "private" })
            exceptions = listOf(SentryException().apply { type = "IOException"; value = "Bearer private" })
            message = Message().apply { formatted = "student@example.test" }
            contexts["lesson"] = mapOf("answer" to "private")
        }
        sanitizeEvent(event)
        assertNull(event.user)
        assertNull(event.request)
        assertTrue(event.extras.orEmpty().isEmpty())
        assertTrue(event.tags.orEmpty().isEmpty())
        assertTrue(event.breadcrumbs.orEmpty().isEmpty())
        assertNull(event.message)
        assertNull(event.exceptions!!.single().value)
        assertEquals("IOException", event.exceptions!!.single().type)
        assertFalse(event.contexts.containsKey("lesson"))
    }

    @Test fun onlyKnownProductEventNamesAreKept() {
        val event = SentryEvent().apply { message = Message().apply { formatted = "lesson_completed" } }
        assertEquals("lesson_completed", sanitizeEvent(event).message?.formatted)
    }
}

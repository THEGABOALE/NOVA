package com.mindflow.nova.data.diagnostics

import android.content.Context
import com.mindflow.nova.BuildConfig
import io.sentry.Sentry
import io.sentry.SentryEvent
import io.sentry.android.core.SentryAndroid
import io.sentry.protocol.Message

enum class ProductEvent {
    LESSON_STARTED, LESSON_COMPLETED, LESSON_FAILED, MISSION_ABANDONED,
    OFFLINE_ATTEMPT_CREATED, SYNC_FAILED
}

/** Sin identidad, texto libre, IDs académicos ni historial de navegación. */
object NovaTelemetry {
    fun init(context: Context) {
        if (BuildConfig.DEBUG || BuildConfig.SENTRY_DSN.isBlank()) return
        SentryAndroid.init(context) { options ->
            options.dsn = BuildConfig.SENTRY_DSN
            options.release = "nova@${BuildConfig.VERSION_NAME}+${BuildConfig.VERSION_CODE}"
            options.environment = "production"
            options.setSendDefaultPii(false)
            options.setEnableAutoSessionTracking(false)
            options.setEnableScopePersistence(false)
            options.setEnableAutoActivityLifecycleTracing(false)
            options.setEnableActivityLifecycleBreadcrumbs(false)
            options.setEnableAppLifecycleBreadcrumbs(false)
            options.setEnableSystemEventBreadcrumbs(false)
            options.setEnableAppComponentBreadcrumbs(false)
            options.setEnableNetworkEventBreadcrumbs(false)
            options.setEnableUserInteractionBreadcrumbs(false)
            options.setEnableNdk(false)
            options.setAttachScreenshot(false)
            options.setAttachViewHierarchy(false)
            options.setAttachAnrThreadDump(false)
            options.tracesSampleRate = 0.0
            options.maxBreadcrumbs = 0
            options.setBeforeSend { event, _ -> sanitizeEvent(event) }
        }
    }

    fun record(event: ProductEvent) {
        if (!Sentry.isEnabled()) return
        val sentryEvent = SentryEvent()
        sentryEvent.message = Message().apply { formatted = event.name.lowercase() }
        Sentry.captureEvent(sentryEvent)
    }
}

/** Conservar tipos/frames para diagnosticar, pero no mensajes que puedan contener datos. */
internal fun sanitizeEvent(event: SentryEvent): SentryEvent {
    event.user = null
    event.request = null
    event.extras = emptyMap()
    event.tags = emptyMap()
    event.breadcrumbs = emptyList()
    event.exceptions?.forEach { it.value = null }
    event.threads?.forEach { it.name = null }
    val productName = event.message?.formatted?.takeIf { it in ProductEvent.entries.map { event -> event.name.lowercase() } }
    event.message = productName?.let { Message().apply { formatted = it } }
    // Los contextos de dispositivos y SDK bastan; no conservar contextos adicionales.
    val allowed = setOf("app", "device", "os", "runtime")
    event.contexts.keys().asSequence().toList().filterNot { it in allowed }.forEach { event.contexts.remove(it) }
    event.contexts.app?.let { it.appName = "NOVA"; it.deviceAppHash = null }
    event.contexts.device?.let { it.id = null; it.name = null }
    return event
}

package com.mindflow.nova.data.diagnostics

import android.content.Context
import com.mindflow.nova.BuildConfig
import io.sentry.SentryEvent
import io.sentry.android.core.SentryAndroid

/**
 * Reporte de crashes y errores, sin identidad, texto libre, IDs académicos ni
 * historial de navegación. Solo errores: las métricas de uso (lecciones
 * empezadas, terminadas, pendientes) salen de mission_attempts en el servidor,
 * no de eventos que gastarían la cuota de crashes.
 */
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
            options.setAttachScreenshot(false)
            options.setAttachViewHierarchy(false)
            options.setAttachAnrThreadDump(false)
            options.tracesSampleRate = 0.0
            options.maxBreadcrumbs = 0
            options.setBeforeSend { event, _ -> sanitizeEvent(event) }
        }
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
    event.message = null
    // Los contextos de dispositivos y SDK bastan; no conservar contextos adicionales.
    val allowed = setOf("app", "device", "os", "runtime")
    event.contexts.keys().asSequence().toList().filterNot { it in allowed }.forEach { event.contexts.remove(it) }
    event.contexts.app?.let { it.appName = "NOVA"; it.deviceAppHash = null }
    event.contexts.device?.let { it.id = null; it.name = null }
    return event
}

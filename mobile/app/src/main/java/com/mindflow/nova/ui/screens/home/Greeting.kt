package com.mindflow.nova.ui.screens.home

/**
 * "¡Hola, Gabriela!" con el primer nombre en mayúscula inicial, o "¡Hola!" si
 * no hay nombre. Reemplaza a "Bienvenido a NOVA", que iba en masculino y sin
 * nombre aunque la app ya lo sabe.
 */
internal fun greeting(fullName: String?): String {
    val first = fullName?.trim()?.split(" ")?.firstOrNull { it.isNotBlank() }
        ?: return "¡Hola!"
    return "¡Hola, ${first.replaceFirstChar { it.uppercaseChar() }}!"
}

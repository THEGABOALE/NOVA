package com.mindflow.nova.ui.components

import androidx.compose.runtime.saveable.Saver
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/** Solo estado pequeño de UI. El contenido de misiones se guarda en Room. */
inline fun <reified T> jsonStateSaver(): Saver<T, String> = Saver(
    save = { Gson().toJson(it) },
    restore = { Gson().fromJson<T>(it, object : TypeToken<T>() {}.type) }
)

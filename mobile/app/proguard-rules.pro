# Gson lee DTOs del API, caché y estado guardado mediante reflexión.
# No cambiar sus nombres ni eliminar constructores/campos en release.
-keep class com.mindflow.nova.data.model.** { *; }
-keepattributes Signature,InnerClasses,EnclosingMethod
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken
-keepclassmembers class kotlin.Pair { *; }
# Cuerpos de error del backend que se leen con Gson fuera de data.model.
-keep class com.mindflow.nova.data.**ApiError { *; }

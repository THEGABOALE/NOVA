package com.mindflow.nova.data.model

/** Una mecánica desconocida permanece visible, pero nunca se ofrece como jugable. */
fun supportsMechanic(mechanic: String?): Boolean =
    mechanic in setOf("multiple_choice", "matching", "true_false")

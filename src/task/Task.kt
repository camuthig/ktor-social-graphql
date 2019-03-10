package org.camuthig.task

import io.requery.Entity
import io.requery.Key
import io.requery.Persistable
import java.util.*

@Entity
data class Task constructor(
    @get:Key
    var id: UUID,
    var title: String,
    var summary: String
): Persistable
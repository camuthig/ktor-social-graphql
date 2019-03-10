package org.camuthig.auth

import io.requery.*
import java.util.*

@Entity
@Table(name = "users")
interface User: Persistable {
    @get:Key
    @get:Generated
    // TODO Move this back to a UUID once https://github.com/requery/requery/issues/824 is released
    var id: Int
    var name: String
    var email: String
    var nickname: String
    var avatar_url: String?
}

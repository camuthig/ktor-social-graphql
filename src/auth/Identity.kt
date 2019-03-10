package org.camuthig.auth

import io.requery.*

@Table(name="user_identities")
@Entity
interface Identity: Persistable {
    @get:ForeignKey
    @get:ManyToOne
    @get:Column(name="user_id")
    var user: User

    var provider: String

    var id: String
}


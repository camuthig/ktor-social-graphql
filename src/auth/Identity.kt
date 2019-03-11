package org.camuthig.auth

import io.requery.*

@Entity
@Table(name="user_identities")
interface Identity: Persistable {
    @get:ForeignKey
    @get:ManyToOne
    @get:Column(name="user_id")
    var user: User

    @get:Key
    var provider: String

    @get:Key
    var id: String
}


package org.camuthig.domain

import io.requery.*
import org.camuthig.auth.User

@Entity(name = "TodoList")
@Table(name = "todo_lists")
interface TodoListEntity: Persistable {
    @get:Key
    @get:Generated
    // TODO Move this back to a UUID once https://github.com/requery/requery/issues/824 is released
    var id: Int
    var title: String
    var description: String?

    @get:Column(name = "created_by")
    @get:ForeignKey
    @get:ManyToOne
    var createdBy: User
}
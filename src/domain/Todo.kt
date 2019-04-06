package org.camuthig.domain

import io.requery.*
import org.camuthig.auth.User

@Entity
@Table(name = "todos")
interface Todo: Persistable {
    @get:Key
    @get:Generated
    // TODO Move this back to a UUID once https://github.com/requery/requery/issues/824 is released
    var id: Int
    var title: String
    var description: String?

    @get:Column(name = "is_completed")
    var completed: Boolean

    @get:Column(name = "assigned_to_id")
    @get:ForeignKey
    @get:ManyToOne
    var assignedTo: User?

    @get:Column(name = "todo_list_id")
    @get:ForeignKey
    @get:ManyToOne
    var todoList: TodoList?

    @get:Column(name = "created_by")
    @get:ForeignKey
    @get:ManyToOne
    var createdBy: User
}

package org.camuthig.graphql

import com.expedia.graphql.annotations.GraphQLContext
import io.ktor.auth.principal
import io.requery.kotlin.eq
import org.camuthig.auth.User
import org.camuthig.auth.UserPrincipal
import org.camuthig.domain.Todo
import org.camuthig.domain.TodoList
import org.camuthig.ktor.database

class TodoMutation {
    data class CreateTodoListInput(val title: String, val description: String?)

    fun createTodoList(input: CreateTodoListInput, @GraphQLContext context: ApplicationCallContext): TodoList {
        return database.invoke {
            val todoList = TodoList()
            todoList.title = input.title
            todoList.description = input.description
            todoList.createdBy = context.call.principal<UserPrincipal>()?.user!!

            insert(todoList)

            todoList
        }
    }

    data class CreateTodoInput(val title: String, val description: String?, val todoListId: Int?, val assignedToId: Int?)

    fun createTodo(input: CreateTodoInput, @GraphQLContext context: ApplicationCallContext): Todo {
        return database.invoke {
            val todoList: TodoList? = input.todoListId?.let {
                (select (TodoList::class) where (TodoList::id eq input.todoListId)).get().first()
            }

            val assignedTo: User? = input.assignedToId?.let {
                (select (User::class) where (User::id eq input.assignedToId)).get().first()
            }

            val todo = Todo()
            todo.title = input.title
            todo.description = input.description
            todo.todoList = todoList
            todo.assignedTo = assignedTo
            todo.createdBy = context.call.principal<UserPrincipal>()?.user!!

            insert(todo)

            todo
        }
    }

    // It isn't possible to support fully optional fields with Kotlin (see: https://github.com/ExpediaDotCom/graphql-kotlin/issues/50)
    // So this is really acting a bit outside the realms of the GraphQL spec, as a full update, always.
    data class UpdateTodoInput(val id: Int, val title: String, val completed: Boolean, val description: String? = null, val todoListId: Int? = null, val assignedToId: Int? = null)

    fun updateTodo(input: UpdateTodoInput, @GraphQLContext context: ApplicationCallContext): Todo {
        return database.invoke {
            val todo = (select (Todo::class) where (Todo::id eq input.id)).get().first()

            val todoList: TodoList? = input.todoListId?.let {
                (select (TodoList::class) where (TodoList::id eq input.todoListId)).get().first()
            }

            val assignedTo: User? = input.assignedToId?.let {
                (select (User::class) where (User::id eq input.assignedToId)).get().first()
            }

            todo.title = input.title
            todo.completed = input.completed
            todo.description = input.description
            todo.todoList = todoList
            todo.assignedTo = assignedTo

            update(todo)

            todo
        }
    }
}
package org.camuthig.graphql

import com.expedia.graphql.annotations.GraphQLContext
import io.ktor.auth.principal
import io.requery.kotlin.eq
import org.camuthig.auth.UserPrincipal
import org.camuthig.domain.Todo
import org.camuthig.domain.TodoList
import org.camuthig.ktor.database

class TodoQuery {
    fun getTodo(id: Int): Todo? {
        return database.invoke {
            (select (Todo::class) where (Todo::id eq id)).get().firstOrNull()
        }
    }

    data class ListTodosFilter(val todoListId: Int?, val isCompleted: Boolean?)

    fun listTodos(filter: ListTodosFilter): List<Todo> {
        return database.invoke {
            val query = select(Todo::class)

            filter.todoListId?.let {
                query.where(Todo::CREATED_BY_ID.get().eq(it))
            }

            filter.isCompleted?.let {
                query.where((Todo::completed eq it))
            }

            query.get().toList()
        }
    }

    fun listTodoLists(@GraphQLContext context: ApplicationCallContext): List<TodoList> {
        val user = context.call.principal<UserPrincipal>()?.user!!
        return database.invoke {
            val query = select(TodoList::class)
                .leftJoin(Todo::class).on(Todo::TODO_LIST_ID.get().eq(TodoList::ID.get()))
                .where (
                    (TodoList::CREATED_BY_ID.get().eq(user.id)).or
                    (Todo::CREATED_BY_ID.get().eq(user.id)).or
                    (Todo::ASSIGNED_TO_ID.get().eq(user.id))
                )

            query.get().toList()
        }
    }
}
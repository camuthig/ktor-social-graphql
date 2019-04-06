package org.camuthig.graphql

import com.expedia.graphql.annotations.GraphQLContext
import io.ktor.auth.principal
import io.requery.kotlin.eq
import org.camuthig.auth.UserPrincipal
import org.camuthig.domain.Todo
import org.camuthig.domain.TodoEntity
import org.camuthig.domain.TodoList
import org.camuthig.domain.TodoListEntity
import org.camuthig.ktor.database

class TodoQuery {
    fun getTodo(id: Int): Todo? {
        return database.invoke {
            (select (TodoEntity::class) where (TodoEntity::id eq id)).get().firstOrNull()
        }
    }

    data class ListTodosFilter(val todoListId: Int?, val isCompleted: Boolean?)

    fun listTodos(filter: ListTodosFilter): List<Todo> {
        return database.invoke {
            var query = select(Todo::class)

            filter.todoListId?.let {
                query.where(TodoEntity::CREATED_BY_ID.get().eq(it))
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
            var query = select(TodoList::class)
                .leftJoin(Todo::class).on(TodoEntity::TODO_LIST_ID.get().eq(TodoListEntity::ID.get()))
                .where (
                    (TodoListEntity::CREATED_BY_ID.get().eq(user.id)).or
                    (TodoEntity::CREATED_BY_ID.get().eq(user.id)).or
                    (TodoEntity::ASSIGNED_TO_ID.get().eq(user.id))
                )

            query.get().toList()
        }
    }
}
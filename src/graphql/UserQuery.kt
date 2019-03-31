package org.camuthig.graphql

import com.expedia.graphql.annotations.GraphQLContext
import io.ktor.auth.principal
import org.camuthig.auth.User
import org.camuthig.auth.UserPrincipal
import org.camuthig.auth.UserRepository

class UserQuery(private val userRepository: UserRepository) {
    fun getUser(id: Int): User? {
        return userRepository.getUser(id)
    }

    fun me(@GraphQLContext context: ApplicationCallContext): User? {
        return context.call.principal<UserPrincipal>()?.user
    }
}
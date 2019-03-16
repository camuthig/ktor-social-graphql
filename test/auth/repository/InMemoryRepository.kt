package org.camuthig.auth.repository

import org.camuthig.auth.User
import org.camuthig.auth.UserEntity
import org.camuthig.auth.UserRepository
import org.camuthig.auth.social.SocialIdentity

class InMemoryRepository: UserRepository {
    val users = mutableListOf<User>()
    val identities = mutableMapOf<String, User>()

    override fun getUser(id: Int): User? {
        return users.firstOrNull {
            it.id == id
        }
    }

    override fun getUserByEmail(email: String): User? {
        return users.firstOrNull {
            it.email == email
        }
    }

    override fun addUser(user: User) {
        if (user.id == 0) {
            user.id = users.count()+1
        }

        users.add(user)
    }

    override fun getUser(provider: String, socialIdentity: SocialIdentity): User? {
        return identities["$provider:${socialIdentity.email}"]
    }

    override fun linkIdentity(provider: String, socialIdentity: SocialIdentity): User {
        var user = getUser(provider, socialIdentity)

        if (user != null) {
            return user
        }

        user = getUserByEmail(socialIdentity.email)

        if (user == null) {
            user = UserEntity()
            user.email = socialIdentity.email
            user.name = socialIdentity.name
            user.nickname = socialIdentity.nickname
            user.avatarUrl = socialIdentity.avatar

            addUser(user)
        }

        identities["$provider:${socialIdentity.email}"] = user

        return user
    }
}
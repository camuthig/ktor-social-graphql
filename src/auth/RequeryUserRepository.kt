package org.camuthig.auth

import io.requery.Persistable
import io.requery.kotlin.*
import io.requery.sql.KotlinEntityDataStore
import org.camuthig.auth.social.SocialIdentity

class RequeryUserRepository(val database: KotlinEntityDataStore<Persistable>): UserRepository {
    override fun getUser(id: Int): User? {
        return database.invoke {
            (select (User::class) where (User::id eq id)).get().firstOrNull()
        }
    }

    override fun getUserByEmail(email: String): User? {
        return database.invoke {
            (select (User::class) where (User::email eq email)).get().firstOrNull()
        }
    }

    override fun addUser(user: User) {
        database.invoke {
            insert(user)
        }
    }

    override fun getUser(provider: String, socialIdentity: SocialIdentity): User? {
        return database.invoke {
            (select (Identity::class) where (Identity::provider eq provider) and (Identity::id eq socialIdentity.id))
                .get()
                .firstOrNull()
                ?.user
        }
    }

    override fun linkIdentity(provider: String, socialIdentity: SocialIdentity): User {
        return database.invoke {
            val identity = (select (Identity::class) where (Identity::provider eq provider) and (Identity::id eq socialIdentity.id))
                .get()
                .firstOrNull()

            if (identity != null) {
                identity.user
            } else {
                var user = getUserByEmail(socialIdentity.email)

                if (user == null) {
                    user = UserEntity()

                    user.name = socialIdentity.name
                    user.nickname = socialIdentity.nickname
                    user.email = socialIdentity.email
                    user.avatarUrl = socialIdentity.avatar

                    insert(user)
                }

                val newIdentity = IdentityEntity()

                newIdentity.id = socialIdentity.id
                newIdentity.provider = provider
                newIdentity.user = user

                insert(newIdentity)

                user
            }
        }
    }

}
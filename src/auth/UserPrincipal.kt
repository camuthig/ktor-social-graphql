package org.camuthig.auth

import io.ktor.auth.Principal

class UserPrincipal(val user: User): Principal
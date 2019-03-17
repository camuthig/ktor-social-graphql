package org.camuthig.graphql

import com.expedia.graphql.SchemaGeneratorConfig
import com.expedia.graphql.TopLevelObject
import com.expedia.graphql.toSchema
import graphql.ExecutionInput
import graphql.GraphQL
import graphql.schema.GraphQLSchema
import io.ktor.application.Application
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.http.content.defaultResource
import io.ktor.http.content.static
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.*
import org.camuthig.auth.UserRepository
import org.koin.ktor.ext.inject

data class GraphQLRequest(val query: String, val operationName: String, val variables: Map<String, Any>)

fun Application.installGraphQL() {
    val userRepository: UserRepository by inject()

    val config = SchemaGeneratorConfig(listOf("org.camuthig"))
    val queries = listOf(TopLevelObject(UserQuery(userRepository)))
    val schema: GraphQLSchema = toSchema(config = config, queries = queries)
    val graphQL = GraphQL.newGraphQL(schema).build()

    suspend fun ApplicationCall.executeQuery() {
        val request = receive<GraphQLRequest>()
        val executionInput = ExecutionInput.newExecutionInput()
            .context(ApplicationCallContext(this))
            .query(request.query)
            .operationName(request.operationName)
//            .variables(request.variables)
            .build()

        graphQL.execute(executionInput)

        respond(graphQL.execute(executionInput))
    }

    routing {
        post("/graphql") {
            call.executeQuery()
        }

        get("/graphql") {
            call.executeQuery()
        }

        static("/graphql-playground") {
            defaultResource("static/graphql/playground.html")
        }
    }
}
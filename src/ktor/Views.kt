package org.camuthig.ktor

import io.ktor.application.ApplicationCall
import io.ktor.html.Template
import io.ktor.html.respondHtmlTemplate
import io.ktor.http.HttpStatusCode
import kotlinx.html.HTML

/**
 * Respond using a view template and some sane defaults.
 *
 * This function is just syntatic sugar on the standard `respondHtmlTemplate` function provided by Ktor, giving defaults
 * for the statusCode and block arguments.
 */
suspend fun <TTemplate : Template<HTML>> ApplicationCall.respondView(
    template: TTemplate,
    statusCode: HttpStatusCode = HttpStatusCode.OK,
    block: TTemplate.() -> Unit = {}
) {
    respondHtmlTemplate(template, statusCode, block)
}
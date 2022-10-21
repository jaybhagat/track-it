package todo.console

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.http.*
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import todo.app.*

object APIConstants {
    const val API_BASE_URL = "http://localhost:8080"
    const val POST_SIGNUP = "/api/add/user"
    const val GET_LOGIN = "/api/authenticate"
}



object HttpRequest {
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
            })
        }
    }

    suspend fun signUpUser(username: String, hashedPass: String): BaseResponse {
        val response: HttpResponse = client.post(APIConstants.API_BASE_URL + APIConstants.POST_SIGNUP) {
            contentType(ContentType.Application.Json)
            setBody(User(username, hashedPass))
        }
        val body = Json.decodeFromString<List<BaseResponse>>(response.bodyAsText())
        return body[0]
    }

    suspend fun logInUser(username: String, hashedPass: String): BaseResponse {
        val response: HttpResponse = client.get(APIConstants.API_BASE_URL + APIConstants.GET_LOGIN) {
            url {
                parameters.append("username", username)
                parameters.append("password", hashedPass)
            }
        }
        val body = Json.decodeFromString<List<BaseResponse>>(response.bodyAsText())
        return body[0]
    }
}
package todo.dtos

import kotlinx.serialization.*

@Serializable
data class User(
    val username: String = "",
    val password: String = ""
) {
    init {
        println("Creating user...")
    }
}

@Serializable
data class Note(
    val id: Int = -1,
    val text: String = "",
    val priority: Int = -1,
    val gid: Int = -1,
    val last_edit: String = "",
    val due: String = "",
    val idx: Int = -1,
) { }

@Serializable
data class Group (
    val group_id: Int = -1,
    val group_name: String = ""
) {
    init {
        println("Creating group")
    }
}


@Serializable
data class BaseResponse(
    @SerialName("status")
    var status: Int = 1,
    @SerialName("message")
    var message: String = "",
    @SerialName("error")
    var error: String = ""
)

fun main() {
    println("dtos")
}
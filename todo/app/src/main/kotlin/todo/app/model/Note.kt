package todo.app.model

class Note(id: Int, gid: Int) {
    val id = id
    var text = ""
    var priority = 3
    var gid = gid
    val last_edit: String = ""
    val due: String = ""
}
package todo.app.model

class Group(id: Int) {
    val id = id
    var name = ""
    val notes = mutableListOf<Note>()
}
package todo.ui.model

class Group(id: Int) {
    val id = id
    var name = ""
    var notes = mutableListOf<Note>()
}
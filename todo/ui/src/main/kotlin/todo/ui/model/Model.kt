package todo.app.model

import javafx.beans.InvalidationListener
import javafx.beans.Observable


object Model: Observable {

    private val listeners = mutableListOf<InvalidationListener?>()

    var gidMappings : HashMap<String, Group>
            = HashMap<String, Group> ()

    override fun addListener(listener: InvalidationListener?) {
        listeners.add(listener)
    }
    override fun removeListener(listener: InvalidationListener?) {
        listeners.remove(listener)
    }

    fun addGroup(group_name: String, gid: Int){
        var new_group = Group(gid)
        gidMappings.put(group_name, new_group)
    }

    fun addNote(gname: String, gid: Int, note_id: Int, text: String, priority: Int, last_edit: String, due: String){
        var new_note = Note(note_id, gid)
        new_note.text = text
        new_note.priority = priority
        new_note.last_edit = last_edit
        new_note.due = due
        Model.gidMappings[gname]!!.notes.add(new_note)
    }

    /**
     * Call this function to broadcast any changes to view/listeners
     */
    fun broadcast() {
        listeners.forEach { it?.invalidated(this)}
    }

    /**
     * List of groups (group class in Group.kt)
     */
    val groups = mutableListOf<Group>()

    init {
        populate()
        gidMappings.put("noGroup", Group(-1))

    }

    /**
     * function to read database and populate groups and notes
     */
    fun populate() {
        /**
         * TO DO: Read from dtaabase
         */
        broadcast()
    }

}
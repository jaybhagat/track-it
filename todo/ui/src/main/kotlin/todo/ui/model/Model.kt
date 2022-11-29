package todo.ui.model

import javafx.beans.InvalidationListener
import javafx.beans.Observable
import todo.ui.view.sideBar
import kotlinx.coroutines.*
import todo.ui.view.NoteView
import todo.ui.*
import java.util.function.Predicate

object Model: Observable {

    private val listeners = mutableListOf<InvalidationListener?>()

    var gidMappings : HashMap<String, Group>
            = HashMap<String, Group> ()

    var gidtogname : HashMap<Int, String> = HashMap<Int, String> ()


    override fun addListener(listener: InvalidationListener?) {
        listeners.add(listener)
    }
    override fun removeListener(listener: InvalidationListener?) {
        listeners.remove(listener)
    }

    fun addGroup(group_name: String, gid: Int){
        var new_group = Group(gid)
        gidMappings.put(group_name, new_group)
        gidtogname.put(gid, group_name)
        sideBar.addGroup(gid, group_name)
    }

    fun addNote(gname: String, gid: Int, note_id: Int, text: String, priority: Int, last_edit: String, due: String, idx: Int){
        var new_note = Note(note_id, gid)
        new_note.text = text
        new_note.priority = priority
        new_note.last_edit = last_edit
        new_note.due = due
        new_note.idx = idx
        gidMappings[gname]!!.notes.add(new_note)
        broadcast()
    }

    fun deleteNote(gname: String, note_id: Int) {
        val check = Predicate { note: Note -> note.id == note_id }
        val deleted = gidMappings[gname]!!.notes.find { x: Note -> check.test(x) }
        gidMappings[gname]!!.notes.removeIf { x: Note -> check.test(x) }

        gidMappings[gname]!!.notes.forEach { x: Note ->
            if ( x.idx >= deleted!!.idx ) {
                x.idx--
                GlobalScope.launch(Dispatchers.IO) {
                    val response =
                        (async { HttpRequest.editTask(x.id, x.text, x.priority, x.gid, x.due, x.idx) }).await()
                    if (response.status != 1) {
                        println("There was an error editing that item: " + response.error)
                    }
                }
            }
        }
        broadcast()
    }

    fun editNote(gname: String, old_gid: Int, gid: Int, old_note_id: Int, note_id: Int, text: String, priority: Int, last_edit: String, due: String, idx: Int) {

        var note_idx = -1

        for (i in 0.. gidMappings[gname]!!.notes.size-1){
            if(gidMappings[gname]!!.notes[i].id == note_id){
                note_idx = i
                break
            }
        }
        if(old_gid != gid){
            deleteNote(gidtogname.getOrDefault(old_gid, "Ungrouped"), old_note_id)
            addNote(gname, gid, note_id, text, priority, last_edit, due, idx)
        }else{
            gidMappings[gname]!!.notes[note_idx].gid = gid
            gidMappings[gname]!!.notes[note_idx].text = text
            gidMappings[gname]!!.notes[note_idx].priority = priority
            gidMappings[gname]!!.notes[note_idx].last_edit = last_edit
            gidMappings[gname]!!.notes[note_idx].due = due
            gidMappings[gname]!!.notes[note_idx].idx = idx
            broadcast()
        }

    }


    fun swapNoteIndex(gname: String, note_id: Int, note_id2: Int, idx: Int, idx2: Int){
        gidMappings[gname]!!.notes[idx2].idx = idx
        gidMappings[gname]!!.notes[idx].idx = idx2
        val temp_note = gidMappings[gname]!!.notes[idx]
        gidMappings[gname]!!.notes[idx] =  gidMappings[gname]!!.notes[idx2]
        gidMappings[gname]!!.notes[idx2] = temp_note

        broadcast()
    }

    fun deleteGroup(gname: String) {
        gidMappings[gname]!!.notes.forEach {
            deleteNote(gname, it.id)
        }
        gidMappings.remove(gname)
        NoteView.remove(gname)
    }

    fun editGroup(old_gname: String, new_gname: String) {
        val duplicateGroup = gidMappings[old_gname]
        gidMappings.remove(old_gname)
        if (duplicateGroup != null) {
            gidMappings.put(new_gname, duplicateGroup)
        }
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
        addGroup("Ungrouped", -1)
    }

    /**
     * function to read database and populate groups and notes
     */
    fun populate() {
        GlobalScope.launch(Dispatchers.IO) {
            val groups = (async { HttpRequest.getGroups() }).await()
            groups.forEachIndexed { i, item ->
                var new_group = Group(item.get("group_id")!!.toInt())
                new_group.name = item.get("group_name").orEmpty()
                gidMappings.put(item.get("group_name").orEmpty(), new_group)
                gidtogname.put(item.get("group_id")!!.toInt(), item.get("group_name").orEmpty())
            }
            for((gname, grp) in gidMappings){
                val notes = (async { HttpRequest.getTasksFromGroup(grp.id) }).await()
                notes.forEachIndexed { i, item ->
                    gidMappings[gname]!!.notes.add(Note(-1,-2))
                }

                notes.forEachIndexed { i, item ->
                    var new_note = Note(item.get("id")!!.toInt(), grp.id)
                    new_note.text = item.get("text").orEmpty()
                    new_note.priority = item.get("priority")!!.toInt()
                    new_note.last_edit = item.getOrDefault("last_edit", "No last edit")
                    new_note.due = item.getOrDefault("due", "no due date")
                    new_note.idx = item.get("idx")!!.toInt()
                    gidMappings[gname]!!.notes[new_note.idx] = new_note
                }
            }
            sideBar.createGroups(initial = true)
            broadcast()
        }

    }

}

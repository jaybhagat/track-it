package todo.app.model

import javafx.beans.InvalidationListener
import javafx.beans.Observable
import todo.app.view.sideBar
import io.ktor.http.*
import kotlinx.coroutines.*
import todo.console.*
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
        sideBar.createGroups()
    }

    fun addNote(gname: String, gid: Int, note_id: Int, text: String, priority: Int, last_edit: String, due: String, idx: Int){
        var new_note = Note(note_id, gid)
        new_note.text = text
        new_note.priority = priority
        new_note.last_edit = last_edit
        new_note.due = due
        new_note.idx = idx
        println(gname)
        gidMappings[gname]!!.notes.add(new_note)
        broadcast()
    }

    fun deleteNote(gname: String, note_id: Int) {
        println(gname)
        val check = Predicate { note: Note -> note.id == note_id }
        val deleted = gidMappings[gname]!!.notes.find { x: Note -> check.test(x) }
        gidMappings[gname]!!.notes.removeIf { x: Note -> check.test(x) }

        gidMappings[gname]!!.notes.forEach { x: Note ->
            if ( x.idx >= deleted!!.idx ) { x.idx-- }
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

        println(old_gid)
        println(gid)
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

    fun deleteGroup(gname: String) {
        gidMappings[gname]!!.notes.forEach {
            deleteNote(gname, it.id)
        }
        gidMappings.remove(gname)
        sideBar.createGroups()
    }

    fun editGroup(old_gname: String, new_gname: String) {

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
                println(item.get("group_name").orEmpty())
                var new_group = Group(item.get("group_id")!!.toInt())
                new_group.name = item.get("group_name").orEmpty()
                gidMappings.put(item.get("group_name").orEmpty(), new_group)
                gidtogname.put(item.get("group_id")!!.toInt(), item.get("group_name").orEmpty())
            }
            for((gname, grp) in gidMappings){
                val notes = (async { HttpRequest.getTasksFromGroup(grp.id) }).await()
                notes.forEachIndexed { i, item ->
                    var new_note = Note(item.get("id")!!.toInt(), grp.id)
                    new_note.text = item.get("text").orEmpty()
                    println("inital notes")
                    println(new_note.text)
                    new_note.priority = item.get("priority")!!.toInt()
                    new_note.last_edit = item.getOrDefault("last_edit", "No last edit")
                    new_note.due = item.getOrDefault("due", "no due date")
                    gidMappings[gname]!!.notes.add(new_note)
                }
            }
            sideBar.createGroups()

            broadcast()
        }

    }

}
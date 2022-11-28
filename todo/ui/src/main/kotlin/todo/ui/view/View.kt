package todo.app.view

import javafx.beans.InvalidationListener
import javafx.beans.Observable
import javafx.scene.control.ScrollPane
import javafx.scene.layout.BorderPane
import javafx.scene.layout.VBox
import todo.app.model.Model
import javafx.scene.control.*
import javafx.geometry.Insets
import javafx.scene.text.Font
import javafx.scene.layout.*
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.stage.Stage
import javafx.stage.Modality;
import io.ktor.http.*
import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.scene.paint.Color
import kotlinx.coroutines.*
import todo.app.model.Note
import todo.console.*
import java.text.DateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar


class View: BorderPane(), InvalidationListener {
    val side_bar = sideBar
    val tool_bar = toolBar()
    init {
        Model.addListener(this)
        invalidated(null)
        left = side_bar.left
        bottom = tool_bar.bottom_box
        minWidth = 400.0
        center = NoteView
    }

    /**
     * Function that is called whenever Model broadcasts a change
     */
    override fun invalidated(observable: Observable?) {

    }
}

class GroupBox(val gid: Int, var name: String): HBox(), InvalidationListener {
    var old_name: String = ""
    val text = TextField().apply {
        background = Background(BackgroundFill(Color.LIGHTGREY, CornerRadii(0.0), Insets(0.0) ))
        text = name
        focusedProperty().addListener { observable, oldValue, newValue ->
            if (newValue) {
                old_name = text
            }
            if (!newValue) {
                if (Model.gidMappings.containsKey(text)) {
                    text = old_name
                }
                else {
                    GlobalScope.launch(Dispatchers.IO) {
                        val response =
                            (async { HttpRequest.editGroup(gid, text) }).await()

                        if (response.status != 1) {
                            println("There was an error editing that item: " + response.error)
                        } else {
                            name = text
                            Model.editGroup(old_name, name)
                            if (NoteView.display_groups.contains(old_name)) {
                                NoteView.display_groups.remove(old_name)
                                NoteView.display_groups.add(name)
                            }
                        }
                    }
                }
            }
        }
    }
    val delete = Button("X").apply {
        setOnAction {
            GlobalScope.launch(Dispatchers.IO) {
            checkBox.isSelected = false
            val deleteNotes = mutableListOf<Note>()
            Model.gidMappings[name]!!.notes.forEach {
                deleteNotes.add(it)
            }
            deleteNotes.forEach {
                    val response = (async { HttpRequest.deleteTask(it.id.toString()) }).await()

                    if (!response.status.isSuccess()) {
                        println("There was an error in deleting the note.")
                    } else {
                        Model.deleteNote(name, it.id)
                    }
            }
                    val response = (async { HttpRequest.deleteGroup(gid.toString()) }).await()

                    if (!response.status.isSuccess()) {
                        println("There was an error in deleting the note.")
                    } else {
                        Model.deleteGroup(name)
                    }
                }
            }
    }

    val checkBox = CheckBox().apply {
        setOnAction {
            if (isSelected) {
                NoteView.show(name)
            }
            else {
                NoteView.remove(name)
            }
        }
    }
    init {
        spacing = 10.0
        children.add(checkBox)
        children.add(text)
        if (name != "Ungrouped") {
            children.add(delete)
        }

        Model.addListener(this)
        invalidated(null)

        if (name == "Ungrouped") {
            checkBox.isSelected = true
            NoteView.show(name)
        }
    }

    override fun invalidated(observable: Observable?) {
        if (checkBox.isSelected) {
            NoteView.show(name)
        }
    }
}



object sideBar {
    var groups_box = VBox().apply{
        spacing = 10.0
    }
    var left = ScrollPane(groups_box).apply {
        vbarPolicy = ScrollPane.ScrollBarPolicy.ALWAYS
    }.apply{
        padding = Insets(10.0, 10.0, 10.0, 10.0)
    }

    val grp_text = TextField("Group name")
    val create_btn = Button("+")
    val create_group = HBox(grp_text, create_btn).apply{
        spacing = 10.0
    }
    val label_grp = Label("Groups").apply {
        setAlignment(Pos.CENTER);
        font = Font("Arial", 18.0)
    }
    init{
        groups_box.children.add(label_grp)
        groups_box.children.add(create_group)
        create_btn.setOnAction(){
            if (Model.gidMappings.contains(grp_text.text)) {
                grp_text.text = "Group name"
            }
            else {
                GlobalScope.launch(Dispatchers.IO) {
                    val response =
                        (async { HttpRequest.addGroup(grp_text.getText()) }).await()

                    if (response.status != 1) {
                        println("There was an error editing that group: " + response.error)
                    } else {
                        println("Group added!\n")
                        var gid = response.message.toIntOrNull() ?: -1
                        Model.addGroup(grp_text.getText(), gid)
                    }
                    grp_text.text = "Group name"
                }
            }
        }
    }

    fun createGroups() {
        Platform.runLater {
            groups_box.children.clear()
            groups_box.children.add(label_grp)
            groups_box.children.add(create_group)
            Model.gidMappings.forEach {
                groups_box.children.add(GroupBox(it.value.id, it.key))
            }
        }
    }

}


class toolBar(){
    val add_task = Button("Add task")
    val rightAlign = Pane()
    var bottom_box = HBox()

    init{
        HBox.setHgrow(rightAlign, Priority.ALWAYS)
        bottom_box = HBox(add_task).apply{
            spacing = 10.0
            padding = Insets(10.0)
        }
        add_task.setOnAction(){
            openModal()
        }

    }

    fun openModal() = runBlocking<Unit>{
        val dialog = Stage();
        dialog.title = "Create a new note!"
        dialog.initModality(Modality.APPLICATION_MODAL);
        //dialog.initOwner(primaryStage);

        val text_note = TextField("New note")
        val text_group = ComboBox(FXCollections.observableArrayList("Ungrouped"))
        Model.gidMappings.forEach {
            if (it.key != "Ungrouped") {
                text_group.items.add(it.key)
            }
        }
        text_group.promptText = "Pick Group"
        val due_date = DatePicker()
        val formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy")


        val low_prio = CheckBox("Low")
        val med_prio = CheckBox("Med")
        val high_prio = CheckBox("High")
        low_prio.setOnAction{
            high_prio.setSelected(false)
            med_prio.setSelected(false)
        }
        med_prio.setOnAction{
            low_prio.setSelected(false)
            high_prio.setSelected(false)
        }
        high_prio.setOnAction{
            low_prio.setSelected(false)
            med_prio.setSelected(false)
        }

        val priority_box = HBox(low_prio, med_prio, high_prio).apply{
            spacing = 5.0
        }
        val create_note = Button("Create Note")
        create_note.setOnAction(){
            GlobalScope.launch(Dispatchers.IO) {
                var gid = -1
                var group_text = text_group.value
                if (Model.gidMappings.containsKey(group_text)) {
                    val id = Model.gidMappings[group_text]!!.id
                    gid = id
                }
                var priority = 1
                if (low_prio.isSelected) {
                    priority = 3
                } else if (med_prio.isSelected) {
                    priority = 2
                }
                println(due_date.value.format(formatter))
                var index = 0
                if(Model.gidMappings.containsKey(group_text)){
                    index =  Model.gidMappings[group_text]!!.notes.size
                }
                val response =
                    (async { HttpRequest.addTask(text_note.getText(), priority, gid, due_date.value.format(formatter), index) }).await()


                if (response.status != 1) {
                    println("There was an error editing that item: " + response.error)
                } else {
                    println("Item edited!\n")
                    var note_id = response.message.toIntOrNull() ?: -1
                    val c = Calendar.getInstance()
                    val year = c.get(Calendar.YEAR).toString()
                    val month = c.get(Calendar.MONTH).toString()
                    val day = c.get(Calendar.DAY_OF_MONTH).toString()
                    val last_edit = month + "/" + day + "/" + year
                    println(last_edit)
                    if (!Model.gidMappings.containsKey(group_text)) {
                        group_text = "Ungrouped"
                        println("Adding to no group because group you entered doesn't exist")
                    }
                    Model.addNote(group_text, gid, note_id, text_note.getText(), priority, last_edit, due_date.value.format(formatter), index)
                }

                text_note.text = "New note"
                high_prio.setSelected(false)
                med_prio.setSelected(false)
                low_prio.setSelected(false)
            }
        }

        val dialogVbox = VBox(text_note, text_group, due_date, priority_box, create_note).apply{
            spacing = 10.0
            padding = Insets(10.0)
        }
        val dialogScene = Scene(dialogVbox, 300.0, 200.0);

        dialog.setScene(dialogScene);
        dialog.show();
    }
}

class NoteBox(var gname: String, var gid: Int, var note_id: Int, var tex: String, var priority: Int, var last_edit: String, var due: String): VBox() {
    val textField = TextField().apply {
        background = Background(BackgroundFill(Color.LIGHTGREY, CornerRadii(0.0), Insets(0.0)))
        text = tex
    }
    val priority_label = Label("priority: " + when(priority) {1 -> "High" 2 -> "Medium" else -> "Low"})
    val due_label = Label("due: " + due)
    val delete_button = Button("X")
    val edit = Button("Edit")

    val reorder_down = Button("down").apply {
        Color.TRANSPARENT
    }
    val reorder_up = Button("up")
    init {
        children.add(HBox(textField, edit, delete_button).apply {
            spacing = 10.0
        })
        children.add(HBox(priority_label, due_label, reorder_up, reorder_down).apply {
            spacing = 10.0
        })
        spacing = 10.0
        padding = Insets(10.0)

        delete_button.setOnAction() {
            deleteNote()
        }

        edit.setOnAction(){
            openModal()
        }
    }


    fun openModal() = runBlocking<Unit>{
        var note_idx = 0

        for (i in 0..Model.gidMappings[gname]!!.notes.size-1){
            if(Model.gidMappings[gname]!!.notes[i].id == note_id){
                note_idx = i
                break
            }
        }

        var note = Model.gidMappings[gname]!!.notes[note_idx]

        val dialog = Stage();
        dialog.title = "Edit note!"
        dialog.initModality(Modality.APPLICATION_MODAL);

        val text_note = TextField(note.text)
//        val text_group = TextField(gname)
        val text_group = ComboBox(FXCollections.observableArrayList("Ungrouped"))
        Model.gidMappings.forEach {
            if (it.key != "Ungrouped") {
                text_group.items.add(it.key)
            }
        }
        text_group.promptText = "Pick Group"

        val formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy")
        var date = LocalDate.parse(note.due, formatter)
        val due_date = DatePicker(date)


        val low_prio = CheckBox("Low")
        val med_prio = CheckBox("Med")
        val high_prio = CheckBox("High")
        if(note.priority == 1){
            low_prio.setSelected(true)
        }else if(note.priority == 2){
            med_prio.setSelected(true)
        }else{
            high_prio.setSelected(true)
        }

        low_prio.setOnAction{
            high_prio.setSelected(false)
            med_prio.setSelected(false)
        }
        med_prio.setOnAction{
            low_prio.setSelected(false)
            high_prio.setSelected(false)
        }
        high_prio.setOnAction{
            low_prio.setSelected(false)
            med_prio.setSelected(false)
        }

        val priority_box = HBox(low_prio, med_prio, high_prio).apply{
            spacing = 5.0
        }
        val edit_note = Button("Edit Note")
        edit_note.setOnAction(){
            GlobalScope.launch(Dispatchers.IO) {
                var gid = -1
                var group_text = text_group.value
                if (Model.gidMappings.containsKey(group_text)) {
                    val id = Model.gidMappings[group_text]!!.id
                    gid = id
                }
                var priority = 1
                if (low_prio.isSelected) {
                    priority = 3
                } else if (med_prio.isSelected) {
                    priority = 2
                }

                val response =
                    (async { HttpRequest.editTask(note.id, text_note.getText(), priority, gid, due_date.value.format(formatter), note.idx) }).await()

                if (response.status != 1) {
                    println("There was an error editing that item: " + response.error)
                } else {
                    println("Item edited!\n")
                    var note_id = response.message.toIntOrNull() ?: -1
                    val c = Calendar.getInstance()
                    val year = c.get(Calendar.YEAR).toString()
                    val month = c.get(Calendar.MONTH).toString()
                    val day = c.get(Calendar.DAY_OF_MONTH).toString()
                    val last_edit = month + "/" + day + "/" + year
                    println(last_edit)
                    if (!Model.gidMappings.containsKey(group_text)) {
                        group_text = "Ungrouped"
                        println("Adding to no group because group you entered doesn't exist")
                    }

                    Model.editNote(group_text, note.gid, gid, note.id, note_id, text_note.getText(), priority, last_edit, due_date.value.format(formatter), note.idx)
                }

            }
        }

        val dialogVbox = VBox(text_note, text_group, due_date, priority_box, edit_note).apply{
            spacing = 10.0
            padding = Insets(10.0)
        }
        val dialogScene = Scene(dialogVbox, 300.0, 200.0);

        dialog.setScene(dialogScene);
        dialog.show();
    }

    fun deleteNote() = runBlocking<Unit> {
        GlobalScope.launch(Dispatchers.IO) {
            val response = (async { HttpRequest.deleteTask(note_id.toString()) }).await()

            if ( !response.status.isSuccess()) {
                println("There was an error in deleting the note.")
            } else {
                Model.deleteNote(gname, note_id)
            }
        }
    }
}


object NoteView: VBox() {
    var display_groups = mutableSetOf<String>()
    fun show(gname: String) {
        Platform.runLater {
            val removeList = mutableListOf<String>()
            display_groups.add(gname)
            children.clear()
            println(display_groups)
            display_groups.forEach {
                if (Model.gidMappings.contains(it)) {
                    Model.gidMappings[it]!!.notes.forEach {
                        children.add(NoteBox(gname, it.gid, it.id, it.text, it.priority, it.last_edit, it.due))
                    }
                }
                else {
                    removeList.add(it)
                }
            }
            removeList.forEach {
                display_groups.remove(it)
            }
        }
            /**
             * For sorting, we can simply sort the children's list by index
             */
    }
    fun remove(gname: String) {
        if (display_groups.contains(gname)) {
            display_groups.remove(gname)
        }
        children.clear()
        display_groups.forEach {
            Model.gidMappings[it]!!.notes.forEach {
                children.add(NoteBox(gname, it.gid, it.id, it.text, it.priority, it.last_edit, it.due))
            }
        }
    }
}
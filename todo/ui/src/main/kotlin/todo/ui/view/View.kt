package todo.ui.view

import io.ktor.http.*
import javafx.application.Platform
import javafx.beans.InvalidationListener
import javafx.beans.Observable
import javafx.beans.binding.Bindings
import javafx.collections.FXCollections
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.stage.Modality
import javafx.stage.Stage
import kotlinx.coroutines.*
import todo.ui.*
import todo.ui.model.Model
import todo.ui.model.Note
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*


class View: BorderPane(), InvalidationListener {
    val side_bar = sideBar
    val tool_bar = toolBar
    init {
        Model.addListener(this)
        invalidated(null)
        left = side_bar.left
        bottom = tool_bar.bottom_box
        minWidth = 700.0
        center = ScrollPane(NoteView).apply {
            isFitToWidth = true

        }
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
            deleteGroup()
        }
    }

    fun deleteGroup() {
        GlobalScope.launch(Dispatchers.IO) {
            checkBox.isSelected = false

            if (name != "Ungrouped") {
                val deleteNotes = mutableListOf<Note>()
                Model.gidMappings[name]!!.notes.forEach {
                    deleteNotes.add(it)
                }
                deleteNotes.forEach {
                    val response = (async { HttpRequest.deleteTask(it.id) }).await()
                    if (!response.status.isSuccess()) {
                        println("There was an error in deleting the note.")
                    } else {
                        Model.deleteNote(name, it.id)
                    }
                }
                val response = (async { HttpRequest.deleteGroup(gid) }).await()

                if (!response.status.isSuccess()) {
                    println("There was an error in deleting the note.")
                } else {
                    Model.deleteGroup(name)
                    sideBar.deleteGroup(this@GroupBox)
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
        setAlignment(Pos.CENTER)
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

    fun createGroups(initial: Boolean = false) {
        Platform.runLater {
            groups_box.children.clear()
            groups_box.children.add(label_grp)
            groups_box.children.add(create_group)
            Model.gidMappings.forEach {
                val newGroup = GroupBox(it.value.id, it.key)
                newGroup.backgroundProperty().bind(Bindings
                    .`when`(newGroup.focusedProperty())
                    .then( Background(BackgroundFill(Color.LIGHTSLATEGREY, CornerRadii(0.0), Insets(0.0))) )
                    .otherwise(Background(BackgroundFill(Color.TRANSPARENT, CornerRadii(0.0), Insets(0.0))))
                )
                newGroup.setOnMouseClicked {
                    newGroup.requestFocus()
                }
                if (initial && it.key == "Ungrouped") {
                    newGroup.checkBox.isSelected = true
                    NoteView.show("Ungrouped")
                }
                groups_box.children.add(newGroup)
            }
        }
    }
    fun addGroup(gid: Int, gname: String) {
        Platform.runLater {
            val newGroup = GroupBox(gid, gname)
            newGroup.setOnMouseClicked {
                newGroup.requestFocus()
            }
            groups_box.children.add(newGroup)
        }
    }

    fun deleteGroup(node: Node) {
        Platform.runLater {
            groups_box.children.remove(node)
        }
    }

}

object toolBar {
    val add_task = Button("Add task")
    val rightAlign = Pane()
    val filter_text = Label("Filter By:")
    val filter_opts = listOf<String>("-", "Low", "Med", "High")
    val obs_list = FXCollections.observableList(filter_opts)
    val filter_menu = ChoiceBox(obs_list)
    var bottom_box = HBox()
    var filter_by = 0
    var is_filter = false

    init {
        HBox.setHgrow(rightAlign, Priority.ALWAYS)
        bottom_box = HBox(add_task, filter_text, filter_menu).apply{
            spacing = 10.0
            padding = Insets(10.0)
        }
        add_task.setOnAction(){
            openModal()
        }
        filter_menu.apply {
            setOnAction {
                if (value == "Low") {
                    filter_by = 3
                    is_filter = true
                } else if (value == "Med") {
                    filter_by = 2
                    is_filter = true
                } else if (value == "High") {
                    filter_by = 1
                    is_filter = true
                } else if (value == "-") {
                    filter_by = 0
                    is_filter = false
                }
                NoteView.display()
            }
        }
    }

    fun openModal() = runBlocking<Unit>{
        val dialog = Stage()
        dialog.title = "Create a new note!"
        dialog.initModality(Modality.APPLICATION_MODAL)

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
    val priority_label = Label("Priority: " + when(priority) {1 -> "High" 2 -> "Medium" else -> "Low"}).apply {
        background = Background(BackgroundFill(Color.LIGHTGREY, CornerRadii(0.0), Insets(0.0)))
        padding = Insets(5.0)
    }
    val due_label = Label("Due: " + due).apply {
        background = Background(BackgroundFill(Color.LIGHTGREY, CornerRadii(0.0), Insets(0.0)))
        padding = Insets(5.0)
    }
    val delete_button = Button("X").apply {
        background = Background(BackgroundFill(Color.RED, CornerRadii(0.0), Insets(0.0)))
        setBorder(Border(BorderStroke(Color.DARKRED, BorderStrokeStyle.SOLID, null, null)))
    }
    val edit = Button("Edit").apply {
        padding = Insets(5.0)
        prefWidth = 60.0
    }

    val reorder_down = Button("Down").apply {
        background = Background(BackgroundFill(Color.LIGHTPINK, CornerRadii(0.0), Insets(0.0)))
        padding = Insets(5.0,0.0,5.0,0.0)
        prefWidth = 60.0
    }
    val reorder_up = Button("Up").apply {
        background = Background(BackgroundFill(Color.LIGHTGREEN, CornerRadii(0.0), Insets(0.0)))
        padding = Insets(5.0,0.0,5.0,0.0)
        prefWidth = 60.0
    }
    val tmpPane = Pane()
    init {
        children.add(HBox(textField, delete_button).apply {
            spacing = 10.0
        })
        HBox.setHgrow(textField, Priority.ALWAYS)
        children.add(HBox(priority_label, due_label, reorder_up, reorder_down, tmpPane, edit).apply {
            spacing = 10.0
        })
        HBox.setHgrow(tmpPane, Priority.ALWAYS)
        spacing = 10.0
        padding = Insets(10.0)
        isFillWidth = true
        setBorder(Border(BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, null, null)))

        delete_button.setOnAction() {
            deleteNote()
        }

        edit.setOnAction(){
            openModal()
        }

        reorder_up.setOnAction(){
            moveUp()
        }

        reorder_down.setOnAction(){
            moveDown()
        }
    }


    fun getNoteIdx(gname: String, note_id: Int): Int{
        var note_idx = 0

        for (i in 0..Model.gidMappings[gname]!!.notes.size-1){
            if(Model.gidMappings[gname]!!.notes[i].id == note_id){
                note_idx = i
                break
            }
        }

        return note_idx
    }


    fun moveNote(newIdx: Int, prevIdx: Int){
        var note_need_swapping = Model.gidMappings[gname]!!.notes[newIdx]
        var note = Model.gidMappings[gname]!!.notes[prevIdx]

        GlobalScope.launch(Dispatchers.IO) {
            val response =
                (async { HttpRequest.editTask(note.id, note.text, note.priority, note.gid, note.due, newIdx) }).await()

            val response_note_swap =
                (async { HttpRequest.editTask(note_need_swapping.id, note_need_swapping.text, note_need_swapping.priority, note_need_swapping.gid, note_need_swapping.due, prevIdx) }).await()

            if (response.status != 1 && response_note_swap.status != -1) {
                println("There was an error editing that item: " + response.error)
            } else {
                println("Item edited!\n")
                var note_id = response.message.toIntOrNull() ?: -1
                var other_note_id = response_note_swap.message.toIntOrNull() ?: -1

                Model.swapNoteIndex(gname, note_id, other_note_id, newIdx, prevIdx)
            }
        }
    }


    fun moveDown(){

        val note_idx = getNoteIdx(gname, note_id)

        if(Model.gidMappings[gname]!!.notes.size-1 >= note_idx + 1){
            moveNote(note_idx + 1, note_idx)
        }

    }


    fun moveUp(){

        val note_idx = getNoteIdx(gname, note_id)

        var note = Model.gidMappings[gname]!!.notes[note_idx]
        if(0 <= note_idx - 1){
            moveNote(note_idx - 1, note_idx)
        }
    }


    fun openModal() = runBlocking<Unit>{

        var note_idx = getNoteIdx(gname, note_id)
        var note = Model.gidMappings[gname]!!.notes[note_idx]

        val dialog = Stage();
        dialog.title = "Edit note!"
        dialog.initModality(Modality.APPLICATION_MODAL);

        val text_note = TextField(note.text)
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

                if(note.gid != gid){
                    note_idx = Model.gidMappings[group_text]!!.notes.size
                }

                val response =
                    (async { HttpRequest.editTask(note.id, text_note.getText(), priority, gid, due_date.value.format(formatter), note_idx) }).await()

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
            val response = (async { HttpRequest.deleteTask(note_id) }).await()

            if ( !response.status.isSuccess()) {
                println("There was an error in deleting the note.")
            } else {
                Model.deleteNote(gname, note_id)
            }
        }
    }
}


object NoteView: VBox() {

    init {
        isFillWidth = true
        spacing = 10.0
        padding = Insets(10.0)
    }

    var display_groups = mutableSetOf<String>()
    fun show(gname: String) {
        Platform.runLater {
            display_groups.add(gname)
            display()
        }
        /**
         * For sorting, we can simply sort the children's list by index
         */
    }

    fun remove(gname: String) {
        if (display_groups.contains(gname)) {
            display_groups.remove(gname)
        }
        display()
    }

    fun display() {
        Platform.runLater {
            val removeList = mutableListOf<String>()

            children.clear()
            display_groups.forEach {
                val name = it
                if (Model.gidMappings.contains(it)) {
                    children.add(Label(name).apply {
                        font = Font.font(15.0)
                        alignment = Pos.TOP_CENTER
                        maxWidth = Double.MAX_VALUE
                        background = Background(BackgroundFill(Color.BLACK, CornerRadii(0.0), Insets(0.0)))
                        padding = Insets(5.0)
                        textFill = Color.WHITE

                    })
                    Model.gidMappings[it]!!.notes.forEach {
                        if (toolBar.is_filter) {
                            if (it.priority == toolBar.filter_by) {
                                val nb = NoteBox(name, it.gid, it.id, it.text, it.priority, it.last_edit, it.due)
                                nb.backgroundProperty().bind(Bindings
                                        .`when`(nb.focusedProperty())
                                        .then(Background(BackgroundFill(Color.LIGHTSLATEGREY, CornerRadii(0.0), Insets(0.0))))
                                        .otherwise(Background(BackgroundFill(Color.TRANSPARENT, CornerRadii(0.0), Insets(0.0))))
                                )
                                nb.setOnMouseClicked {
                                    nb.requestFocus()
                                }
                                children.add(nb)
                            }
                        } else {
                            val nb = NoteBox(name, it.gid, it.id, it.text, it.priority, it.last_edit, it.due)
                            nb.backgroundProperty().bind(Bindings
                                .`when`(nb.focusedProperty())
                                .then(Background(BackgroundFill(Color.LIGHTSLATEGREY, CornerRadii(0.0), Insets(0.0))))
                                .otherwise(Background(BackgroundFill(Color.TRANSPARENT, CornerRadii(0.0), Insets(0.0))))
                            )
                            nb.setOnMouseClicked {
                                nb.requestFocus()
                            }
                            children.add(nb)
                          }
                        }
                    } else {
                    removeList.add(it)
                }
            }
            removeList.forEach {
                display_groups.remove(it)
            }
        }
    }
}
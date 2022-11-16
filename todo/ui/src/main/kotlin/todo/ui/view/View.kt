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
import kotlinx.coroutines.*
import todo.console.*
import java.util.Calendar


class View: BorderPane(), InvalidationListener {
    val side_bar = sideBar()
    val tool_bar = toolBar()
    init {
        Model.addListener(this)
        invalidated(null)
        left = side_bar.left
        bottom = tool_bar.bottom_box
        minWidth = 400.0
    }

    /**
     * Function that is called whenever Model broadcasts a change
     */
    override fun invalidated(observable: Observable?) {

    }
}

class sideBar(){
    var groups_box = VBox().apply{
        spacing = 10.0
    }
    var left = ScrollPane(groups_box).apply {
        vbarPolicy = ScrollPane.ScrollBarPolicy.ALWAYS
    }.apply{
        padding = Insets(10.0, 10.0, 10.0, 10.0)
    }

    init{
        val label_grp = Label("Groups")
        label_grp.setAlignment(Pos.CENTER);
        label_grp.font = Font("Arial", 18.0)
        groups_box.children.add(label_grp)
        val grp_text = TextField("Group name")
        val create_btn = Button("+")
        val create_group = HBox(grp_text, create_btn).apply{
            spacing = 10.0
        }
        groups_box.children.add(create_group)
        create_btn.setOnAction(){
            GlobalScope.launch(Dispatchers.IO) {
                val response =
                    (async { HttpRequest.addGroup(grp_text.getText())}).await()

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
        val text_group = TextField("Add to existing group")
        val due_date = TextField("Due Date (month/day/year)")

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
                val group_text = text_group.getText()
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
                    (async { HttpRequest.addTask(text_note.getText(), priority, gid, due_date.getText()) }).await()

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
                    Model.addNote(group_text, gid, note_id, text_note.getText(), priority, last_edit, due_date.getText())
                }

                text_note.text = "New note"
                text_group.text = "Add to existing group"
                due_date.text = "Due Date (month/day/year)"
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
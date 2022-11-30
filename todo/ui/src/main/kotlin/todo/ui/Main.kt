package todo.ui

import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination
import javafx.scene.input.KeyEvent
import javafx.stage.Stage
import todo.ui.view.GroupBox
import todo.ui.view.NoteBox
import todo.ui.view.View
import todo.ui.view.toolBar
import java.io.*
import java.util.*


class ToDoApplication: Application() {
    override fun start(stage: Stage) {
        val new_item = KeyCodeCombination(KeyCode.N, KeyCombination.SHORTCUT_DOWN)
        val delete_item = KeyCodeCombination(KeyCode.D, KeyCombination.SHORTCUT_DOWN)
        val edit_item = KeyCodeCombination(KeyCode.E, KeyCombination.SHORTCUT_DOWN)
        stage.title = "TrackIt"
        stage.scene = Scene(View())
        stage.scene.addEventHandler(KeyEvent.KEY_RELEASED) {
            if (new_item.match(it)) {
                toolBar.openModal()
            } else if (delete_item.match(it)) {
                if ( stage.scene.focusOwnerProperty().get() is NoteBox ) {
                    (stage.scene.focusOwnerProperty().get() as NoteBox).deleteNote()
                } else if ( stage.scene.focusOwnerProperty().get() is GroupBox ) {
                    (stage.scene.focusOwnerProperty().get() as GroupBox).deleteGroup()
                }
            } else if (edit_item.match(it)) {
                if ( stage.scene.focusOwnerProperty().get() is NoteBox) {
                    (stage.scene.focusOwnerProperty().get() as NoteBox).openModal()
                }
            }
        }
        stage.apply {
            minWidth = 800.0
            minHeight = 600.0
            try {
                val s = Scanner(File("dimensions.txt"))
                val line: String = s.nextLine()
                width =  line.toDouble()
                val line2: String = s.nextLine()
                height = line2.toDouble()

            } catch (ex: FileNotFoundException) {
                width = minWidth
                height = minHeight
            }
            setOnHiding { event ->
                try {
                    BufferedWriter(FileWriter("dimensions.txt")).use { bf ->
                        bf.write("${width}")
                        bf.newLine()
                        bf.write("${height}")
                    }
                } catch (ex: IOException) {
                    println("Problem saving")
                }
            }
        }
        stage.show()
    }
}

fun main(args: Array<String>) {
    Application.launch(ToDoApplication::class.java)
}
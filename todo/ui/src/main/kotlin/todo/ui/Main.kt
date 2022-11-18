package todo.ui

import javafx.application.Application
import javafx.scene.Scene
import javafx.stage.Stage
import todo.app.view.View

class ToDoApplication: Application() {
    override fun start(stage: Stage) {
        stage.title = "TrackIt"
        stage.scene = Scene(View(), 600.0, 600.0)
        stage.apply{minWidth= 500.0; minHeight=450.0}
        stage.show()
    }
}

fun main(args: Array<String>) {
    Application.launch(ToDoApplication::class.java)
}
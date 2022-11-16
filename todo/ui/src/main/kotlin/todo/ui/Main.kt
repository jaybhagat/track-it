package todo.console

import io.ktor.http.*
import java.math.BigInteger
import java.security.MessageDigest
import kotlinx.coroutines.*
import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.layout.Pane
import javafx.stage.Stage
import todo.app.view.View

class ToDoApplication: Application() {
    override fun start(stage: Stage) {
        stage.title = "TrackIt"
        stage.scene = Scene(View(), 600.0, 600.0)
        stage.show()
    }
}

fun main(args: Array<String>) {
    Application.launch(ToDoApplication::class.java)
}
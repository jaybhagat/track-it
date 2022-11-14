package todo.app.view

import javafx.beans.InvalidationListener
import javafx.beans.Observable
import javafx.scene.control.ScrollPane
import javafx.scene.layout.BorderPane
import javafx.scene.layout.VBox
import todo.app.model.Model

class View: BorderPane(), InvalidationListener {
    init {
        Model.addListener(this)
        invalidated(null)

        left = ScrollPane(VBox()).apply {
            vbarPolicy = ScrollPane.ScrollBarPolicy.ALWAYS
        }

    }

    /**
     * Function that is called whenever Model broadcasts a change
     */
    override fun invalidated(observable: Observable?) {

    }
}
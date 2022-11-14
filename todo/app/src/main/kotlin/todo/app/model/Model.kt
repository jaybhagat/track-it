package todo.app.model

import javafx.beans.InvalidationListener
import javafx.beans.Observable


object Model: Observable {

    private val listeners = mutableListOf<InvalidationListener?>()

    override fun addListener(listener: InvalidationListener?) {
        listeners.add(listener)
    }
    override fun removeListener(listener: InvalidationListener?) {
        listeners.remove(listener)
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
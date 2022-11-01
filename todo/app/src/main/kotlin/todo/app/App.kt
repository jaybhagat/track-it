package todo.app

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.http.ResponseEntity
import org.springframework.beans.BeanUtils
import org.springframework.beans.BeansException
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import javafx.application.Application
import javafx.stage.Stage
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import org.springframework.web.bind.annotation.*

@Serializable
data class User(
    val username: String = "",
    val password: String = ""
) {
    init {
        println("Creating user...")
    }
}

@Serializable
data class Note(
    val id: Int = -1,
    val text: String = "",
    val priority: Int = -1,
    val gid: Int = -1,
    val last_edit: String = "",
    val due: String = ""
) {
    init {
        println("Creating task")
    }
}


@Serializable
data class BaseResponse(
    @SerialName("status")
    var status: Int = 1,
    @SerialName("message")
    var message: String = "",
    @SerialName("error")
    var error: String = ""
)

var conn: Connection? = null

@SpringBootApplication
class ToDoApplication: Application() {
    override fun start(stage: Stage) {
    }
}

@RestController
class TaskController() {
    val errorMapping =
        mapOf("[SQLITE_CONSTRAINT_PRIMARYKEY] A PRIMARY KEY constraint failed (UNIQUE constraint failed: users.username)" to "User already exists. Please choose a different username")

    // counter for getting unique note IDs -> replicate for group IDs
    var noteIdCounter: Int = -1
    init {
        val con = conn
        if (con != null) {
            val sql = "select max(note_id) from notes"
            val query = con.createStatement()
            val results = query.executeQuery(sql)
            results.next()
            noteIdCounter = results.getInt("max(note_id)") + 1
        }
    }


    @GetMapping("/test")
    fun connect_test(): String? {
        try {
            val url = "jdbc:sqlite:src/main/kotlin/assets/database/test.db"
            conn = DriverManager.getConnection(url)
            println("Test DB connection to SQLite has been established.")
            return "Test DB connection established"
        } catch (e: SQLException) {
            println(e.message)
            return e.message
        }
    }

    @GetMapping("/api")
    fun query(): String? {
        val con = conn;
        val map: HashMap<String, String> = HashMap()
        try {
            if (con != null) {
                val sql = "select * from users"
                val query = con.createStatement()
                val results = query.executeQuery(sql)
                while (results.next()) {
                    println(results.getString(1));
                }
            } else {
                println("Database connection not set up")
            }
        } catch (ex: SQLException) {
            println(errorMapping.getOrDefault(ex.message, ex.message));
        }
        return "done"
    }


    @PostMapping(value = ["/api/add/user"])
    fun createUser(@RequestBody getUserDetails: User): String {

        val res = BaseResponse()

        val con = conn;
        try {
            if (con != null) {
                val sql =
                    "insert into users(username, password) values ('${getUserDetails.username}', '${getUserDetails.password}')"
                val query = con.createStatement()
                query.executeUpdate(sql)
                res.status = 1
                res.message = "User added"
                return Json.encodeToString(listOf(res))
            }
        } catch (ex: SQLException) {
            val error = errorMapping.getOrDefault(ex.message, ex.message).orEmpty()
            println(error)
            res.status = 0
            res.error = error
            return Json.encodeToString(listOf(res))
        }
        return Json.encodeToString(listOf(res))
        // Terminal command for testing post request:
        //	curl -H "Content-Type: application/json" -d '{ "id": 6, "name": "Joe Bloggs", "username" : "cartman", "password" : "southpark" }' http://localhost:8080/api/add/user
    }


    @GetMapping("/api/authenticate")
    fun authenticateUser(
        @RequestParam(name = "username", required = true) username: String,
        @RequestParam(name = "password", required = true) pass: String
    ): String {
        println(username)
        val con = conn
        val res = BaseResponse()

        try {
            if (con != null) {
                val sql =
                    "select count(*) from users where username='${username}' and password='${pass}'"
                val query = con.createStatement()
                query.executeUpdate(sql)
                val results = query.executeQuery(sql)
                var numUsers = "0"
                if(results != null){
                    res.status = 1
                    numUsers = results.getString(1)
                }
                if(numUsers == "0"){
                    res.status = 0
                    res.error = "User not authenticated"
                }else {
                    res.status = 1
                    res.message = "User authenticated"
                }
            }
        } catch (ex: SQLException) {
            val error = errorMapping.getOrDefault(ex.message, ex.message).orEmpty()
            println(error)
            res.status = 0
            res.error = error
        }
        return Json.encodeToString(listOf(res))
    }



    @PostMapping("/api/add/task")
    fun addTask(@RequestBody getNoteDetails: Note): String {
        val res = BaseResponse()

        val con = conn
        try {
            if (con != null) {
                val sql =
                    "insert into notes(note_id, note_text, priority, group_id, last_edited, due_date) values " +
                            "('${noteIdCounter++}', " +
                            "'${getNoteDetails.text}', " +
                            "'${getNoteDetails.priority}', " +
                            "'${getNoteDetails.gid}', " +
                            "'${getNoteDetails.last_edit}', " +
                            "'${getNoteDetails.due}')"
                val query = con.createStatement()
                query.executeUpdate(sql)
                res.status = 1
                res.message = "Note added ${noteIdCounter}"
                return Json.encodeToString(listOf(res))
            }
        } catch (ex: SQLException) {
            val error = "Error in note creation"/* errorMapping.getOrDefault(ex.message, ex.message).orEmpty() */
            println(error)
            res.status = 0
            res.error = error
            return Json.encodeToString(listOf(res))
        }
        return Json.encodeToString(listOf(res))
    }


//    @PutMapping("/api/edit/task")
//    fun editTask(@RequestBody getNoteDetails: Note): String {
//        val res = BaseResponse()
//
//        val con = conn
//        try {
//            if (con != null) {
//                val sql =
//                    "UPDATE notes SET " +
//                            "note_text = '${getNoteDetails.text}', " +
//                            "priority = ${getNoteDetails.priority}, " +
//                            "group_id = ${getNoteDetails.gid}, " +
//                            "last_edited = '${getNoteDetails.last_edit}', " +
//                            "due_date = '${getNoteDetails.due}' " +
//                        "WHERE note_id = ${getNoteDetails.id}"
//                val query = con.createStatement()
//                query.executeUpdate(sql)
//                res.status = 1
//                res.message = "Note edited ${getNoteDetails.id}"
//                return Json.encodeToString(listOf(res))
//            }
//        } catch (ex: SQLException) {
//            val error = "Error in note edit"/* errorMapping.getOrDefault(ex.message, ex.message).orEmpty() */
//            println(error)
//            res.status = 0
//            res.error = error
//            return Json.encodeToString(listOf(res))
//        }
//        return Json.encodeToString(listOf(res))
//    }


}


fun main(args: Array<String>) {

    //Connecting to database
    try {
        val url = "jdbc:sqlite:src/main/kotlin/assets/database/todo.db"
        conn = DriverManager.getConnection(url)
        println("Connection to SQLite has been established.")
    } catch (e: SQLException) {
        println(e.message)
    }

    runApplication<ToDoApplication>(*args)
}

package com.example.ToDo

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.ResponseEntity
import org.springframework.beans.BeanUtils
import org.springframework.beans.BeansException
import org.springframework.web.bind.annotation.RequestBody
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
import org.springframework.web.bind.annotation.RequestMapping
import kotlinx.serialization.*
import kotlinx.serialization.json.*



public class User() {
	var username = ""
	var password = ""
	init {
		println("Creating user...")
	}
}

@Serializable
data class BaseResponse(
	var status: Int = 1,
	var message: String = "",
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

	@GetMapping("/test")
	fun connect_test(): String? {
		try {
			val url = "jdbc:sqlite:src/main/assets/database/test.db"
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
				val sql = "select count(*) from users"
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
		val newUser = User()
		val res = BaseResponse()
		try {
			BeanUtils.copyProperties(getUserDetails, newUser)
		} catch (e: BeansException) {
			val error = errorMapping.getOrDefault(e.message, e.message).orEmpty()
			println(error)
			res.status = 0
			res.error = error
			return Json.encodeToString(listOf(res))
		}
		val con = conn;
		try {
			if (con != null) {
				val sql =
					"insert into users(username, password) values ('${newUser.username}', '${newUser.password}')"
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
}


fun main(args: Array<String>) {

	//Connecting to database
	try {
		val url = "jdbc:sqlite:src/main/assets/database/todo.db"
		conn = DriverManager.getConnection(url)
		println("Connection to SQLite has been established.")
	} catch (e: SQLException) {
		println(e.message)
	}

	runApplication<ToDoApplication>(*args)
}



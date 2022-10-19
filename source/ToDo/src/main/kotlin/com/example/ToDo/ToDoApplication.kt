package com.example.ToDo

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
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

public class User() {
	var id = -1
	var name = ""
	var username = ""
	var password = ""
	init {
		println("Creating user...")
	}
}

@SpringBootApplication
class ToDoApplication: Application() {
	override fun start(stage: Stage) {
	}
}

@RestController
class TaskController() {
	var conn: Connection? = null
	@GetMapping("/")
	fun connect(): String? {
		try {
			val url = "jdbc:sqlite:src/main/assets/database/todo.db"
			conn = DriverManager.getConnection(url)
			println("Connection to SQLite has been established.")
			return "Connection established"
		} catch (e: SQLException) {
			println(e.message)
			return e.message
		}
	}
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
	fun query(): String?{
		val con = conn;
		val map: HashMap<String, String> = HashMap()
		try {
			if (con != null) {
				val sql = "select count(*) from users"
				val query = con.createStatement()
				val results = query.executeQuery(sql)
				while(results.next()){
					println(results.getString(1));
				}
			}else{
				println("Database connection not set up")
			}
		} catch (ex: SQLException) {
			println(ex.message);
		}
		return "done"
	}

	@GetMapping("/api/add")
	fun queryInsert(): String?{
		val con = conn;
		val map: HashMap<String, String> = HashMap()
		try {
			if (con != null) {
				val sql = "insert into users(id, name, username, password) values ('1', 'tapish', 'tapi', 'pass')"
				val query = con.createStatement()
				val results = query.executeQuery(sql)
				while(results.next()){
					println(results.getString(1));
				}
			}
		} catch (ex: SQLException) {
			println(ex.message);
		}
		return "done"
	}

	@GetMapping("/api/add/user")
	fun userInsert(): String?{
		val con = conn;
		val map: HashMap<String, String> = HashMap()
		try {
			if (con != null) {

			}
		} catch (ex: SQLException) {
			println(ex.message);
		}
		return "done"
	}

	@PostMapping(value = ["/api/add/user"])
	fun createUser(@RequestBody getUserDetails: User): Int {
		val newUser = User()
		try {
			BeanUtils.copyProperties(getUserDetails, newUser)
		} catch (e: BeansException) {
			println(e.message)
			return 0
		}
		val con = conn;
		try {
			if (con != null) {
				val sql =
					"insert into users(id, name, username, password) values (${newUser.id}, '${newUser.name}', '${newUser.username}', '${newUser.password}')"
				val query = con.createStatement()
				// Use executeUpdate for Insert, Delete, Update
				// Use executeQuery for SELECT
				query.executeUpdate(sql)
			}
		} catch (ex: SQLException) {
			println(ex.message)
			return 0
		}
		return 1
		// Terminal command for testing post request:
		//	curl -H "Content-Type: application/json" -d '{ "id": 6, "name": "Joe Bloggs", "username" : "cartman", "password" : "southpark" }' http://localhost:8080/api/add/user
	}
}
fun main(args: Array<String>) {
	runApplication<ToDoApplication>(*args)
}



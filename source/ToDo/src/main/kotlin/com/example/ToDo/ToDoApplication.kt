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
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

public class User() {
	public var id = null
	public var name = null
	public var username = null
	public var password = null
	init {
		println("Creating user for: $name")
	}
}

@SpringBootApplication
class ToDoApplication

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
				println("Fetched data:");
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
				println("Fetched data:");
				while(results.next()){
					println(results.getString(1));
				}
			}
		} catch (ex: SQLException) {
			println(ex.message);
		}
		return "done"
	}

	@PostMapping("/api/add/user")
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
					"insert into users(id, name, username, password) values (${newUser.id}, ${newUser.name}, ${newUser.username}, ${newUser.password})"
				val query = con.createStatement()
				val results = query.executeQuery(sql)
				println("Fetched data:")
				while (results.next()) {
					println(results.getString(1))
				}
			}
		} catch (ex: SQLException) {
			println(ex.message)
			return 0
		}
		return 1
	}
}

fun String.utf8(): String = URLEncoder.encode(this, "UTF-8")
fun formData(data: Map<String, String>): HttpRequest.BodyPublisher? {
	// Build string out of data
	val result = data.map { (key, value) -> "${(key.utf8())}" }.joinToString { "&" }
	return HttpRequest.BodyPublishers.ofString(result)
}
fun main(args: Array<String>) {
	val userValues = mapOf("id" to "1", "name" to "Sarvesh", "username" to "cartman", "password" to "southpark")
	val client = HttpClient.newBuilder().build()

	val request = HttpRequest.newBuilder()
		.uri(URI.create("https://localhost:8080/api/add/user"))
		.POST(formData(userValues))
	runApplication<ToDoApplication>(*args)
}



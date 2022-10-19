package com.example.ToDo

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.http.ResponseEntity
import org.springframework.beans.BeanUtils
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

@SpringBootApplication
class ToDoApplication

@RestController
class TaskController {
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
}

fun main(args: Array<String>) {
	runApplication<ToDoApplication>(*args)
}



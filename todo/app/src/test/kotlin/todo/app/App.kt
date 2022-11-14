package todo.app

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.springframework.boot.test.context.SpringBootTest
import java.sql.Connection
import java.sql.Statement
import java.sql.DriverManager
import java.sql.SQLException
import kotlinx.serialization.json.*
import kotlinx.serialization.*

@SpringBootTest
class ToDoApplicationTests {

	var conn: Connection? = null
	val ToDoInst = TaskController()

	init {
		try {
			val url = "jdbc:sqlite:src/main/kotlin/assets/database/test.db"
			conn = DriverManager.getConnection(url)
			println("Connection to SQLite has been established.")
		} catch (e: SQLException) {
			println(e.message)
		}
	}


	@Test
	fun userCreateTest() {
		var user = User("test", "test1")
		val result = ToDoInst.user_create(user, conn)
		var res = BaseResponse()
		res.status = 0
		res.error = "User already exists. Please choose a different username"
		Assertions.assertEquals(Json.encodeToString(listOf(res)), result)
	}

	@Test
	fun userAuthTest(){
		val result = ToDoInst.isValidUser("test", "test1", conn)
		println(result)
		var res = BaseResponse()
		res.status = 1
		res.message = "User authenticated"
		Assertions.assertEquals(Json.encodeToString(listOf(res)), result)
	}

	@Test
	fun validDateTest(){
		val result = ToDoInst.isValidDate("10/10/2025")
		Assertions.assertEquals(result, true)
	}

	@Test
	fun notvalidDateTest(){
		val result = ToDoInst.isValidDate("10/10/2020")
		Assertions.assertEquals(result, false)
	}

}

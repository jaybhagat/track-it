@PostMapping("/api/add/user")


public class User (private val id: Int, private val name: String, private val email: String, private val password: String) {
    init {
        println("Creating user: $email")
    }   
}
/*
    public class User() {
        public var id = null
        public var name = null
        public var username = null
        public var password = null

        init {
            println("Creating user for: $name")
        }
    }

    @PostMapping("/api/add/user")
    public fun createUser(@RequestBody User getUserDetails): Int {
        User newUser = User ()
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
*/

package todo.console

import java.math.BigInteger
import java.security.MessageDigest
import kotlinx.coroutines.*

private fun md5(input:String): String {
    val md = MessageDigest.getInstance("MD5")
    return BigInteger(1, md.digest(input.toByteArray())).toString(16).padStart(32, '0')
}

fun main(args: Array<String>) = runBlocking<Unit> {
    println("*********************************")
    println("*****    WELCOME TO TODO   ******")
    println("*********************************\n")

    println("I am your personal TODO List.\n")

    var haveAccount = false

    while (true) {
        print("Do you have an account with us? (y/n): ")
        val userInputAccount = readLine()!!.lowercase()
        if (userInputAccount == "y") {
            haveAccount = true
            break;
        } else if (userInputAccount == "n") {
            break;
        }
        print("\n")
    }

    if (haveAccount) {
        while(true) {
            print("Please enter your username: ")
            val username = readLine()!!
            print("Please enter your password: ")
            val password = readLine()!!
            val hashedPassword = md5(password)

            val response = (async { HttpRequest.logInUser(username, hashedPassword) }).await()
            if (response.status == 1) {
                break
            } else {
                println("There was an error accessing your account: " + response.error)
                println("Please try again.")
            }
        }
    } else {
        while(true) {
            print("Let's set up an account. Please enter a preferred username: ")
            val username = readLine()!!
            print("Please enter a password: ")
            val password = readLine()!!
            val hashedPassword = md5(password)

            val response = (async { HttpRequest.signUpUser(username, hashedPassword) }).await()
            if (response.status == 1) {
                break
            } else {
                println("There was an error creating your account: " + response.error)
                println("Please try again.")
            }
        }
    }

    println("Awesome, you're in!")
    println("Please type h to learn prompts you can use.")
    while(true) {
        val command = readLine()!!

        if (command == "q") {
            break;
        } else if (command == "h") {
            println("Here are the commands you can use:")
            println("h                     - get a list of available commands")
            println("add [item]            - add item with description [item] to your list")
            println("delete [num]          - delete the to-do list item corresponding to [num]")
            println("edit [num] [new-item] - edit the to-do list item [num] to be [new-item]")
            println("q                     - quit the console application")
        } else {
            println("Sorry! Command not recognized, please try again.")
        }
    }
}

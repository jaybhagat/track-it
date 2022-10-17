package com.example.ToDo

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.annotation.Id
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@SpringBootApplication
class ToDoApplication

fun main(args: Array<String>) {
	runApplication<ToDoApplication>(*args)
}


@RestController
@RequestMapping("/tasks")
class TaskResource(val service: TaskService) {
	@GetMapping
	fun index(): List<Task> = service.findTasks()

	@PostMapping
	fun post(@RequestBody task: Task) {
		service.post(task)
	}
}

data class Task(@Id val id: String?, val text: String)

@Service
class TaskService {
	var tasks: MutableList<Task> = mutableListOf()
	fun findTasks() = tasks
	fun post(task: Task) {
		tasks.add(task)
	}
}


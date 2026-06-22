package com.prac.taskmanagement.controller;

import com.prac.taskmanagement.dto.PagedResponse;
import com.prac.taskmanagement.dto.TaskResponse;
import com.prac.taskmanagement.service.TaskService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final TaskService taskService;

    public AdminController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping("/tasks")
    public ResponseEntity<PagedResponse<TaskResponse>> getAllTasksAdmin(
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {

        return ResponseEntity.ok(taskService.getAllTasksAdmin(pageable));
    }
}

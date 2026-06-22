package com.prac.taskmanagement.service;

import com.prac.taskmanagement.dto.PagedResponse;
import com.prac.taskmanagement.dto.TaskRequest;
import com.prac.taskmanagement.dto.TaskResponse;
import com.prac.taskmanagement.exception.ResourceNotFoundException;
import com.prac.taskmanagement.model.User;
import com.prac.taskmanagement.repository.TaskRepository;
import com.prac.taskmanagement.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

import com.prac.taskmanagement.model.Task;

@Service
public class TaskService {

   private final TaskRepository taskRepository;
   private final UserRepository userRepository;

    public TaskService(TaskRepository taskRepository, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    private TaskResponse mapToTaskResponse(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getCompleted()
        );
    }

    public PagedResponse<TaskResponse> getAllTasks(Pageable pageable) {
        User currentUser = getCurrentUser();
        Page<Task> page = taskRepository.findByUserId(currentUser.getId(), pageable);

        List<TaskResponse> content = page.getContent().stream()
                .map(this::mapToTaskResponse).toList();

        return new PagedResponse<>(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }

    public TaskResponse getTaskById(Long id) {
        return mapToTaskResponse(getOwnedTaskOrThrow(id));
    }

    public TaskResponse createTask(TaskRequest request) {
        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setCompleted(request.getCompleted());
        task.setUser(getCurrentUser());

        Task taskSaved = taskRepository.save(task);

        return mapToTaskResponse(taskSaved);
    }

    public TaskResponse updateTask(Long id, TaskRequest taskRequest) {
        Task existingTask = getOwnedTaskOrThrow(id);

        existingTask.setTitle(taskRequest.getTitle());
        existingTask.setDescription(taskRequest.getDescription());
        existingTask.setCompleted(taskRequest.getCompleted());

        Task updetedTask = taskRepository.save(existingTask);

        return mapToTaskResponse(updetedTask);
    }

    public void deleteTask(Long id) {
        Task task = getOwnedTaskOrThrow(id);
        taskRepository.deleteById(task.getId());
    }

    private User getCurrentUser() {
        String username= SecurityContextHolder.getContext().getAuthentication().getName();

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found: " + username));
    }

    private Task getOwnedTaskOrThrow(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));
        User currentUser = getCurrentUser();

        if(!task.getUser().getId().equals(currentUser.getId())) {
            throw new ResourceNotFoundException("Task not found with id: " + id);
        }

        return task;
    }

}

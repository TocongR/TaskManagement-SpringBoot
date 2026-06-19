package com.prac.taskmanagement.service;

import com.prac.taskmanagement.dto.TaskRequest;
import com.prac.taskmanagement.dto.TaskResponse;
import com.prac.taskmanagement.exception.ResourceNotFoundException;
import com.prac.taskmanagement.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.util.List;

import com.prac.taskmanagement.model.Task;

@Service
public class TaskService {

   private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    private TaskResponse mapToTaskResponse(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getCompleted()
        );
    }

    public List<TaskResponse> getAllTasks() {
        return taskRepository.findAll().stream()
                .map(this::mapToTaskResponse).toList();
    }

    public TaskResponse getTaskById(Long id) {
        return taskRepository.findById(id)
                .map(this::mapToTaskResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));
    }

    public TaskResponse createTask(TaskRequest request) {
        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setCompleted(request.getCompleted());

        Task taskSaved = taskRepository.save(task);

        return mapToTaskResponse(taskSaved);
    }

    public TaskResponse updateTask(Long id, TaskRequest taskRequest) {
        Task existingTask = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));

        existingTask.setTitle(taskRequest.getTitle());
        existingTask.setDescription(taskRequest.getDescription());
        existingTask.setCompleted(taskRequest.getCompleted());

        Task updetedTask = taskRepository.save(existingTask);

        return mapToTaskResponse(updetedTask);
    }

    public void deleteTask(Long id) {
        if(!taskRepository.existsById(id)) {
            throw new ResourceNotFoundException("Task not found with id: " + id);
        }

        taskRepository.deleteById(id);
    }

}

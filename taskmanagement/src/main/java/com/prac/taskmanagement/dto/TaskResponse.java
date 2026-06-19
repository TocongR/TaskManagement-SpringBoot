package com.prac.taskmanagement.dto;

public class TaskResponse {
    private Long id;
    private String title;
    private String description;
    private boolean completed;

    public TaskResponse(
            Long id,
            String title,
            String description,
            boolean completed
    ) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.completed = completed;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public boolean getCompleted() {
        return completed;
    }
}

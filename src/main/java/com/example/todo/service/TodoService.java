package com.example.todo.service;

import com.example.todo.api.dto.CompletionRequest;
import com.example.todo.api.dto.TodoRequest;
import com.example.todo.api.dto.TodoResponse;
import com.example.todo.domain.Todo;
import com.example.todo.repository.TodoRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class TodoService {
    private final TodoRepository todoRepository;

    public TodoService(TodoRepository todoRepository) {
        this.todoRepository = todoRepository;
    }

    @Transactional
    public TodoResponse create(TodoRequest request) {
        Todo todo = todoRepository.save(new Todo(request.title().trim()));
        return TodoResponse.from(todo);
    }

    public List<TodoResponse> findAll() {
        return todoRepository.findAll().stream().map(TodoResponse::from).toList();
    }

    public TodoResponse findById(Long id) {
        return TodoResponse.from(getTodo(id));
    }

    @Transactional
    public TodoResponse update(Long id, TodoRequest request) {
        Todo todo = getTodo(id);
        todo.changeTitle(request.title().trim());
        return TodoResponse.from(todo);
    }

    @Transactional
    public TodoResponse changeCompletion(Long id, CompletionRequest request) {
        Todo todo = getTodo(id);
        todo.changeCompletion(request.completed());
        return TodoResponse.from(todo);
    }

    @Transactional
    public void delete(Long id) {
        todoRepository.delete(getTodo(id));
    }

    private Todo getTodo(Long id) {
        return todoRepository.findById(id)
                .orElseThrow(() -> new TodoNotFoundException(id));
    }
}

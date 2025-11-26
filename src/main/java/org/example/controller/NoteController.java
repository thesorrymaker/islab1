package org.example.controller;

import org.example.dto.CreateNoteRequest;
import org.example.dto.NoteRequest;
import org.example.entity.Note;
import org.example.service.NoteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class NoteController {
    private final NoteService noteService;

    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    @GetMapping("/data")
    public ResponseEntity<List<NoteRequest>> getPosts(@RequestParam String username) {
        return ResponseEntity.ok(noteService.getNotes(username));
    }

    @PostMapping("/notes")
    public ResponseEntity<?> createPost(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody CreateNoteRequest request
    ) {
        String token = authHeader.replace("Bearer ", "");
        Note note = noteService.createNotes(token, request.getContent());
        return ResponseEntity.ok("Post created with id: " + note.getId());
    }
}

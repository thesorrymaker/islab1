package org.example.repository;

import org.example.entity.Note;
import org.example.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NoteRepository extends JpaRepository<Note, Long> {
    List<Note> findByProfile(Profile profile);
}

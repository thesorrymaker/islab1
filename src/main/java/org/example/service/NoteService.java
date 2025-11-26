package org.example.service;

import lombok.AllArgsConstructor;
import org.example.dto.NoteRequest;
import org.example.entity.Note;
import org.example.entity.Profile;
import org.example.repository.NoteRepository;
import org.example.repository.ProfileRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class NoteService {
    private final ProfileRepository profileRepository;
    private final NoteRepository noteRepository;
    private final JwtService jwtService;


    public List<NoteRequest> getNotes(String username) {
        Profile profile = profileRepository.findByUserUsername(username)
                .orElseThrow(() -> new RuntimeException("Profile not found"));
        return noteRepository.findByProfile(profile)
                .stream()
                .map(p -> new NoteRequest(p.getId(), p.getContent(), p.getProfile().getNickname()))
                .toList();
    }

    public Note createNotes(String token, String content) {
        String username = jwtService.extractUsername(token);
        Profile profile = profileRepository.findByUserUsername(username)
                .orElseThrow(() -> new RuntimeException("Profile not found"));
        Note note = Note.builder().content(content).profile(profile).build();
        return noteRepository.save(note);
    }
}

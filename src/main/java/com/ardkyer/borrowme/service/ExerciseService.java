package com.ardkyer.borrowme.service;

import com.ardkyer.borrowme.entity.Exercise;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public interface ExerciseService {
    List<Exercise> getAllExercises();
    Set<String> getHashtagsByExerciseName(String name);
}
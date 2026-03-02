package com.ardkyer.rion.service;

import com.ardkyer.rion.entity.Exercise;
import com.ardkyer.rion.repository.ExerciseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;


@Service
public class ExerciseServiceImpl implements ExerciseService{

    @Autowired
    private ExerciseRepository exerciseRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Exercise> getAllExercises() {
        return exerciseRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Set<String> getHashtagsByExerciseName(String name) {
        return exerciseRepository.findHashtagsByExerciseName(name);
    }

}
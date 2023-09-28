package com.example.telegrambot.service;

import com.example.telegrambot.dto.VacancyDto;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class VacancyService {

    @Autowired
    private VacanciesReaderService vacanciesReaderService;
    private final Map<String, VacancyDto> vacancies = new HashMap<>();

    @PostConstruct
    public void init(){
        List<VacancyDto> list = vacanciesReaderService.getVacanciesFromFile("vacancies.csv");
        for (VacancyDto vacancyDto : list) {
            vacancies.put(vacancyDto.getId(), vacancyDto);
        }
    }

    public List<VacancyDto> getMiddleVacancies(){
        return vacancies.values().stream()
                .filter(vacancyDto -> vacancyDto.getTitle().toLowerCase().contains("middle"))
                .toList();
    }

    public List<VacancyDto> getJuniorVacancies(){
        return vacancies.values().stream()
                .filter(vacancyDto -> vacancyDto.getTitle().toLowerCase().contains("junior"))
                .toList();
    }

    public List<VacancyDto> getSeniorVacancies(){
        return vacancies.values().stream()
                .filter(vacancyDto -> !vacancyDto.getTitle().toLowerCase().contains("junior"))
                .filter(vacancyDto -> !vacancyDto.getTitle().toLowerCase().contains("middle"))
                .toList();
    }

    public VacancyDto get(String id){
        return vacancies.get(id);
    }
}

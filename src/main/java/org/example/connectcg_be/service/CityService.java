package org.example.connectcg_be.service;

import org.example.connectcg_be.dto.CityDTO;

import java.util.List;

public interface CityService {
    List<CityDTO> getAllCities();
    CityDTO getCityById(Integer id);
}

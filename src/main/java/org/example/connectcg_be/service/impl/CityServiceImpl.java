package org.example.connectcg_be.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.connectcg_be.dto.CityDTO;
import org.example.connectcg_be.entity.City;
import org.example.connectcg_be.repository.CityRepository;
import org.example.connectcg_be.service.CityService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CityServiceImpl implements CityService {

    private final CityRepository cityRepository;

    @Override
    public List<CityDTO> getAllCities() {
        return cityRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public CityDTO getCityById(Integer id) {
        City city = cityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("City not found with id: " + id));
        return convertToDTO(city);
    }

    private CityDTO convertToDTO(City city) {
        return CityDTO.builder()
                .id(city.getId())
                .code(city.getCode())
                .name(city.getName())
                .region(city.getRegion())
                .build();
    }
}

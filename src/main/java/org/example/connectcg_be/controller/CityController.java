package org.example.connectcg_be.controller;

import lombok.RequiredArgsConstructor;
import org.example.connectcg_be.dto.CityDTO;
import org.example.connectcg_be.service.CityService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/v1/cities")
@RequiredArgsConstructor
public class CityController {

    private final CityService cityService;

    @GetMapping
    public ResponseEntity<List<CityDTO>> getAllCities() {
        List<CityDTO> cities = cityService.getAllCities();
        return ResponseEntity.ok(cities);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CityDTO> getCityById(@PathVariable Integer id) {
        CityDTO city = cityService.getCityById(id);
        return ResponseEntity.ok(city);
    }
}

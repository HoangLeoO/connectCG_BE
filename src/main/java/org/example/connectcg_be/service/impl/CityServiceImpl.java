package org.example.connectcg_be.service.impl;

import org.example.connectcg_be.repository.CityRepository;
import org.example.connectcg_be.service.CityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CityServiceImpl implements CityService {

    @Autowired
    private CityRepository cityRepository;
}

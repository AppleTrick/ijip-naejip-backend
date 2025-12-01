package com.ssafy.home.service;

import com.ssafy.home.dto.AddressResponse;
import com.ssafy.home.dto.AreaScope;

import java.util.List;

public interface AreaService {
    List<AddressResponse> searchAreaAddress(Double minLat, Double maxLat, Double minLng, Double maxLng, AreaScope scope);
}


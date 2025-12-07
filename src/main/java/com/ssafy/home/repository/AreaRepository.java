package com.ssafy.home.repository;

import com.ssafy.home.dto.AddressResponse;

import java.util.List;

public interface AreaRepository {
    List<AddressResponse> findAptDongByBoundingBox(Double minLat, Double maxLat, Double minLng, Double maxLng);
    List<AddressResponse> findAptByBoundingBox(Double minLat, Double maxLat, Double minLng, Double maxLng);
    List<AddressResponse> findSidoByBoundingBox(Double minLat, Double maxLat, Double minLng, Double maxLng);
    List<AddressResponse> findGugunByBoundingBox(Double minLat, Double maxLat, Double minLng, Double maxLng);
    List<AddressResponse> findDongByBoundingBox(Double minLat, Double maxLat, Double minLng, Double maxLng);
}


package com.ssafy.home.repository.impl;

import com.ssafy.home.dto.AddressResponse;
import com.ssafy.home.mapper.AreaMapper;
import com.ssafy.home.repository.AreaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class AreaRepositoryImpl implements AreaRepository {

    private final AreaMapper areaMapper;

    @Override
    public List<AddressResponse> findSidoByBoundingBox(Double minLat, Double maxLat, Double minLng, Double maxLng) {
        return areaMapper.findSidoByBoundingBox(minLat, maxLat, minLng, maxLng);
    }

    @Override
    public List<AddressResponse> findGugunByBoundingBox(Double minLat, Double maxLat, Double minLng, Double maxLng) {
        return areaMapper.findGugunByBoundingBox(minLat, maxLat, minLng, maxLng);
    }

    @Override
    public List<AddressResponse> findDongByBoundingBox(Double minLat, Double maxLat, Double minLng, Double maxLng) {
        return areaMapper.findDongByBoundingBox(minLat, maxLat, minLng, maxLng);
    }

    @Override
    public List<AddressResponse> findAptByBoundingBox(Double minLat, Double maxLat, Double minLng, Double maxLng) {
        return areaMapper.findAptByBoundingBox(minLat, maxLat, minLng, maxLng);
    }
}


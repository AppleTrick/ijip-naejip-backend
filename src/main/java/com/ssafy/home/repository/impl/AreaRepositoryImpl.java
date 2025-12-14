package com.ssafy.home.repository.impl;

import com.ssafy.home.dto.AddressResponse;
import com.ssafy.home.dto.GeoBoundParam;
import com.ssafy.home.dto.PriceRangeParam;
import com.ssafy.home.dto.PyungRangeParam;
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
    public List<AddressResponse> findSidoByBoundingBox(GeoBoundParam geoBoundParam, PriceRangeParam priceRangeParam) {
        return areaMapper.findSidoByBoundingBox(geoBoundParam, priceRangeParam);
    }

    @Override
    public List<AddressResponse> findGugunByBoundingBox(GeoBoundParam geoBoundParam, PriceRangeParam priceRangeParam) {
        return areaMapper.findGugunByBoundingBox(geoBoundParam, priceRangeParam);
    }

    @Override
    public List<AddressResponse> findDongByBoundingBox(GeoBoundParam geoBoundParam, PriceRangeParam priceRangeParam) {
        return areaMapper.findDongByBoundingBox(geoBoundParam, priceRangeParam);
    }

    @Override
    public List<AddressResponse> findAptByBoundingBox(GeoBoundParam geoBoundParam, PriceRangeParam priceRangeParam, PyungRangeParam pyungRangeParam) {
        return areaMapper.findAptByBoundingBox(geoBoundParam, priceRangeParam, pyungRangeParam);
    }

    @Override
    public List<AddressResponse> findAptDongByBoundingBox(GeoBoundParam geoBoundParam, PriceRangeParam priceRangeParam, PyungRangeParam pyungRangeParam) {
        return areaMapper.findAptDongByBoundingBox(geoBoundParam, priceRangeParam, pyungRangeParam);
    }
}


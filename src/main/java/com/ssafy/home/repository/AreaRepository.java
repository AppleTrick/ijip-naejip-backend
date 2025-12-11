package com.ssafy.home.repository;

import com.ssafy.home.dto.AddressResponse;
import com.ssafy.home.dto.GeoBoundParam;
import com.ssafy.home.dto.PriceRangeParam;
import com.ssafy.home.dto.PyungRangeParam;

import java.util.List;

public interface AreaRepository {
    List<AddressResponse> findAptDongByBoundingBox(GeoBoundParam geoBoundParam, PriceRangeParam priceRangeParam, PyungRangeParam pyungRangeParam);
    List<AddressResponse> findAptByBoundingBox(GeoBoundParam geoBoundParam, PriceRangeParam priceRangeParam, PyungRangeParam pyungRangeParam);
    List<AddressResponse> findSidoByBoundingBox(GeoBoundParam geoBoundParam, PriceRangeParam priceRangeParam);
    List<AddressResponse> findGugunByBoundingBox(GeoBoundParam geoBoundParam, PriceRangeParam priceRangeParam);
    List<AddressResponse> findDongByBoundingBox(GeoBoundParam geoBoundParam, PriceRangeParam priceRangeParam);
}


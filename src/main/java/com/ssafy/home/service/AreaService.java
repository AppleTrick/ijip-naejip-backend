package com.ssafy.home.service;

import com.ssafy.home.dto.*;

import java.util.List;

public interface AreaService {
    List<AddressResponse> searchAreaAddress(GeoBoundParam geoBoundParam, AreaScope scope,
                                            PriceRangeParam priceRangeParam, PyungRangeParam pyungRangeParam);
}


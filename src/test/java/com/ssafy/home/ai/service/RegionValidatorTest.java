package com.ssafy.home.ai.service;

import com.ssafy.home.ai.dto.RegionInfo;
import com.ssafy.home.dto.DongCodeResponse;
import com.ssafy.home.mapper.DongCodeMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class RegionValidatorTest {

    private static final DongCodeResponse MAPO = new DongCodeResponse("1144000000", "서울특별시", "마포구", null, null, null, null);
    private static final DongCodeResponse HAPJEONG = new DongCodeResponse("1144012000", "서울특별시", "마포구", "합정동", null, null, null);

    private DongCodeMapper dongCodeMapper;
    private RegionValidator regionValidator;

    @BeforeEach
    void setUp() {
        dongCodeMapper = mock(DongCodeMapper.class);
        when(dongCodeMapper.searchByRegionToken(anyString())).thenReturn(List.of());
        when(dongCodeMapper.searchByRegionToken("마포구")).thenReturn(List.of(MAPO));
        when(dongCodeMapper.searchByRegionToken("합정동")).thenReturn(List.of(HAPJEONG));
        regionValidator = new RegionValidator(dongCodeMapper);
    }

    @Test
    void 행정구역_접미사가_붙은_지역명을_추출해_DB에서_검증한다() {
        List<RegionInfo> regions = regionValidator.extractAndValidateRegions("마포구 합정동 아파트 시세 알려줘");

        assertThat(regions).extracting(RegionInfo::getDongCode).containsExactly("1144000000", "1144012000");
        assertThat(regions.get(1).getFullAddress()).isEqualTo("서울특별시 마포구 합정동");
    }

    @Test
    void 조사가_붙어도_지역명만_추출한다() {
        regionValidator.extractAndValidateRegions("마포구에서 거래량이 많은 아파트");

        verify(dongCodeMapper).searchByRegionToken("마포구");
    }

    @Test
    void DB에_없는_토큰은_버린다() {
        List<RegionInfo> regions = regionValidator.extractAndValidateRegions("가나다구 아파트");

        verify(dongCodeMapper).searchByRegionToken("가나다구");
        assertThat(regions).isEmpty();
    }

    @Test
    void 같은_지역은_한_번만_넣는다() {
        List<RegionInfo> regions = regionValidator.extractAndValidateRegions("마포구랑 마포구 비교");

        assertThat(regions).hasSize(1);
    }

    @Test
    void 접미사_없는_약칭은_추출하지_않는다() {
        // 현재 한계: "마포"처럼 구·동 접미사가 없는 지역명은 전처리 대상이 아니다 (LLM이 SQL에서 직접 처리)
        List<RegionInfo> regions = regionValidator.extractAndValidateRegions("마포 아파트");

        assertThat(regions).isEmpty();
        verify(dongCodeMapper, never()).searchByRegionToken("마포");
    }

    @Test
    void 지역_컨텍스트에_검증된_코드와_이름을_넣는다() {
        List<RegionInfo> regions = regionValidator.extractAndValidateRegions("합정동");

        String context = regionValidator.buildRegionContext(regions);

        assertThat(context).contains("dong_code=1144012000", "gugun_name=마포구", "dong_name=합정동");
        assertThat(regionValidator.buildRegionContext(List.of())).isEmpty();
    }
}

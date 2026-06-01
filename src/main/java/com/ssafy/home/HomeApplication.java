package com.ssafy.home;

import com.ssafy.home.dto.AreaScope;
import com.ssafy.home.dto.GeoBoundParam;
import com.ssafy.home.service.AreaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@Slf4j
public class HomeApplication {

	public static void main(String[] args) {
		SpringApplication.run(HomeApplication.class, args);
	}

	// 서버 시작 시 JVM JIT + InnoDB 버퍼 풀 워밍업 (잠실 일대 기준)
	@Bean
	ApplicationRunner warmUp(AreaService areaService) {
		return args -> {
			GeoBoundParam bounds = new GeoBoundParam(37.505, 37.525, 127.095, 127.115);
			for (AreaScope scope : new AreaScope[]{AreaScope.APT_DONG, AreaScope.APT}) {
				try {
					areaService.searchAreaAddress(bounds, scope, null, null);
					log.info("[WarmUp] {} query completed", scope);
				} catch (Exception e) {
					log.warn("[WarmUp] {} failed: {}", scope, e.getMessage());
				}
			}
		};
	}

}

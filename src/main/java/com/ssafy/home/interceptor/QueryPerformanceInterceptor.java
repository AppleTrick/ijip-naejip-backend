package com.ssafy.home.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.session.ResultHandler;
import org.springframework.stereotype.Component;

import java.sql.Statement;

@Slf4j
@Component
@Intercepts({
    @Signature(type = StatementHandler.class, method = "query",  args = {Statement.class, ResultHandler.class}),
    @Signature(type = StatementHandler.class, method = "update", args = {Statement.class})
})
public class QueryPerformanceInterceptor implements Interceptor {

    private static final long WARN_THRESHOLD_MS  = 2_000;
    private static final long INFO_THRESHOLD_MS  =   500;

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        long start  = System.currentTimeMillis();
        Object result = invocation.proceed();
        long elapsed = System.currentTimeMillis() - start;

        try {
            StatementHandler handler = (StatementHandler) invocation.getTarget();
            MetaObject meta = SystemMetaObject.forObject(handler);

            // Mapper 메서드명 추출 (예: ApartmentMapper.findMonthlyPriceTrend)
            MappedStatement ms = (MappedStatement) meta.getValue("delegate.mappedStatement");
            String fullId = ms.getId();
            String methodId = fullId.substring(fullId.lastIndexOf('.') + 1);
            String mapperName = fullId.substring(fullId.lastIndexOf('.', fullId.lastIndexOf('.') - 1) + 1, fullId.lastIndexOf('.'));

            String label = mapperName + "." + methodId;

            if (elapsed >= WARN_THRESHOLD_MS) {
                log.warn("[SLOW QUERY] {}ms | {}", elapsed, label);
            } else if (elapsed >= INFO_THRESHOLD_MS) {
                log.info("[QUERY] {}ms | {}", elapsed, label);
            } else {
                log.debug("[QUERY] {}ms | {}", elapsed, label);
            }
        } catch (Exception e) {
            log.debug("[QUERY] {}ms | (mapper 정보 파싱 실패)", elapsed);
        }

        return result;
    }
}

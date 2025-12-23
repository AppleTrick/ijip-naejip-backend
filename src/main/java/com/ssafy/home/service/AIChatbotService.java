package com.ssafy.home.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.home.ai.dto.SemanticSearchResponse;
import com.ssafy.home.ai.dto.SqlResultWrapper;
import com.ssafy.home.dto.AddressResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIChatbotService {

    private final ChatClient.Builder chatClientBuilder;
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    // Allowed tables for query generation
    private static final List<String> ALLOWED_TABLES = List.of(
            "dongcodes", "houseinfos", "housedeals", "apt_dong_stats", "apt_pyung_stats", "apt_dong_pyung_stats");

    private static final String SYSTEM_PROMPT_TEMPLATE = """
            You are a MySQL expert specializing in real estate data analysis.
            Your goal is to answer user questions by generating and executing SQL queries against the database.

            ### Database Schema

            Table `dongcodes`:
            - dong_code (PK): code for the region
            - sido_name, gugun_name, dong_name: region names
            - Important Query Logic:
                - There are NO physical columns for sido_code or gugun_code.
                - To filter by Sido, you must extract the first 2 digits of dong_code in your query (e.g., SUBSTR(dong_code, 1, 2)).
                - To filter by Gugun, you must extract the first 5 digits of dong_code in your query (e.g., SUBSTR(dong_code, 1, 5)).
            Table `houseinfos`:
            - apt_seq (PK): unique apartment ID
            - dong_code: FK to dongcodes
            - apt_nm: Apartment Name
            - build_year: built year
            - road_nm, jibun: address
            - latitude, longitude: coordinates

            Table `housedeals`:
            - no (PK): deal ID
            - apt_seq: FK to houseinfos
            - deal_date: YYYYMMDD format int
            - deal_amount: price in 10,000 KRW (man-won)
            - pyung: size in pyung (approx 3.3 sq m)
            - floor: floor number

            Table `apt_pyung_stats` (aggregated stats per apartment per size):
            - apt_seq, pyung (PK)
            - avg_price: average price
            - deal_count: number of deals

            ### Rules
            1. Response Format: You must strictly follow the response format.
               - Steps:
                 1. Reason about the user's request.
                 2. Generate a MySQL SELECT query.
                 3. Provide a brief natural language description of what the query does (e.g., "Recently traded apartments in Seongsu-dong").
               - Output JSON format:
               {
                 "reasoning": "Brief explanation of logic",
                 "sql": "SELECT ...",
                 "query_description": "Natural language summary of the query"
               }
            2. Security:
               - ONLY generate SELECT statements.
               - DO NOT use tables other than: %s
            3. Data Logic:
               - Transaction amounts (`deal_amount`) are in 10,000 KRW.
               - 'Recently' usually means the last 365 days or the latest available data.
               - If checking for "expensive" or "cheap", use `avg_price` or `deal_amount`.
            4. If no specific region is mentioned, do not guess. Ask the user for clarification ideally, but since you are a one-shot agent, try to query for 'Seoul' or general stats if applicable, or return a SQL that searches by popular regions like 'Gangnam-gu'.
            """;

    private static final String INTERPRETATION_PROMPT = """
            You are a Real Estate Analyst.
            Using the provided User Question and SQL Query Results, provide a helpful, natural language answer.

            User Question: %s

            SQL Query Used: %s
            (Description: %s)

            SQL Results:
            %s

            ### Instructions
            1. **Analyze the results**:
            - **Main Answer (답변)**: Provide a natural language summary of the findings. Combine the analysis and insights here. Use soft language ("~같습니다", "~로 보입니다") to infer patterns or trends.

            2. **Response Format**:
            - You MUST return a VALID JSON object.
            - Place the formatted text into the "analysis" field.
            - The "analysis" field must follow this exact structure:
                답변: [Summary and Insights go here]
            - Do NOT output any text outside the JSON block.

            3. **JSON Structure**:
            {
                "analysis": "답변: ...",
                "recommended_apartments": [
                    { "aptSeq": "...", "aptName": "...", "aptDong": "...", "avgPrice": 12345, "primaryPyung": 32 }
                ]
            }
            - "recommended_apartments" should map columns from the result if available. Limit to 5 items.
            """;

    public SemanticSearchResponse generateResponse(String userMessage) {
        ChatClient chatClient = chatClientBuilder.build();

        // 1. Generate SQL
        String schemaTables = String.join(", ", ALLOWED_TABLES);
        String systemPrompt = String.format(SYSTEM_PROMPT_TEMPLATE, schemaTables);

        String response = chatClient.prompt()
                .system(systemPrompt)
                .user(userMessage)
                .call()
                .content();

        // 2. Parse SQL from JSON response
        Map<String, String> parsedResponse = extractSqlFromJson(response);
        String sql = parsedResponse.get("sql");
        String queryDescription = parsedResponse.getOrDefault("query_description", "Generated SQL Query");

        if (sql == null) {
            return SemanticSearchResponse.builder()
                    .analysis("죄송합니다. 질문을 이해하여 올바른 쿼리를 생성하지 못했습니다. 다시 질문해 주시겠어요?")
                    .results(Collections.emptyList())
                    .build();
        }

        // 3. Validate & Execute SQL with Retry
        SqlResultWrapper queryResult = executeQueryWithRetry(chatClient, systemPrompt, sql);

        // 4. Interpret Results
        return interpretResults(chatClient, userMessage, queryResult, queryDescription);
    }

    private Map<String, String> extractSqlFromJson(String jsonResponse) {
        try {
            // Improved JSON extraction
            int startIndex = jsonResponse.indexOf("{");
            int endIndex = jsonResponse.lastIndexOf("}");

            if (startIndex == -1 || endIndex == -1 || startIndex >= endIndex) {
                // Fallback to regex if no JSON object is found
                throw new IllegalArgumentException("No JSON object found");
            }

            String cleanJson = jsonResponse.substring(startIndex, endIndex + 1);
            Map<String, Object> map = objectMapper.readValue(cleanJson, Map.class);

            Map<String, String> result = new HashMap<>();
            result.put("sql", (String) map.get("sql"));
            result.put("query_description", (String) map.get("query_description"));
            return result;
        } catch (Exception e) {
            log.error("Failed to parse JSON from LLM: {}", jsonResponse, e);
            // Fallback: try regex if JSON parsing fails
            Pattern pattern = Pattern.compile("SELECT.*", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
            Matcher matcher = pattern.matcher(jsonResponse);
            Map<String, String> result = new HashMap<>();
            if (matcher.find()) {
                result.put("sql", matcher.group());
            }
            return result;
        }
    }

    private SqlResultWrapper executeQueryWithRetry(ChatClient chatClient, String systemPrompt, String initialSql) {
        String currentSql = initialSql;
        int maxRetries = 2;

        for (int i = 0; i <= maxRetries; i++) {
            // Security Check
            if (!isSafeQuery(currentSql)) {
                return SqlResultWrapper.builder()
                        .isSuccess(false)
                        .errorMessage("허용되지 않는 쿼리 또는 테이블 접근입니다.")
                        .generatedSql(currentSql)
                        .build();
            }

            try {
                // Execute
                log.info("Executing SQL (Attempt {}): {}", i + 1, currentSql);
                List<Map<String, Object>> result = jdbcTemplate.queryForList(currentSql);
                return SqlResultWrapper.builder()
                        .resultTable(result)
                        .rowCount(result.size())
                        .generatedSql(currentSql)
                        .isSuccess(true)
                        .build();

            } catch (Exception e) {
                log.warn("SQL Execution Failed (Attempt {}): {}", i + 1, currentSql, e);

                if (i == maxRetries) {
                    return SqlResultWrapper.builder()
                            .resultTable(Collections.emptyList())
                            .isSuccess(false)
                            .errorMessage("쿼리 실행에 반복해서 실패했습니다: " + e.getMessage())
                            .generatedSql(currentSql)
                            .build();
                }

                // Request Correction
                String retryPrompt = String.format("""
                        The SQL query you generated failed with the following error:
                        %s

                        Original Query: %s

                        Please correct the SQL query. Return ONLY the JSON format as before:
                        {
                          "reasoning": "fix explanation",
                          "sql": "CORRECTED SELECT ...",
                          "query_description": "..."
                        }
                        """, e.getMessage(), currentSql);

                String retryResponse = chatClient.prompt()
                        .system(systemPrompt)
                        .user(retryPrompt)
                        .call()
                        .content();

                Map<String, String> parsed = extractSqlFromJson(retryResponse);
                if (parsed.get("sql") != null) {
                    currentSql = parsed.get("sql");
                }
            }
        }
        return null; // Should not reach here
    }

    private boolean isSafeQuery(String sql) {
        String upperSql = sql.trim().toUpperCase();
        if (!upperSql.startsWith("SELECT"))
            return false;

        for (String forbidden : List.of("USERS", "USER_", "NOTIFICATION")) {
            if (upperSql.contains(forbidden))
                return false;
        }
        return true;
    }

    private SemanticSearchResponse interpretResults(ChatClient chatClient, String userQuestion,
            SqlResultWrapper resultWrapper, String queryDescription) {
        if (!resultWrapper.isSuccess()) {
            return SemanticSearchResponse.builder()
                    .analysis("데이터 조회 중 오류가 발생했습니다: " + resultWrapper.getErrorMessage())
                    .results(Collections.emptyList())
                    .build();
        }

        String analysisResponse = "";
        try {
            String resultJson = objectMapper.writeValueAsString(resultWrapper.getResultTable());
            // Truncate if too long
            if (resultJson.length() > 5000) {
                resultJson = resultJson.substring(0, 5000) + "... (truncated)";
            }

            String prompt = String.format(INTERPRETATION_PROMPT, userQuestion, resultWrapper.getGeneratedSql(),
                    queryDescription, resultJson);

            analysisResponse = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

            // Improved JSON extraction: find the first '{' and last '}'
            int startIndex = analysisResponse.indexOf("{");
            int endIndex = analysisResponse.lastIndexOf("}");

            if (startIndex == -1 || endIndex == -1 || startIndex >= endIndex) {
                // If no JSON found, treat the whole response as analysis
                return SemanticSearchResponse.builder()
                        .analysis(analysisResponse)
                        .results(Collections.emptyList())
                        .build();
            }

            String jsonString = analysisResponse.substring(startIndex, endIndex + 1);
            Map<String, Object> responseMap = objectMapper.readValue(jsonString, Map.class);

            String analysisText = (String) responseMap.get("analysis");
            List<Map<String, Object>> aptList = (List<Map<String, Object>>) responseMap.get("recommended_apartments");

            List<AddressResponse> addressResponses = Collections.emptyList();
            if (aptList != null) {
                addressResponses = aptList.stream().map(map -> AddressResponse.builder()
                        .aptSeq(String.valueOf(map.getOrDefault("aptSeq", map.getOrDefault("apt_seq", ""))))
                        .aptName((String) map.getOrDefault("aptName", map.getOrDefault("apt_nm", "")))
                        .aptDong((String) map.getOrDefault("aptDong", map.getOrDefault("apt_dong", "")))
                        .avgPrice(parseIntSafely(map.getOrDefault("avgPrice", map.getOrDefault("deal_amount", 0))))
                        .primaryPyung(parseIntSafely(map.getOrDefault("primaryPyung", map.getOrDefault("pyung", 0))))
                        .build())
                        .collect(Collectors.toList());
            }

            return SemanticSearchResponse.builder()
                    .analysis(analysisText)
                    .results(addressResponses)
                    .build();

        } catch (Exception e) {
            log.error("Failed to interpret results", e);
            // Fallback: return raw response if JSON parsing fails
            String fallbackAnalysis = analysisResponse.replaceAll("```json", "").replaceAll("```", "").trim();
            if (fallbackAnalysis.isEmpty()) {
                fallbackAnalysis = "결과를 분석하는 도중 오류가 발생했습니다.";
            }
            return SemanticSearchResponse.builder()
                    .analysis(fallbackAnalysis)
                    .results(Collections.emptyList())
                    .build();
        }
    }

    private Integer parseIntSafely(Object obj) {
        if (obj instanceof Integer)
            return (Integer) obj;
        if (obj instanceof Number)
            return ((Number) obj).intValue();
        try {
            return Integer.parseInt(String.valueOf(obj));
        } catch (Exception e) {
            return 0;
        }
    }
}

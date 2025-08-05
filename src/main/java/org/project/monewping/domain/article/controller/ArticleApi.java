package org.project.monewping.domain.article.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.project.monewping.domain.article.dto.data.ArticleDto;
import org.project.monewping.domain.article.dto.data.ArticleViewDto;
import org.project.monewping.domain.article.dto.response.ArticleRestoreResultDto;
import org.project.monewping.global.dto.CursorPageResponse;
import org.project.monewping.global.dto.ErrorResponse;
import org.springframework.http.ResponseEntity;

@Tag(name = "뉴스 기사 뷰 관리", description = "뉴스 기사 뷰 등록 API")
public interface ArticleApi {

    // 1. 기사 뷰 등록
    @Operation(summary = "기사 뷰 등록", description = "기사 뷰를 등록합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "기사 뷰 등록 성공",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ArticleViewDto.class),
                examples = @ExampleObject(value = """
                {
                  "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                  "viewedBy": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                  "createdAt": "2025-08-04T11:41:07.755Z",
                  "articleId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                  "source": "Chosun",
                  "sourceUrl": "string",
                  "articleTitle": "string",
                  "articlePublishedDate": "2025-08-04T11:41:07.755Z",
                  "articleSummary": "string",
                  "articleCommentCount": 9007199254740991,
                  "articleViewCount": 9007199254740991
                }
                """))),
        @ApiResponse(responseCode = "404", description = "기사 정보 없음",
            content = @Content(mediaType = "*/*",
                schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류",
            content = @Content(mediaType = "*/*",
                schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<ArticleViewDto> registerArticleView(
        @Parameter(description = "기사 ID", required = true, in = ParameterIn.PATH) UUID articleId,
        @Parameter(description = "요청자 ID", required = true, in = ParameterIn.HEADER, name = "Monew-Request-User-ID") UUID monewRequestUserId
    );

    // 2. 뉴스 기사 목록 조회
    @Operation(summary = "뉴스 기사 목록 조회", description = "조건에 맞는 뉴스 기사 목록을 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = CursorPageResponse.class),
                examples = @ExampleObject(value = """
                {
                  "content": [
                    {
                      "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                      "source": "HanKyung",
                      "sourceUrl": "string",
                      "title": "string",
                      "publishDate": "2025-08-04T11:41:07.770Z",
                      "summary": "string",
                      "commentCount": 9007199254740991,
                      "viewCount": 9007199254740991,
                      "viewedByMe": true
                    }
                  ],
                  "nextCursor": "string",
                  "nextAfter": "2025-04-06T15:04:05.000Z",
                  "size": 10,
                  "totalElements": 100,
                  "hasNext": true
                }
                """))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (정렬 기준 오류, 페이지네이션 파라미터 오류 등)",
            content = @Content(mediaType = "*/*",
                schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류",
            content = @Content(mediaType = "*/*",
                schema = @Schema(implementation = ErrorResponse.class)))
    })
    CursorPageResponse<ArticleDto> getArticles(
        @Parameter(description = "검색어(제목, 요약)", required = false) String keyword,
        @Parameter(description = "관심사 ID", required = false) UUID interestId,
        @Parameter(description = "출처 목록 필터", required = false) List<String> sourceIn,
        @Parameter(description = "발행일 시작 (ISO 8601)", required = false) LocalDateTime publishDateFrom,
        @Parameter(description = "발행일 끝 (ISO 8601)", required = false) LocalDateTime publishDateTo,
        @NotBlank(message = "orderBy 는 필수입니다.") @Pattern(regexp = "publishDate|commentCount|viewCount", message = "정렬 조건은 publishDate, commentCount, viewCount 중 하나여야 합니다.") String orderBy,
        @NotBlank(message = "direction 은 필수입니다.") @Pattern(regexp = "ASC|DESC", flags = Pattern.Flag.CASE_INSENSITIVE, message = "정렬 방향은 ASC 또는 DESC 여야 합니다.") String direction,
        @Parameter(description = "커서", required = false) String cursor,
        @Parameter(description = "보조 커서(createdAt) (ISO 8601)", required = false) LocalDateTime after,
        @Min(value = 1, message = "limit 은 최소 1 이상이어야 합니다.")
        int limit,
        @Parameter(description = "요청자 ID", required = true, in = ParameterIn.HEADER, name = "Monew-Request-User-ID")
        UUID monewRequestUserId
    );

    // 3. 출처 목록 조회
    @Operation(summary = "출처 목록 조회", description = "출처 목록을 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공",
            content = @Content(mediaType = "application/json",
                array = @ArraySchema(schema = @Schema(implementation = String.class)),
                examples = @ExampleObject(value = """
                [
                  "Chosun",
                  "HanKyung",
                  "YonHap"
                ]
                """))),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류",
            content = @Content(mediaType = "*/*",
                schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<List<String>> getAllSources();

    // 4. 뉴스 복구
    @Operation(summary = "뉴스 복구", description = "유실된 뉴스 기사를 복구합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "복구 성공",
            content = @Content(mediaType = "application/json",
                array = @ArraySchema(schema = @Schema(implementation = ArticleRestoreResultDto.class)),
                examples = @ExampleObject(value = """
                [
                  {
                    "restoreDate": "2025-08-04T11:41:07.778Z",
                    "restoredArticleIds": [
                      "3fa85f64-5717-4562-b3fc-2c963f66afa6"
                    ],
                    "restoredArticleCount": 9007199254740991
                  }
                ]
                """))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (from, to 누락 또는 from > to)",
            content = @Content(mediaType = "*/*",
                schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류",
            content = @Content(mediaType = "*/*",
                schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<List<ArticleRestoreResultDto>> restoreArticles(
        @Parameter(description = "복구 시작 날짜/시간 (ISO 8601)", required = true) LocalDateTime from,
        @Parameter(description = "복구 종료 날짜/시간 (ISO 8601)", required = true) LocalDateTime to
    );

    // 5. 뉴스 기사 논리 삭제
    @Operation(summary = "뉴스 기사 논리 삭제", description = "뉴스 기사를 논리적으로 삭제합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "논리 삭제 성공"),
        @ApiResponse(responseCode = "404", description = "뉴스 기사 정보 없음",
            content = @Content(mediaType = "*/*",
                schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류",
            content = @Content(mediaType = "*/*",
                schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<Void> softDelete(
        @Parameter(description = "뉴스 기사 ID", required = true, in = ParameterIn.PATH) UUID articleId
    );

    // 6. 뉴스 기사 물리 삭제
    @Operation(summary = "뉴스 기사 물리 삭제", description = "뉴스 기사를 물리적으로 삭제합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "삭제 성공"),
        @ApiResponse(responseCode = "404", description = "뉴스 기사 정보 없음",
            content = @Content(mediaType = "*/*",
                schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류",
            content = @Content(mediaType = "*/*",
                schema = @Schema(implementation = ErrorResponse.class)))
    })
    ResponseEntity<Void> hardDelete(
        @Parameter(description = "뉴스 기사 ID", required = true, in = ParameterIn.PATH) UUID articleId
    );
}
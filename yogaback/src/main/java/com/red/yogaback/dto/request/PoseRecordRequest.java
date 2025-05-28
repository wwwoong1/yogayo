package com.red.yogaback.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 요가 기록 생성/수정 시 JSON으로 전달되는 요청 데이터
 * ranking은 선택사항입니다.
 * (recordImg는 MultipartFile로 별도 받으므로 JSON에는 포함하지 않음)
 */
@Getter
@Setter    // Setter 추가
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PoseRecordRequest {
    private Float accuracy;
    private Integer ranking;    // 선택사항
    private Float poseTime;

    // 새 필드: 멀티 모드인 경우 클라이언트가 roomId를 전달하면 해당 값을 사용하고,
    // 솔로 모드인 경우에는 null로 처리합니다.
    private Long roomId;
}

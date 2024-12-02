package com.gathering.gathering.controller;

import com.gathering.common.base.response.BaseResponse;
import com.gathering.gathering.model.dto.GatheringCreate;
import com.gathering.gathering.model.dto.GatheringResponse;
import com.gathering.gathering.model.entity.GatheringUserStatus;
import com.gathering.gathering.service.GatheringService;
import com.gathering.user.model.dto.response.UserResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/gathering")
@RequiredArgsConstructor
@Tag(name = "모임 API", description = "모임 관련 api")
@ApiResponse(responseCode = "200", description = "success", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BaseResponse.class)))
public class GatheringController {

    private final GatheringService gatheringService;

    /**
     * TODO -
     */
    @PostMapping
    @Operation(summary = "모임 생성", description = "모임 생성 API (와이어 프레임 확인 후 데이터 추가 필요)")
    public BaseResponse<Void> create(@RequestBody @Valid GatheringCreate gatheringCreate,
                                     @AuthenticationPrincipal UserDetails userDetails) {
        gatheringService.create(gatheringCreate, userDetails);
        return new BaseResponse<>();
    }

    /**
     * TODO - Challenge 정보 추가 여부 보류
     */
    @GetMapping("/{gathering_id}/details")
    @Operation(summary = "모임 상세 조회", description = "모임 상세 조회 API, Challenge 정보 추가 여부 보류 (와이어 프레임 확인 후 데이터 추가 필요)")
    public BaseResponse<GatheringResponse> getGatheringByGatheringId(@PathVariable Long gatheringId) {
        return new BaseResponse<>(gatheringService.getGatheringByGatheringId(gatheringId));
    }

    /**
     * TODO - 참여 클릭 시 동시성 제어 추후 필요
     */
    @PostMapping("/join/{gathering_id}")
    @Operation(summary = "모임 참여", description = "모임 참여 API (와이어 프레임 확인 후 데이터 추가 필요)")
    public BaseResponse<Void> join(@PathVariable Long gatheringId,
                                   @AuthenticationPrincipal UserDetails userDetails) {
        gatheringService.join(gatheringId, userDetails.getUsername());
        return new BaseResponse<>();
    }

    /**
     * TODO - 참여 클릭 시 동시성 제어 추후 필요
     */
    @PostMapping("/join/{gathering_id}")
    @Operation(summary = "모임 떠나기", description = "모임 떠나기 API (와이어 프레임 확인 후 데이터 추가 필요)")
    public BaseResponse<Void> leave(@PathVariable Long gatheringId,
                                    GatheringUserStatus gatheringUserStatus,
                                   @AuthenticationPrincipal UserDetails userDetails) {
        gatheringService.leave(gatheringId, userDetails.getUsername(), gatheringUserStatus);
        return new BaseResponse<>();
    }

    @DeleteMapping("/{gathering_id}")
    @Operation(summary = "모임 삭제", description = "모임 삭제 API (와이어 프레임 확인 후 데이터 추가 필요)")
    public BaseResponse<Void> delete(@PathVariable Long gatheringId,
                                     @AuthenticationPrincipal UserDetails userDetails) {
        gatheringService.delete(gatheringId, userDetails.getUsername());
        return new BaseResponse<>();
    }

    /**
     * TODO -
     */
    @GetMapping("/{gatheringId}/users")
    @Operation(summary = "모임 참여 상태에 따른 유저 조회", description = "모임 참여 상태에 따른 유저 조회 API (와이어 프레임 확인 후 데이터 추가 필요) 참여 상태 -> PARTICIPATING(참여 O), NOT_PARTICIPATING(참여 X)")
    public BaseResponse<List<UserResponseDto>> findGatheringWithUsersByIdAndStatus(@PathVariable Long gatheringId, @RequestParam GatheringUserStatus gatheringUserStatus) {
        return new BaseResponse<>(gatheringService.findGatheringWithUsersByIdAndStatus(gatheringId, gatheringUserStatus));
    }
}
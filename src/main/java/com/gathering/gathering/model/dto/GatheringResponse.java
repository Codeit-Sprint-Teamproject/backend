package com.gathering.gathering.model.dto;

import com.gathering.gathering.model.entity.Gathering;
import com.gathering.gathering.model.entity.GatheringStatus;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class GatheringResponse {

    private Long id;
    private String name;
    private String content;
    private long goalDays;
    private LocalDate endDate;
    private int minCapacity;
    private int maxCapacity;
    private GatheringStatus gatheringStatus;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
    private String bookTitle;
    private String bookImage;
    private String publisher;
    private String publishDate;

    public static GatheringResponse fromEntity(Gathering gathering) {
        return GatheringResponse.builder()
                .id(gathering.getId())
                .name(gathering.getName())
                .content(gathering.getContent())
                .goalDays(gathering.getGoalDays())
                .endDate(gathering.getEndDateTime())
                .minCapacity(gathering.getMinCapacity())
                .maxCapacity(gathering.getMaxCapacity())
                .gatheringStatus(gathering.getGatheringStatus())
                .createdTime(gathering.getCreatedTime())
                .updatedTime(gathering.getModifiedTime())
                .bookTitle(gathering.getBook().getTitle())
                .bookImage(gathering.getBook().getImage())
                .publisher(gathering.getBook().getPublisher())
                .publishDate(gathering.getBook().getPublisher())
                .build();
    }
}

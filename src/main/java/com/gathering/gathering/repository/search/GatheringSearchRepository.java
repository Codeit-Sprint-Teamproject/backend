package com.gathering.gathering.repository.search;

import com.gathering.gathering.model.dto.GatheringSearch;
import com.gathering.gathering.model.entity.Gathering;
import com.gathering.gathering.model.entity.GatheringStatus;
import com.gathering.gathering.model.entity.GatheringUserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface GatheringSearchRepository {
    Slice<Gathering> findGatherings(GatheringSearch gatheringSearch, Pageable pageable);

    Page<Gathering> findGatheringsForUserByUsername(String username, Pageable pageable, GatheringStatus gatheringStatus, GatheringUserStatus gatheringUserStatus);
}

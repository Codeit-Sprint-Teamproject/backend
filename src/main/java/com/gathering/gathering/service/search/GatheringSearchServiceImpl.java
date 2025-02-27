package com.gathering.gathering.service.search;

import com.gathering.challenge.model.entity.Challenge;
import com.gathering.challenge.model.entity.ChallengeUser;
import com.gathering.challenge.repository.ChallengeRepository;
import com.gathering.common.base.exception.BaseException;
import com.gathering.gathering.model.dto.GatheringResponse;
import com.gathering.gathering.model.dto.GatheringSearch;
import com.gathering.gathering.model.dto.GatheringSearchResponse;
import com.gathering.gathering.model.entity.*;
import com.gathering.gathering.repository.search.GatheringSearchJpaRepository;
import com.gathering.gathering.service.GatheringSearchAsync;
import com.gathering.gathering.util.GatheringSearchActions;
import com.gathering.review.model.dto.BookReviewDto;
import com.gathering.review.model.dto.ReviewListDto;
import com.gathering.review.repository.ReviewRepository;
import com.gathering.user.model.entitiy.User;
import com.gathering.user.repository.UserRepository;
import com.gathering.util.string.FullTextIndexParser;
import com.gathering.util.string.StringUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.gathering.common.base.response.BaseResponseStatus.INVALID_SEARCH_WORD;
import static com.gathering.common.base.response.BaseResponseStatus.NON_EXISTED_GATHERING;

@Service
@RequiredArgsConstructor
public class GatheringSearchServiceImpl implements GatheringSearchService {

    private final GatheringSearchJpaRepository gatheringSearchJpaRepository;
    private final GatheringSearchAsync gatheringSearchAsync;
    private final GatheringSearchActions gatheringSearchActions;
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final ChallengeRepository challengeRepository;

    @Override
    public GatheringSearchResponse findGatherings(GatheringSearch gatheringSearch, Pageable pageable, UserDetails userDetails) {
        Slice<Gathering> slice = gatheringSearchJpaRepository.findGatherings(gatheringSearch, pageable);

        Set<Long> wishGatheringIds
                = userDetails != null ? userRepository.findByUsername(userDetails.getUsername()).getWishGatheringIds() : new HashSet<>();

        return gatheringSearchActions.convertToGatheringSearchResponse(slice, wishGatheringIds);
    }

    @Override
    public GatheringSearchResponse findJoinableGatherings(GatheringSearch gatheringSearch, Pageable pageable, UserDetails userDetails) {
        Slice<Gathering> slice = gatheringSearchJpaRepository.findJoinableGatherings(gatheringSearch, pageable);

        Set<Long> wishGatheringIds
                = userDetails != null ? userRepository.findByUsername(userDetails.getUsername()).getWishGatheringIds() : new HashSet<>();

        return gatheringSearchActions.convertToGatheringSearchJoinableResponse(slice, wishGatheringIds);
    }

    @Override
    public GatheringResponse getById(Long gatheringId, String userKey, UserDetails userDetails) {
        Gathering gathering = gatheringSearchJpaRepository.getGatheringWithChallengeAndBook(gatheringId)
                .orElseThrow(() -> new BaseException(NON_EXISTED_GATHERING));

        // 조회수 비동기 처리
        gatheringSearchAsync.incrementViewCount(gathering.getId(), userKey);

        Set<Long> wishGatheringIds
                = userDetails != null ? userRepository.findByUsername(userDetails.getUsername()).getWishGatheringIds() : new HashSet<>();

        return GatheringResponse.fromEntity(gathering, wishGatheringIds.contains(gathering.getId()));
    }

    @Override
    public GatheringSearchResponse findMyGatherings(String username, Pageable pageable, GatheringStatus gatheringStatus, GatheringUserStatus gatheringUserStatus) {
        Page<Gathering> result = gatheringSearchJpaRepository.findGatheringsForUserByUsername(username, pageable, gatheringStatus, gatheringUserStatus);

        Map<Long, Double> challengeReadingRateMap = getReadingRateMap(username, result);

        return gatheringSearchActions.convertToMyGatheringPage(result, challengeReadingRateMap);
    }

    @Override
    public GatheringSearchResponse findMyCreated(String username, Pageable pageable) {
        User user = userRepository.findByUsername(username);
        Page<Gathering> result = gatheringSearchJpaRepository.findMyCreated(user.getUserName(), pageable);

        Map<Long, Double> challengeReadingRateMap = getReadingRateMap(username, result);

        return gatheringSearchActions.convertToMyGatheringPage(result, challengeReadingRateMap);
    }

    @Override
    public GatheringSearchResponse findMyWishes(String username, Pageable pageable) {
        Set<Long> wishGatheringIds = userRepository.findWishGatheringIdsByUserName(username);

        if (wishGatheringIds.isEmpty()) {
            return GatheringSearchResponse.empty();
        }

        Page<Gathering> result = gatheringSearchJpaRepository.findMyWishes(wishGatheringIds, pageable);

        Map<Long, Double> challengeReadingRateMap = getReadingRateMap(username, result);

        return gatheringSearchActions.convertToMyGatheringPage(result, challengeReadingRateMap);
    }

    @Override
    public GatheringResponse introduce(Long gatheringId) {
        Gathering gathering = gatheringSearchJpaRepository.getGatheringWithChallengeAndBook(gatheringId)
                .orElseThrow(() -> new BaseException(NON_EXISTED_GATHERING));

        User user = userRepository.findByUsername(gathering.getOwner());

        return GatheringResponse.introduceFromEntity(gathering, user);
    }

    @Override
    public ReviewListDto review(Long gatheringId, GatheringReviewSortType sort, Pageable pageable) {
        return gatheringSearchJpaRepository.getGatheringReviewList(gatheringId, sort, pageable);
    }

    /**
     * 검색 시 첫 검색은 integrated로 호출
     */
    @Override
    public GatheringSearchResponse getIntegratedResultBySearchWordAndType(String searchWord, SearchType searchType, Pageable pageable, String username) {
        // 2글자 미만일 경우 X
        if (!StringUtil.isValidLength(searchWord, 2)) {
            throw new BaseException(INVALID_SEARCH_WORD);
        }
        
        // Full Text 인덱스에 맞는 검색 조건으로 변환
        String formatSearchWord = FullTextIndexParser.formatForFullTextQuery(searchWord);

        /**
         * 모임 리스트 및 카운트 조회
         */
        Page<Object[]> gatherings = gatheringSearchActions
                .findGatheringsBySearchWordAndType(formatSearchWord, searchType, pageable);

        /**
         * 리뷰 목록 및 카운트 조회
         */
        ReviewListDto tempReviews = reviewRepository.searchReviews(searchType, searchWord, pageable, username);
        Page<BookReviewDto> reviews = new PageImpl<>(tempReviews.getBookReviews(), pageable, tempReviews.getTotal());

        return gatheringSearchActions.convertToIntegratedResultPages(gatherings, reviews);
    }

    /**
     * integrated 이후 따로 검색
     */
    @Override
    public GatheringSearchResponse getGatheringsBySearchWordAndType(String searchWord, SearchType searchType, Pageable pageable) {
        // 2글자 미만일 경우 X
        if (!StringUtil.isValidLength(searchWord, 2)) {
            throw new BaseException(INVALID_SEARCH_WORD);
        }

        // Full Text 인덱스에 맞는 검색 조건으로 변환
        String formatSearchWord = FullTextIndexParser.formatForFullTextQuery(searchWord);

        Page<Object[]> gatherings = gatheringSearchActions
                .findGatheringsBySearchWordAndType(formatSearchWord, searchType, pageable);

        return gatheringSearchActions.convertToGatheringsResultPage(gatherings, gatherings.getTotalElements());
    }

    @Override
    public GatheringSearchResponse getReviewsBySearchWordAndType(String searchWord, SearchType searchType, Pageable pageable, String username) {
        /**
         * 리뷰 목록 및 카운트 조회
         */
        ReviewListDto tempReviews = reviewRepository.searchReviews(searchType, searchWord, pageable, username);
        Page<BookReviewDto> reviews = new PageImpl<>(tempReviews.getBookReviews(), pageable, tempReviews.getTotal());

        return gatheringSearchActions.convertToReviewsResultPage(reviews);
    }

    private Map<Long, Double> getReadingRateMap(String username, Page<Gathering> result) {
        // Gathering에서 페치조인한 Challenge 정보에서 Id 리스트를 가져온다.
        List<Long> challengeIds = result.getContent().stream().map(gathering -> gathering.getChallenge().getId()).collect(Collectors.toList());

        /**
         * 챌린지 ID 리스트로 ChallengeUser와 User를 한번 더 페치조인하여 조회한다.
         * 한번 더 조회하는 이유
         * - 이미 Gathering을 조회할 때 GatheringUsers 1대다 페치조인 했기 때문에 ChallengeUser는 분리시켰다.
          */
        List<Challenge> challenges = challengeRepository.getByIdsIn(challengeIds);

        /**
         * Challenge Id를 키값으로 한 Map을 생성한다.
         * Value는 해당 모임의 챌린지에 참여중인 유저의 독서 달성률
         * key - challengeId
         * value - 50.0 %
         */
        return challenges.stream()
                .collect(Collectors.toMap(
                        Challenge::getId, // Key: Challenge ID
                        challenge -> challenge.getChallengeUsers().stream()
                                .filter(challengeUser -> challengeUser.getUser().getUserName().equals(username)) // 사용자 ID 매칭
                                .findFirst() // ChallengeUser는 1명뿐이므로 첫 번째 요소를 가져옴
                                .map(ChallengeUser::getReadingRate)
                                .orElse(0.0) // 매칭되지 않는 경우 null로 처리
                ));
    }
}

package com.gathering.gathering.util;

import com.gathering.book.model.entity.Book;
import com.gathering.book.repository.BookRepository;
import com.gathering.challenge.model.entity.Challenge;
import com.gathering.challenge.model.entity.ChallengeUser;
import com.gathering.challenge.repository.ChallengeRepository;
import com.gathering.gathering.model.dto.GatheringCreate;
import com.gathering.gathering.model.entity.Gathering;
import com.gathering.gathering.model.entity.GatheringUser;
import com.gathering.gathering.model.entity.GatheringUserStatus;
import com.gathering.gathering.repository.GatheringRepository;
import com.gathering.gathering.validator.GatheringValidator;
import com.gathering.user.model.dto.response.UserResponseDto;
import com.gathering.user.model.entitiy.User;
import com.gathering.user.repository.UserRepository;
import com.gathering.util.date.DateCalculateHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GatheringActions {

    private final GatheringRepository gatheringRepository;
    private final UserRepository userRepository;
    private final ChallengeRepository challengeRepository;
    private final GatheringValidator gatheringValidator;
    private final BookRepository bookRepository;
    private final DateCalculateHolder dateCalculateHolder;

    public Gathering createGathering(GatheringCreate gatheringCreate, String username) {
        User user = userRepository.findByUsername(username);
        Book book = bookRepository.findBookByBookIdAndCategoryId(gatheringCreate.getBookId(), gatheringCreate.getCategoryId());
        book.incrementSelectedCount();

        return Gathering.createGathering(
                gatheringCreate,
                Challenge.createChallenge(gatheringCreate, ChallengeUser.createChallengeUser(user)),
                book,
                GatheringUser.createGatheringUser(user, GatheringUserStatus.PARTICIPATING),
                dateCalculateHolder,
                gatheringValidator);
    }


    public void joinGathering(Long gatheringId, String username) {
        User user = userRepository.findByUsername(username);
        Gathering gathering = gatheringRepository.getGatheringAndGatheringUsersById(gatheringId);

        Gathering.join(gathering, user, GatheringUser.createGatheringUser(user, GatheringUserStatus.PARTICIPATING), gatheringValidator);
        Challenge.join(gathering.getChallenge(), ChallengeUser.createChallengeUser(user));
    }

    public void leaveGathering(Long gatheringId, String username, GatheringUserStatus gatheringUserStatus) {
        Gathering gathering = gatheringRepository.findGatheringWithUsersByIdAndStatus(gatheringId, gatheringUserStatus);
        User user = userRepository.findByUsername(username);

        Gathering.leave(gathering, user);
        Challenge challenge = challengeRepository.getChallengeUsersById(gathering.getChallenge().getId());
        Challenge.leave(challenge, user);
    }

    public Gathering deleteGathering(Long gatheringId, String username) {
        User user = userRepository.findByUsername(username);
        Gathering gathering = gatheringRepository.getById(gatheringId);
        gatheringValidator.validateOwner(gathering.getOwner(), user.getUserName());
        return gathering;
    }

    public List<UserResponseDto> mapToUserResponseDtos(List<GatheringUser> gatheringUsers) {
        return gatheringUsers.stream()
                .map(gatheringUser -> UserResponseDto.fromEntity(gatheringUser.getUser()))
                .collect(Collectors.toList());
    }
}
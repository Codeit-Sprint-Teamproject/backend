package com.gathering.challenge.model.entity;

import com.gathering.common.base.exception.BaseException;
import com.gathering.common.base.jpa.BaseTimeEntity;
import com.gathering.gathering.model.dto.GatheringCreate;
import com.gathering.gathering.model.entity.Gathering;
import com.gathering.user.model.entitiy.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static com.gathering.common.base.response.BaseResponseStatus.USER_NOT_IN_CHALLENGE;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Challenge extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "challenge_id")
    private Long id;

    @OneToOne(mappedBy = "challenge", fetch = FetchType.LAZY)
    private Gathering gathering;

    @OneToMany(mappedBy = "challenge", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChallengeUser> challengeUsers = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private ChallengeStatus challengeStatus;

    private double completeRate;    // 챌린지 달성률
    private LocalDate startDateTime;
    private LocalDate endDateTime;  // 챌린지 종료일
    private long goalTime;

    public static Challenge createChallenge(GatheringCreate gatheringCreate, ChallengeUser challengeUsers) {
        Challenge challenge = new Challenge();
        challenge.challengeStatus = ChallengeStatus.INACTIVE;
        challenge.completeRate = 0.0;
        challenge.startDateTime = gatheringCreate.getGatheringDate();
        challenge.endDateTime = gatheringCreate.getEndDate();
        challenge.addChallengeUser(challengeUsers);
        return challenge;
    }

    public static void leave(Challenge challenge, User user) {
        List<ChallengeUser> challengeUsers = challenge.getChallengeUsers();
        ChallengeUser challengeUser = challengeUsers.stream()
                .filter(cu -> cu.getUser().getId() == user.getId())
                .findFirst()
                .orElseThrow(() -> new BaseException(USER_NOT_IN_CHALLENGE));
        challengeUsers.remove(challengeUser);
    }

    public static void join(Challenge challenge, ChallengeUser challengeUser) {
        challenge.addChallengeUser(challengeUser);
    }

    public void addChallengeUser(ChallengeUser challengeUser) {
        challengeUsers.add(challengeUser);
        challengeUser.addChallenge(this);
    }

    public void addGathering(Gathering gathering) {
        this.gathering = gathering;
    }
}

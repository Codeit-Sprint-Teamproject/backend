package com.gathering.util;

import com.gathering.common.base.exception.BaseException;
import com.gathering.util.holder.DateCalculateHolder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import static com.gathering.common.base.response.BaseResponseStatus.INVALID_GOAL_PERIOD;

/**
 * 기간 계산용 Util 클래스
 */
@Component
public class DateCalculateUtil implements DateCalculateHolder {

    @Override
    public long calculateGoalDays(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate) || startDate.isEqual(endDate)) {
            throw new BaseException(INVALID_GOAL_PERIOD);
        }

        // LocalDate 객체 간 차이를 ChronoUnit.DAYS.between()을 사용하여 계산
        return ChronoUnit.DAYS.between(startDate, endDate);
    }
}
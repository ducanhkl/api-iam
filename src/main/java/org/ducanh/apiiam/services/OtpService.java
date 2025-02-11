package org.ducanh.apiiam.services;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.ducanh.apiiam.entities.OTP;
import org.ducanh.apiiam.entities.User;
import org.ducanh.apiiam.entities.UserStatus;
import org.ducanh.apiiam.helpers.TimeHelpers;
import org.ducanh.apiiam.repositories.OtpRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.Objects;

@Service
@Slf4j
public class OtpService {

    private final int NUMBER_OF_OTP_DIGITS = 6;
    private final int EXPIRATION_IN_MINUTES = 5;

    private final OtpRepository otpRepository;
    private final TimeHelpers timeHelpers;

    @Autowired
    public OtpService(
            final OtpRepository otpRepository,
            final TimeHelpers timeHelpers) {
        this.otpRepository = otpRepository;
        this.timeHelpers = timeHelpers;
    }

    @Transactional
    public OTP generateOtpForVerify(User user) {
        if (UserStatus.ACTIVE != user.getStatus()) {
            throw new RuntimeException("User status not valid");
        }
        String code = generateOtpForVerify(NUMBER_OF_OTP_DIGITS);
        OffsetDateTime current = timeHelpers.currentTime();
        Integer numberOfOtpSentInLast24h =
                otpRepository.countOTPByUserIdAndCreatedAt(user.getUserId(), current.minusDays(1), current);
        if (numberOfOtpSentInLast24h > 5) {
            throw new RuntimeException("Too many send otp request");
        }
        disableAllOtherOtp(user);
        OffsetDateTime offsetDateTime = current.plusMinutes(EXPIRATION_IN_MINUTES);
        OTP otp = OTP.createOtpForVerify(user, code, offsetDateTime);
        return otpRepository.save(otp);
    }

    @Transactional
    public boolean completeVerifyOtp(User user, String code) {
        Long userId = user.getUserId();
        OTP otp = otpRepository.findLatestOTPByUserIdAndType(userId, OTP.Type.VERIFY);
        otp.incrementRetries();
        if (!verifyBeforeComplete(otp, code)) {
            return false;
        }
        otp.makeVerified();
        user.makeVerified();
        return true;
    }

    private boolean verifyBeforeComplete(OTP otp, String code) {
        Objects.requireNonNull(otp, "OTP cannot be null");
        boolean isNotVerified = !otp.isVerified();
        boolean isNotUsed = !otp.getUsed();
        boolean isCodeMatch = code.equals(otp.getCode());
        boolean isRetryTimeOk = otp.getRetries() < 5;
        log.info("Check parameters of otp with infos isNotVerified: {}, isNotUsed: {}, isCodeMatch: {}, isRetryTimeOk: {}",
                isNotVerified, isNotUsed, isCodeMatch, isRetryTimeOk);
        return isNotVerified && isNotUsed && isCodeMatch && isRetryTimeOk;
    }

    private void disableAllOtherOtp(User user) {
        int count = otpRepository.updateOtpUsedByUserId(user.getUserId(), true);
        log.debug("Disabled {} OTPs for user {}", count, user.getUsername());
    }

    private String generateOtpForVerify(int numberOfDigits) {
        if (numberOfDigits <= 0 || numberOfDigits > 6) {
            throw new IllegalArgumentException("Number of digits must be greater than zero");
        }
        SecureRandom random = new SecureRandom();
        int lowerBound = (int) Math.pow(10, numberOfDigits - 1);
        int upperBound = (int) Math.pow(10, numberOfDigits) - 1;
        int otp = lowerBound + random.nextInt(upperBound - lowerBound + 1);

        return String.valueOf(otp);


    }
}

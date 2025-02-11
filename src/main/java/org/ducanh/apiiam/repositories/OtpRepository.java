package org.ducanh.apiiam.repositories;

import org.ducanh.apiiam.entities.OTP;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;

@Repository
public interface OtpRepository extends JpaRepository<OTP, Long> {

    @Modifying
    @Query("""
            update OTP otp set otp.used=:used where otp.userId=:userId
        """)
    Integer updateOtpUsedByUserId(@Param("userId") Long userId,
                                  @Param("used") Boolean used);

    @Query("""
        select count(otp.otpId) from OTP otp where
                otp.userId=:userId and otp.createdAt between :begin and :end
        """)
    Integer countOTPByUserIdAndCreatedAt(@Param("userId") Long userId,
                                         @Param("begin") OffsetDateTime begin,
                                         @Param("end") OffsetDateTime end);

    @Query("""
        select otp from OTP otp
            where otp.userId = :userId
                and otp.type = :type
            order by otp.createdAt desc limit 1
    """)
    OTP findLatestOTPByUserIdAndType(Long userId, OTP.Type type);
}

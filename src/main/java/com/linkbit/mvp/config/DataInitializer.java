package com.linkbit.mvp.config;

import com.linkbit.mvp.domain.*;
import com.linkbit.mvp.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final LoanOfferRepository loanOfferRepository;
    private final LoanRepository loanRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Database already seeded. Skipping data initialization.");
            return;
        }

        log.info("Seeding sample data...");

        // 1. Create Admin User
        createUser("admin@linkbit.com", "SystemAdmin", "0000000000", KycStatus.VERIFIED, ActorType.ADMIN);

        // 2. Create Sample Lenders
        User lender1 = createUser("lender1@example.com", "CryptoCapital", "9876543210", KycStatus.VERIFIED, ActorType.CLIENT);
        User lender2 = createUser("lender2@example.com", "SatStacker", "8765432109", KycStatus.VERIFIED, ActorType.CLIENT);

        // 3. Create Sample Offers
        createOffer(lender1, new BigDecimal("50000"), new BigDecimal("12.5"), 60, 6);
        createOffer(lender1, new BigDecimal("100000"), new BigDecimal("11.0"), 50, 12);
        createOffer(lender2, new BigDecimal("25000"), new BigDecimal("14.0"), 70, 3);
        createOffer(lender2, new BigDecimal("500000"), new BigDecimal("9.5"), 40, 24);

        log.info("Sample data seeded successfully.");
    }

    private User createUser(String email, String pseudonym, String phone, KycStatus status, ActorType role) {
        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode("password123"))
                .phoneNumber(phone)
                .pseudonym(pseudonym)
                .kycStatus(status)
                .role(role)
                .build();

        UserKycDetails kycDetails = UserKycDetails.builder()
                .user(user)
                .fullLegalName(pseudonym + " Full Name")
                .bankAccountNumber("12345678" + (int)(Math.random() * 90 + 10))
                .ifsc("SBIN0001234")
                .upiId(pseudonym.toLowerCase() + "@upi")
                .build();

        user.setKycDetails(kycDetails);
        return userRepository.save(user);
    }

    private LoanOffer createOffer(User lender, BigDecimal amount, BigDecimal interest, int ltv, int tenure) {
        LoanOffer offer = LoanOffer.builder()
                .lender(lender)
                .loanAmountInr(amount)
                .interestRate(interest)
                .expectedLtvPercent(ltv)
                .tenureMonths(tenure)
                .status(LoanOfferStatus.OPEN)
                .build();
        return loanOfferRepository.save(offer);
    }
}

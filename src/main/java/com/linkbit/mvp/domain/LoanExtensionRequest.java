package com.linkbit.mvp.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "loan_extension_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanExtensionRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_id", nullable = false)
    private Loan loan;

    @Column(name = "new_tenure_days", nullable = false)
    private Integer newTenureDays;

    @Column(name = "new_interest_rate", precision = 5, scale = 2)
    private BigDecimal newInterestRate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExtensionStatus status;

    @Column(name = "reason")
    private String reason;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum ExtensionStatus {
        PENDING,
        APPROVED,
        REJECTED
    }
}

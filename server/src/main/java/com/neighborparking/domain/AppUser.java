package com.neighborparking.domain;

import com.neighborparking.common.domain.BaseEntity;
import com.neighborparking.domain.enums.RiskStatus;
import com.neighborparking.domain.enums.UserRole;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.Table;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "app_user")
public class AppUser extends BaseEntity {

    @Column(name = "phone_masked", nullable = false, length = 32)
    private String phoneMasked;

    @Column(nullable = false, length = 64)
    private String nickname;

    @Column(name = "avatar_url", length = 512)
    private String avatarUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_status", nullable = false, length = 24)
    private RiskStatus riskStatus = RiskStatus.NORMAL;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_role", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 32)
    private Set<UserRole> roles = new LinkedHashSet<UserRole>();
}

package com.lifeos.onboarding.repository;

import com.lifeos.onboarding.domain.OnboardingProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface OnboardingProgressRepository extends JpaRepository<OnboardingProgress, UUID> {
}

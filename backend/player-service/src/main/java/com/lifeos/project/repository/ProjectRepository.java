package com.lifeos.project.repository;

import com.lifeos.project.domain.Project;
import com.lifeos.project.domain.enums.ProjectStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID> {
    long countByPlayerPlayerIdAndStatus(UUID playerId, ProjectStatus status);
}

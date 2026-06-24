package com.projectsystem.repository;

import com.projectsystem.model.entity.ProjectMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с участниками проектов.
 *
 * @author Евдокимов Д.А.
 */
@Repository
public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {

    List<ProjectMember> findByProjectId(Long projectId);

    List<ProjectMember> findByUserId(Long userId);

    Optional<ProjectMember> findByProjectIdAndUserId(Long projectId, Long userId);
}
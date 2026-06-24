package com.projectsystem.repository;

import com.projectsystem.model.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * Репозиторий для работы с ролями.
 *
 * @author Евдокимов Д.А.
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(String name);
}
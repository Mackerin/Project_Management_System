package com.projectsystem.repository;

import com.projectsystem.model.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Репозиторий для работы с комментариями.
 *
 * @author Евдокимов Д.А.
 */
@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByTaskIdOrderByCreatedAtAsc(Long taskId);
}
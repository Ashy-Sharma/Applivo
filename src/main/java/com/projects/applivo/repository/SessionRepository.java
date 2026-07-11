package com.projects.applivo.repository;

import com.projects.applivo.entity.Session;
import com.projects.applivo.entity.SessionStatus;
import com.projects.applivo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {

    boolean existsByUserAndSessionStatusIn(User user, List<SessionStatus> active);

    Optional<Session> findByIdAndUser(Long id, User user);

    Session getSessionById(Long id);

    List<Session> findBySessionStatus(SessionStatus sessionStatus);
}
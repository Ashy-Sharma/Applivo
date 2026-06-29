package com.projects.applivo.repository;

import com.projects.applivo.entity.EmulatorInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmulatorInstanceRepository extends JpaRepository<EmulatorInstance, Long> {
}

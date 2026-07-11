package com.projects.applivo.repository;

import com.projects.applivo.entity.EmulatorInstance;
import com.projects.applivo.entity.EmulatorStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmulatorInstanceRepository extends JpaRepository<EmulatorInstance, Long> {

    Optional<EmulatorInstance> findByContainerId(String containerId);

    @Query("select coalesce(max(e.adbPort), :basePort) from EmulatorInstance e where e.status in ('STARTING', 'RUNNING')")
    int findMaxAdbPort(@Param("basePort") int basePort);


    List<EmulatorInstance> findByStatusIn(Collection<EmulatorStatus> statuses);
}

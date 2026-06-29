package com.projects.applivo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "emulator_instances")
public class EmulatorInstance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "container_id", nullable = false, unique = true, length = 100)
    private String containerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmulatorStatus status;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id")
    private Session session;

    @Column(name = "adb_port", nullable = false)
    private Integer adbPort;

    @Column(name = "vnc_port")
    private Integer vncPort;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "last_access_at")
    private Instant lastAccessAt;

}

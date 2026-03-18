package org.appvibessolution.server;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "project_data")
public class ProjectDataModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, unique = true, updatable = false, length = 36)
    private Long id;

    @Column(name = "repo_url", length = 1024, nullable = false)
    private String repoUrl;

    @Column(length = 64, nullable = false)
    private String framework;

    @Column(length = 64)
    private boolean DbInclude;

    @Column(length = 64)
    private String dbType;

    @Column(length = 64)
    private String dbUser;

    @Column(length = 64)
    private String dbPassword;

    @Column(name = "public_url", length = 512)
    private String publicUrl;

    // Add one-to-many relationship with environments
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProjectEnvironment> environments = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // Helper to add environment and set parent
    public void addEnvironment(ProjectEnvironment env) {
        env.setProject(this);
        environments.add(env);
    }
}
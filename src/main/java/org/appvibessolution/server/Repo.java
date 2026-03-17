package org.appvibessolution.server;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface Repo extends JpaRepository<ProjectDataModel, Long> {
}

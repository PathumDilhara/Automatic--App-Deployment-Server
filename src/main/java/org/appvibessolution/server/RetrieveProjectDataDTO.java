package org.appvibessolution.server;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RetrieveProjectDataDTO {
    private Long id;
    private String repoUrl;
    private String framework;
    private boolean DbInclude;
    private String dbType;
    private String dbUser;
    private String dbPassword;
    private String publicUrl;
    private Map<String, String> env;
    private LocalDateTime createdAt;
}

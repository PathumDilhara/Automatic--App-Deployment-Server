package org.appvibessolution.server;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateProjectDataDTO {
    private String repoUrl;
    private String framework;
    private boolean dbInclude;
    private String dbType;
    private String dbUser;
    private String dbPassword;
    private String publicUrl;
    private Map<String, String> env;
}

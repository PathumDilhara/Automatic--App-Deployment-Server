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
    private String databaseName;
    private String databasePassword;
    private String publicUrl;
    private Map<String, String> env;
}

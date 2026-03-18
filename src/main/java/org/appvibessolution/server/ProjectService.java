package org.appvibessolution.server;

import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
@Transactional
public class ProjectService {

    private final Repo repository;
    private final ModelMapper modelMapper;

    public ProjectService(Repo repository, ModelMapper modelMapper) {
        this.repository = repository;
        this.modelMapper = modelMapper;
    }

    public List<RetrieveProjectDataDTO> getAllProject() {
        List<ProjectDataModel> result = repository.findAll();
        return modelMapper.map(result, new TypeToken<List<RetrieveProjectDataDTO>>(){}.getType());
    }

    public RetrieveProjectDataDTO getProjectById(Long id) {
         ProjectDataModel model = repository.findById(id).orElseThrow(
                 ()-> new RuntimeException("Project not found"));

         return mapToDTO(model);
    }

    public RetrieveProjectDataDTO saveProject(CreateProjectDataDTO dto) {

        try {
            ProjectDataModel entity = new ProjectDataModel();
            entity.setRepoUrl(dto.getRepoUrl());
            entity.setFramework(dto.getFramework());
            entity.setDbType(dto.getDbType());
            entity.setDbUser(dto.getDbUser());
            entity.setDbPassword(dto.getDbPassword());
            entity.setPublicUrl(dto.getPublicUrl());

            dto.getEnv().forEach((key, value) -> {
                ProjectEnvironment env = new ProjectEnvironment();
                env.setKey(key);
                env.setValue(value);
                entity.addEnvironment(env);
            });

            // Save entity
            ProjectDataModel savedEntity = repository.save(entity);

            // Run jenkins file
             boolean isBuildDone = RunJenkins(dto.getFramework(), savedEntity.getId(), dto.isDbInclude(), dto.getDbUser(), dto.getDbPassword(), dto.getRepoUrl());

            return mapToDTO(savedEntity);
        } catch (Exception e) {
            System.out.println("### ERROR : " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void deleteAllProjects() {
        repository.deleteAll();
    }

    public void deleteProjectById(Long id) {
        repository.deleteById(id);
    }

    private RetrieveProjectDataDTO mapToDTO(ProjectDataModel entity) {
        RetrieveProjectDataDTO dto = new RetrieveProjectDataDTO();
        dto.setId(entity.getId());
        dto.setRepoUrl(entity.getRepoUrl());
        dto.setFramework(entity.getFramework());
        dto.setDbInclude(entity.isDbInclude());
        dto.setDbType(entity.getDbType());
        dto.setDbUser(entity.getDbUser());
        dto.setDbPassword(entity.getDbPassword());
        dto.setPublicUrl(entity.getPublicUrl());
        dto.setEnv(convertEnvListToMap(entity.getEnvironments()));
        return dto;
    }

    private Map<String, String> convertEnvListToMap(List<ProjectEnvironment> envList) {
        return envList.stream()
                .collect(Collectors.toMap(ProjectEnvironment::getKey, ProjectEnvironment::getValue));
    }

    private boolean RunJenkins (String framework, Long id, boolean INCLUDE_DB, String DBUser, String DBPassword, String repoUrl) {
        try {
            String jobName;
            String xmlPath;

            switch (framework.toLowerCase()) {
                case "spring":
                    System.out.println("### spring-boot fretwork");
                    jobName = "sboot-postgres-pipeline";
                    xmlPath = "/home/deploy/job/sboot-postgres-pipeline.xml";
                    break;
                case "next":
                    jobName = "nextjs-deploy-pipeline";
                    xmlPath = "/home/deploy/job/nextjs-deploy-pipeline.xml";
                    break;
//                case "react":
//                    jobName = "react-job";
//                    xmlPath = "/home/deploy/job/sboot-postgres-pipeline.xml";
//                    break;
//                case "node":
//                    jobName = "node-job";
//                    xmlPath = "/home/deploy/job/sboot-postgres-pipeline.xml";
//                    break;
                default:
                    throw new RuntimeException("Unsupported framework: " + framework);
            }

            String cliJar = "/home/deploy/jenkins-cli/jenkins-cli.jar";
            String jenkinsUrl = "http://10.20.11.250:8081/";
            String auth = "admin:77af06f09ebc4a659b4d84639ce241d7";

            // Step 1: Create or update job
            ProcessBuilder createJob = new ProcessBuilder(
                    "bash", "-c",
                    "java -jar " + cliJar +
                            " -s " + jenkinsUrl +
                            " -auth " + auth +
                            " -http update-job " + jobName +
                            " < " + xmlPath
            );

            createJob.inheritIO();
            Process p1 = createJob.start();
            int exit1 = p1.waitFor();
            if (exit1 != 0) throw new RuntimeException("Jenkins job update failed");

            String buildCmd;

            // Step 2: Trigger build with parameters
            if (INCLUDE_DB) {
                System.out.println("buildCmd yes db");
                buildCmd =
                        "java -jar " + cliJar +
                                " -s " + jenkinsUrl +
                                " -auth " + auth +
                                " -http build " + jobName +
                                " -p DEPLOY_ID=" + id.toString() +
                                " -p DB_USER=" + DBUser +
                                " -p DB_PASSWORD=" + DBPassword +
                                " -p INCLUDE_DB=" + Boolean.toString(INCLUDE_DB).toLowerCase() +
                                " -p DB_NAME=" + "app_db" +
                                " -p REPO_URL=\"" + repoUrl + "\"";

            } else {
                System.out.println("buildCmd no db");
                buildCmd =
                        "java -jar " + cliJar +
                                " -s " + jenkinsUrl +
                                " -auth " + auth +
                                " -http build " + jobName +
                                " -p DEPLOY_ID=" + id.toString()+
                                " -p INCLUDE_DB=" + Boolean.toString(INCLUDE_DB).toLowerCase() +
                                " -p REPO_URL=\"" + repoUrl + "\"";
            }

            ProcessBuilder buildJob = new ProcessBuilder("bash", "-c", buildCmd);
            buildJob.inheritIO();

            Process p2 = buildJob.start();
            p2.waitFor();

            System.out.println("### Jenkins job triggered: " + jobName);
            return true;

        } catch (Exception ex){
             throw new RuntimeException("Error triggering pipeline");
        }
    }
}

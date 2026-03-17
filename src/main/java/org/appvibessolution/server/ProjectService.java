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
            entity.setDatabaseName(dto.getDatabaseName());
            entity.setDatabasePassword(dto.getDatabasePassword());
            entity.setPublicUrl(dto.getPublicUrl());

            dto.getEnv().forEach((key, value) -> {
                ProjectEnvironment env = new ProjectEnvironment();
                env.setKey(key);
                env.setValue(value);
                entity.addEnvironment(env);
            });

            // Save entity
            ProjectDataModel savedEntity = repository.save(entity);

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
        dto.setDatabaseName(entity.getDatabaseName());
        dto.setDatabasePassword(entity.getDatabasePassword());
        dto.setPublicUrl(entity.getPublicUrl());
        dto.setEnv(convertEnvListToMap(entity.getEnvironments()));
        return dto;
    }

    private Map<String, String> convertEnvListToMap(List<ProjectEnvironment> envList) {
        return envList.stream()
                .collect(Collectors.toMap(ProjectEnvironment::getKey, ProjectEnvironment::getValue));
    }
}

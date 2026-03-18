package org.appvibessolution.server;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://10.20.11.250:3000")
@RequestMapping(value = "api/v1/server")
public class controller {

    private final ProjectService service;

    public controller(ProjectService service) {
        this.service = service;
    }

    // Get all users
    @GetMapping
    public List<RetrieveProjectDataDTO> getAllProjectData() {
        return service.getAllProject();
    }

    //Get user data by id
    @GetMapping("/{id}")
    public RetrieveProjectDataDTO getProjectDataById (@PathVariable Long id) {
        return service.getProjectById(id);
    }

    @PostMapping
    public RetrieveProjectDataDTO saveProjectData (@RequestBody CreateProjectDataDTO dto){
        return service.saveProject(dto);
    }

//    @PostMapping
//    public boolean triggerJob (@RequestBody CreateProjectDataDTO dto){
//        return service.RunJenkins(dto);
//    }

    @DeleteMapping
    public void deleteAllProjectData (){
        service.deleteAllProjects();
    }

    @DeleteMapping("/{id}")
    public void deleteProjectDataById (@PathVariable Long id){
        service.deleteProjectById(id);
    }
}

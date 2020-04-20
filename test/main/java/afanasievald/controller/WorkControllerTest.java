package afanasievald.controller;

import afanasievald.databaseEntity.Photo;
import afanasievald.datasource.DatasourceService;
import afanasievald.repository.FolderRepository;
import afanasievald.repository.PhotoRepository;
import afanasievald.uploadingfiles.storage.StorageService;
import org.junit.jupiter.api.Test;


import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.LinkedHashMap;

import static org.junit.jupiter.api.Assertions.*;

@AutoConfigureMockMvc
@SpringBootTest
class WorkControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DatasourceService datasourceService;

    @MockBean
    private StorageService storageService;

    @Autowired
    private PhotoRepository photoRepository;

    @Autowired
    private FolderRepository folderRepository;

   /* @GetMapping("/gallery")
    public String viewPhoto(Model model) {
        LinkedHashMap<String, Integer> folders = datasource.getFoldersWithPhotoHashcode(folderRepository, photoRepository);
        if (!folders.isEmpty()) {
            model.addAttribute("folders", folders.keySet());
            model.addAttribute("foldersAndPhotos", folders);
        }
        return "gallery";
    }*/

    @Test
    void viewPhoto() throws Exception {
       // when(datasourceService.getFoldersWithPhotoHashcode()).thenReturn("Hello, Mock");
        this.mockMvc.perform(get("/gallery")).andDo(print()).andExpect(status().isOk())
                .andExpect(content().string(containsString("gallery")));
    }

    @Test
    void uploadPhoto() {
    }

    @Test
    void showOneFoto() {
    }

    @Test
    void viewPhotoInFolder() {
    }

    @Test
    void testUploadPhoto() {
    }

 /*   @PostMapping("/photo/changedescription")
    public ResponseEntity changeDescription(@RequestBody Photo photo,
                                            Errors errors) throws Exception{
        datasourceService.changeDescription(photoRepository,
                photo.getHashcode(),
                photo.getDescription());
        return new ResponseEntity<>("", HttpStatus.OK);
    }*/
    @Test
    void changeDescription() {
    }

    @Test
    void handleStorageFileNotFound() {
    }
}
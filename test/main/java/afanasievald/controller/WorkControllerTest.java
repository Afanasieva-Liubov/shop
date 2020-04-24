package afanasievald.controller;

import afanasievald.databaseEntity.Folder;
import afanasievald.databaseEntity.Photo;
import afanasievald.repository.DatasourceHelper;
import afanasievald.repository.FolderRepository;
import afanasievald.repository.PhotoRepository;
import afanasievald.uploadingfiles.storage.PhotoSystemStorageService;
import org.junit.jupiter.api.Test;


import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@SpringBootTest
class WorkControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PhotoSystemStorageService storageService;

    @MockBean
    private PhotoRepository photoRepository;

    @MockBean
    private FolderRepository folderRepository;

    @Test
    void viewPhoto_WithFoldersAndPhotos() throws Exception {
        Folder folder1 = new Folder("folder1", new Date(1000L));
        List<Folder> folders = new LinkedList<>();
        folders.add(folder1);
        when(folderRepository.findByOrderByCreatedDateAsc()).thenReturn(folders);

        List<Photo> photos = new LinkedList<>();
        photos.add(new Photo(1, folder1,"photo", null));
        when(photoRepository.findByFolder(folder1)).thenReturn(photos);

        mockMvc.perform(get("/gallery"))
                .andExpect(status().isOk())
                .andExpect(model().size(2))
                .andExpect(model().attributeExists("folders"))
                .andExpect(model().attributeExists("foldersAndPhotos"))
                .andExpect(content().string(containsString("gallery")));
    }

    @Test
    void viewPhoto_WithoutFolders() throws Exception {
        mockMvc.perform(get("/gallery"))
                .andExpect(handler().handlerType(WorkController.class))
                .andExpect(handler().methodName("viewPhoto"))
                .andExpect(status().isOk())
                .andExpect(model().size(0))
                .andExpect(model().attributeDoesNotExist("folders"))
                .andExpect(model().attributeDoesNotExist("foldersAndPhotos"))
                .andExpect(content().string(containsString("gallery")));

       //проверить вызов 1 раз DatasourceHelper.getFoldersWithPhotoHashcode(folderRepository, photoRepository);
    }

    @Test
    void uploadPhoto() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                    .multipart("/gallery/folder/upload/foldername")
                )
                .andExpect(handler().handlerType(WorkController.class))
                .andExpect(handler().methodName("uploadPhoto"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/gallery/folder/foldername"));

        verify(storageService, times(1))
                .uploadPhotos(folderRepository,
                photoRepository,
                "foldername",
                 new MultipartFile[0]);
    }

    @Test
    void showOneFoto_NullableResource() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/gallery/folder/notexistingfolder/1"))
                .andExpect(handler().handlerType(WorkController.class))
                .andExpect(handler().methodName("showOneFoto"))
                .andExpect(status().isOk())
                .andReturn();

        verify(storageService, times(1))
                .loadPhotoAsResource(photoRepository,
                        "notexistingfolder",
                        1);

        MockHttpServletResponse mockHttpServletResponse = mvcResult.getResponse();
        assertEquals("", mockHttpServletResponse.getContentAsString());
    }


    @Test
    void showOneFoto_WithPhoto() throws Exception {
        Resource resource = new ByteArrayResource("mockfile".getBytes());
        when(storageService.loadPhotoAsResource(photoRepository, "foldername", 1))
                .thenReturn(resource);

        MvcResult mvcResult = mockMvc.perform(get("/gallery/folder/foldername/1"))
                .andExpect(handler().handlerType(WorkController.class))
                .andExpect(handler().methodName("showOneFoto"))
                .andExpect(status().isOk())
                .andReturn();

        MockHttpServletResponse mockHttpServletResponse = mvcResult.getResponse();
        assertEquals("mockfile", mockHttpServletResponse.getContentAsString());
    }

    @Test
    void viewPhotoInFolder_WithFolderPhotos() throws Exception {
        Folder folder1 = new Folder("folder1", new Date(1000L));
        Optional<Folder> folder = Optional.of(folder1);
        when(folderRepository.findByName(folder1.getName())).thenReturn(folder);

        List<Photo> photos = new LinkedList<>();
        photos.add(new Photo(1, folder1,"photo", null));
        when(photoRepository.findByFolder(folder1)).thenReturn(photos);

        mockMvc.perform(get(String.format("/gallery/folder/%s", folder1.getName())))
                .andExpect(status().isOk())
                .andExpect(model().size(1))
                .andExpect(model().attributeExists("photos"))
                .andExpect(content().string(containsString("folder")));
    }

    @Test
    void viewPhotoInFolder_WithoutFolder() throws Exception {
        mockMvc.perform(get("/gallery/folder/notexistingfolder"))
                .andExpect(status().isOk())
                .andExpect(model().size(0))
                .andExpect(model().attributeDoesNotExist("photos"))
                .andExpect(content().string(containsString("folder")));
    }

    @Test
    void changeDescription_ExistingPhoto() throws Exception {
        Photo photo = new Photo(1, null,"photo", null);
        when(photoRepository.findByHashcode(1)).thenReturn(Optional.of(photo));
        when(photoRepository.save(photo)).thenReturn(photo);
        mockMvc
                .perform(post("/photo/changedescription")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content("{\"hashcode\":1,\"description\":\"description\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void changeDescription_NotexistingPhoto() throws Exception {
        mockMvc.perform(post("/photo/changedescription"))
                .andExpect(status().is4xxClientError());
    }

    //нужно тестировать?
    @Test
    void handleStorageFileNotFound() {
    }
}
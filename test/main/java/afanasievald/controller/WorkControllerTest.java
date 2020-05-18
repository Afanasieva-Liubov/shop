package afanasievald.controller;

import afanasievald.databaseEntity.Folder;
import afanasievald.databaseEntity.Photo;
import afanasievald.repository.FolderRepository;
import afanasievald.repository.PhotoRepository;
import afanasievald.uploadingPhoto.PhotoStorageService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.util.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@SpringBootTest
class WorkControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PhotoStorageService storageService;

    @MockBean
    private PhotoRepository photoRepository;

    @MockBean
    private FolderRepository folderRepository;

    @Test
    void testViewPhoto_WithFoldersAndPhotos() throws Exception {
        Folder folder = new Folder("folder");
        List<Folder> folders = new LinkedList<>();
        folders.add(folder);
        when(folderRepository.findByOrderByCreatedDateAsc()).thenReturn(folders);

        List<Photo> photos = new LinkedList<>();
        photos.add(new Photo(1L, folder, "photo", "description"));
        when(photoRepository.findByFolder(folder)).thenReturn(photos);

        Map<String, Object> model = Objects.requireNonNull(mockMvc.perform(get("/gallery"))
                .andExpect(status().isOk())
                .andExpect(model().size(2))
                .andExpect(model().attributeExists("folders"))
                .andExpect(model().attributeExists("foldersAndPhotos"))
                .andExpect(content().string(containsString("gallery")))
                .andReturn()
                .getModelAndView())
                .getModel();

        Set<String> resultFolder = (Set<String>) model.get("folders");
        assertEquals(1, resultFolder.size());
        assertEquals(folder.getName(), (String) resultFolder.toArray()[0]);

        Map<String, Long> foldersAndPhotos = (Map<String, Long>) model.get("foldersAndPhotos");
        assertEquals(1, foldersAndPhotos.size());
        assertEquals(1L, foldersAndPhotos.get(folder.getName()));
    }

    @Test
    void testViewPhoto_WithoutFolders() throws Exception {
        Map<String, Object> model = Objects.requireNonNull(mockMvc.perform(get("/gallery"))
                .andExpect(handler().handlerType(WorkController.class))
                .andExpect(handler().methodName("viewPhoto"))
                .andExpect(status().isOk())
                .andExpect(model().size(2))
                .andExpect(model().attributeDoesNotExist("folders"))
                .andExpect(model().attributeDoesNotExist("foldersAndPhotos"))
                .andExpect(content().string(containsString("gallery")))
                .andReturn()
                .getModelAndView())
                .getModel();

        assertNull(model.get("folders"));
        assertNull(model.get("foldersAndPhotos"));
    }

    @Test
    void testShowOnePhoto_NotFoundPhoto() throws Exception {
        mockMvc.perform(get("/gallery/showOnePhoto/1"))
                .andExpect(handler().handlerType(WorkController.class))
                .andExpect(handler().methodName("showOnePhoto"))
                .andExpect(status().isNotFound())
                .andReturn();
        verify(storageService, times(1)).loadPhotoAsResource(photoRepository, 1L);
    }

    @Test
    void testShowOnePhoto_WithPhoto() throws Exception {
        when(storageService.loadPhotoAsResource(photoRepository, 1L))
                .thenReturn("mockphoto".getBytes());

        mockMvc.perform(get("/gallery/showOnePhoto/1"))
                .andExpect(handler().handlerType(WorkController.class))
                .andExpect(handler().methodName("showOnePhoto"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_JPEG))
                .andExpect(content().string("mockphoto"))
                .andReturn();
    }

    @Test
    void testViewPhotoInFolder_WithFolderPhotos() throws Exception {
        Folder folder = new Folder("folder");
        when(folderRepository.findByName(folder.getName())).thenReturn(Optional.of(folder));

        List<Photo> photos = new LinkedList<>();
        photos.add(new Photo(1L, folder, "photo", "description"));
        when(photoRepository.findByFolder(folder)).thenReturn(photos);

        Map<String, Object> model = Objects.requireNonNull(mockMvc.perform(get(String.format("/gallery/folder/%s", folder.getName())))
                .andExpect(handler().handlerType(WorkController.class))
                .andExpect(handler().methodName("viewPhotoInFolder"))
                .andExpect(status().isOk())
                .andExpect(model().size(2))
                .andExpect(model().attributeExists("photos"))
                .andExpect(model().attributeExists("isUploadable"))
                .andExpect(content().string(containsString("folder")))
                .andReturn()
                .getModelAndView())
                .getModel();

        List<Photo> resultPhotos = (List<Photo>) model.get("photos");
        assertEquals(1, resultPhotos.size());
        assertEquals(1L, ((Photo) resultPhotos.toArray()[0]).getIdentifier());

        Boolean isUploadable = (Boolean) model.get("isUploadable");
        assertTrue(isUploadable);
    }

    @Test
    void testViewPhotoInFolder_WithoutFolder() throws Exception {
        ModelAndView modelAndView = mockMvc.perform(get("/gallery/folder/notExistingFolder"))
                .andExpect(handler().handlerType(WorkController.class))
                .andExpect(handler().methodName("viewPhotoInFolder"))
                .andExpect(status().isOk())
                .andExpect(model().size(2))
                .andExpect(model().attributeExists("operationStatus"))
                .andExpect(model().attributeExists("isUploadable"))
                .andReturn()
                .getModelAndView();

        assertEquals(modelAndView.getViewName(), "folder");
        Boolean isUploadable = (Boolean) modelAndView.getModel().get("isUploadable");
        assertFalse(isUploadable);
    }

    @Test
    void testUploadPhoto_WithoutFiles() throws Exception {
        mockMvc.perform(multipart("/gallery/folder/upload/folderName"))
                .andExpect(handler().handlerType(WorkController.class))
                .andExpect(handler().methodName("uploadPhoto"))
                .andExpect(status().is3xxRedirection())
                .andExpect(model().size(0))
                .andExpect(flash().attributeCount(1))
                .andExpect(flash().attributeExists("operationStatus"))
                .andExpect(flash().attribute("operationStatus", "Download was correct"))
                .andExpect(redirectedUrl("/gallery/folder/folderName"));
    }

    @Test
    void testUploadPhoto_NullableFile() throws Exception {
        MockMultipartFile firstFile = new MockMultipartFile("files", null, "text/plain", (byte[]) null);
        mockMvc.perform(MockMvcRequestBuilders.multipart("/gallery/folder/upload/folderName")
                .file(firstFile))
                .andExpect(handler().handlerType(WorkController.class))
                .andExpect(handler().methodName("uploadPhoto"))
                .andExpect(status().is3xxRedirection())
                .andExpect(model().size(0))
                .andExpect(flash().attributeCount(1))
                .andExpect(flash().attributeExists("operationStatus"))
                .andExpect(flash().attribute("operationStatus", "Download wasn't correct"))
                .andExpect(redirectedUrl("/gallery/folder/folderName"));
    }

    @Test
    void testUploadPhoto_IOException() throws Exception {
        Folder folder = new Folder("folderName");
        Photo photo1 = new Photo(1L, folder, "photo1.jpg", "description");
        MockMultipartFile firstFile = new MockMultipartFile("files", photo1.getName(), "image/jpeg", photo1.getName().getBytes()) {
            @Override
            public byte[] getBytes() throws IOException {
                throw new IOException("io exception");
            }
        };

        mockMvc.perform(MockMvcRequestBuilders.multipart("/gallery/folder/upload/folderName")
                .file(firstFile))
                .andExpect(handler().handlerType(WorkController.class))
                .andExpect(handler().methodName("uploadPhoto"))
                .andExpect(status().is3xxRedirection())
                .andExpect(model().size(0))
                .andExpect(flash().attributeCount(1))
                .andExpect(flash().attributeExists("operationStatus"))
                .andExpect(flash().attribute("operationStatus", "Download wasn't correct"))
                .andExpect(redirectedUrl("/gallery/folder/folderName"));
    }

    @Test
    void testUploadPhoto_NotUploadableFile() throws Exception {
        MockMultipartFile firstFile = new MockMultipartFile("files", "notExistingFile.txt", "text/plain", "notExistingFile".getBytes());
        mockMvc.perform(MockMvcRequestBuilders.multipart("/gallery/folder/upload/folderName")
                .file(firstFile))
                .andExpect(handler().handlerType(WorkController.class))
                .andExpect(handler().methodName("uploadPhoto"))
                .andExpect(status().is3xxRedirection())
                .andExpect(model().size(0))
                .andExpect(flash().attributeCount(1))
                .andExpect(flash().attributeExists("operationStatus"))
                .andExpect(flash().attribute("operationStatus", "Download wasn't correct"))
                .andExpect(redirectedUrl("/gallery/folder/folderName"));
    }

    @Test
    void testUploadPhoto_CorrectUploadTwoFiles() throws Exception {
        Folder folder = new Folder("folderName");
        Photo photo1 = new Photo(1L, folder, "photo1.jpg", "description");
        Photo photo2 = new Photo(2L, folder, "photo2.jpg", "description");
        when(storageService.uploadPhotos(photo1.getName(), photo1.getName().getBytes())).thenReturn(photo1);
        when(storageService.uploadPhotos(photo2.getName(), photo2.getName().getBytes())).thenReturn(photo2);
        when(folderRepository.findByName(folder.getName())).thenReturn(Optional.of(folder));
        when(photoRepository.findByIdentifier(photo1.getIdentifier())).thenReturn(Optional.empty());
        when(photoRepository.findByIdentifier(photo2.getIdentifier())).thenReturn(Optional.empty());

        MockMultipartFile firstFile = new MockMultipartFile("files", photo1.getName(), "image/jpeg", photo1.getName().getBytes());
        MockMultipartFile secondFile = new MockMultipartFile("files", photo2.getName(), "image/jpeg", photo2.getName().getBytes());
        mockMvc.perform(MockMvcRequestBuilders.multipart(String.format("/gallery/folder/upload/%s", folder.getName()))
                .file(firstFile)
                .file(secondFile))
                .andExpect(handler().handlerType(WorkController.class))
                .andExpect(handler().methodName("uploadPhoto"))
                .andExpect(status().is3xxRedirection())
                .andExpect(model().size(0))
                .andExpect(flash().attributeCount(1))
                .andExpect(flash().attributeExists("operationStatus"))
                .andExpect(flash().attribute("operationStatus", "Download was correct"))
                .andExpect(redirectedUrl(String.format("/gallery/folder/%s", folder.getName())));
    }

    @Test
    void testChangeDescription_ExistingPhoto() throws Exception {
        Photo photo = new Photo(1L, new Folder(), "photo", "description");
        when(photoRepository.findByIdentifier(photo.getIdentifier())).thenReturn(Optional.of(photo));
        when(photoRepository.save(photo)).thenReturn(photo);
        mockMvc.perform(post("/photo/changedescription")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content("{\"identifier\":1,\"description\":\"description\"}"))
                .andExpect(handler().handlerType(WorkController.class))
                .andExpect(handler().methodName("changeDescription"))
                .andExpect(status().isOk());
    }

    @Test
    void testChangeDescription_NotExistingPhoto() throws Exception {
        mockMvc.perform(post("/photo/changedescription")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content("{\"identifier\":1,\"description\":\"description\"}"))
                .andExpect(handler().handlerType(WorkController.class))
                .andExpect(handler().methodName("changeDescription"))
                .andExpect(status().isInternalServerError());
    }
}
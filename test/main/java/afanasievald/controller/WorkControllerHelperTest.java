package afanasievald.controller;

import afanasievald.databaseEntity.Folder;
import afanasievald.databaseEntity.Photo;
import afanasievald.repository.FolderRepository;
import afanasievald.repository.PhotoRepository;
import afanasievald.uploadingPhoto.PhotoStorageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@SpringJUnitConfig
@DataJpaTest
class WorkControllerHelperTest {

    @MockBean
    private PhotoStorageService storageService;

    @Autowired
    private PhotoRepository photoRepository;

    @Autowired
    private FolderRepository folderRepository;

    //нормальные тесты ? или я должна была реальные тесты делать, без моков?
    @Test
    void testUploadOnePhoto_PhotoNotUploadToDisk() {
        String fileName = "fileName";
        when(storageService.uploadPhotos(fileName, fileName.getBytes())).thenReturn(null);
        boolean isUploaded = WorkControllerHelper.uploadOnePhoto(storageService,
                folderRepository,
                photoRepository, "",
                fileName,
                fileName.getBytes());
        assertFalse(isUploaded);
    }

    @Test
    void testUploadOnePhoto_PhotoNotSaveToDB() {
        Photo photo = new Photo();
        String fileName = "fileName";
        when(storageService.uploadPhotos(fileName, fileName.getBytes())).thenReturn(photo);

        boolean isUploaded = WorkControllerHelper.uploadOnePhoto(storageService,
                folderRepository,
                photoRepository, "fake name",
                fileName,
                fileName.getBytes());
        assertFalse(isUploaded);
        verify(storageService, times(1)).deletePhoto(photo);
    }

    @Test
    void testUploadOnePhoto_Correctly() {
        Folder folder = folderRepository.save(new Folder("folderName"));
        Photo photo = new Photo();
        photo.setName("fileName");
        photo.setIdentifier(1L);
        when(storageService.uploadPhotos(photo.getName(), "photo".getBytes())).thenReturn(photo);

        boolean isUploaded = WorkControllerHelper.uploadOnePhoto(storageService,
                folderRepository,
                photoRepository,
                folder.getName(),
                photo.getName(),
                "photo".getBytes());
        assertTrue(isUploaded);
    }
}
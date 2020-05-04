package afanasievald.uploadingPhoto;

import afanasievald.databaseEntity.Photo;
import afanasievald.repository.FolderRepository;
import afanasievald.repository.PhotoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import java.net.MalformedURLException;
import java.nio.file.InvalidPathException;
import static org.junit.jupiter.api.Assertions.*;

@SpringJUnitConfig
@DataJpaTest
@ComponentScan("afanasievald.uploadingPhoto")
@TestPropertySource("classpath:application-test.properties")
class PhotoStorageServiceTest {

    @Autowired
    private StorageProperties storageProperties;

    @Autowired
    private PhotoRepository photoRepository;

    @Autowired
    private FolderRepository folderRepository;

    @Autowired
    private PhotoStorageService storageService;

    @Test
    void uploadPhotos() {
    }


    @Test
    void loadPhotoAsResource_NullableFolder() throws MalformedURLException {
      //  Resource resource = storageService.loadPhotoAsResource(photoRepository, null, 0);
     //   assertEquals(null, resource);
    }

    @Test
    void loadPhotoAsResource_EmptyFolder() throws MalformedURLException {
      //  Resource resource = storageService.loadPhotoAsResource(photoRepository, "", 0);
      //  assertEquals(null, resource);
    }

    @Test
    void loadPhotoAsResource_NotexistingPhoto() throws MalformedURLException {
      //  Resource resource = storageService.loadPhotoAsResource(photoRepository, "foldername", 0);
       // assertEquals(null, resource);
    }

    @Test
    void loadPhotoAsResource_StorageFileNotFoundException() {
        Photo photo = photoRepository.save(new Photo(1, null,"filename", null));
      //  Exception exception = assertThrows(StorageFileNotFoundException.class, () -> {
         //   storageService.loadPhotoAsResource(photoRepository, "foldername", 1);
       // });
    }

    @Test
    void loadPhotoAsResource_InvalidPathException() {
        Photo photo = photoRepository.save(new Photo(1, null,"filename", null));
        Exception exception = assertThrows(InvalidPathException.class, () -> {
          //  storageService.loadPhotoAsResource(photoRepository, "/D:/;foldername", 1);
        });
    }

    @Test
    void loadPhotoAsResource_GetResources() throws MalformedURLException {
        Photo photo = photoRepository.save(new Photo(1, null,"test.jpg", null));
       // Resource resource = storageService.loadPhotoAsResource(photoRepository, "test", 1);
       // assertNotNull(resource);

       // String fileName = resource.getFilename();
       // assertEquals("test.jpg", fileName);
    }
}
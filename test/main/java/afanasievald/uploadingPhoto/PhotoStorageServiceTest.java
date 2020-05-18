package afanasievald.uploadingPhoto;

import afanasievald.databaseEntity.Folder;
import afanasievald.databaseEntity.Photo;
import afanasievald.repository.FolderRepository;
import afanasievald.repository.PhotoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
    void testPhotoStorageService_CorrectPhotoLocation() throws IOException {
        PhotoStorageService photoStorageService = new PhotoStorageService(storageProperties);
        assertNotNull(photoStorageService);
    }

    @Test
    void testPhotoStorageService_CreateDirectory() throws IOException {
        StorageProperties properties = new StorageProperties();
        properties.setPhotoLocation("../shop/test/main/resources/static/image/newfolder");
        PhotoStorageService photoStorageService = new PhotoStorageService(properties);
        assertNotNull(photoStorageService);
        Files.delete(Paths.get(properties.getPhotoLocation()));
    }

    @Test
    void testPhotoStorageService_IllegalArgumentException(){
        StorageProperties properties = new StorageProperties();
        properties.setPhotoLocation(String.format("%s%s", storageProperties.getPhotoLocation(), "testPhotoStorageService_IllegalArgumentException.html" ));
        assertThrows(IllegalArgumentException.class, () -> new PhotoStorageService(properties));
    }

    @Test
    void testUploadPhotos_NullableMimeType() {
        Photo photo = storageService.uploadPhotos("testNotImageFile", "testNotImageFile".getBytes());
        assertNull(photo);
    }

    @Test
    void testUploadPhotos_NotImageMimeType() throws IOException {
        String photoLocation = storageProperties.getPhotoLocation();
        Path fileNameAndPath = Paths.get(photoLocation, "testWithMimeType.html");
        byte[] byteArray = Files.readAllBytes(fileNameAndPath);
        Photo photo = storageService.uploadPhotos("testWithMimeType.html", byteArray);
        assertNull(photo);
    }

    @Test
    void testUploadPhotos_ImageFile() throws IOException {
        String photoLocation = storageProperties.getPhotoLocation();
        Path fileNameAndPath = Paths.get(photoLocation, "test.jpg");
        byte[] byteArray = Files.readAllBytes(fileNameAndPath);
        Photo photo = storageService.uploadPhotos("test.jpg", byteArray);
        assertNotNull(photo);
        fileNameAndPath = Paths.get(photoLocation, photo.getName());
        Files.delete(fileNameAndPath);
    }

    @Test
    void testDeletePhoto_NotExistingFile() {
        Folder folder = folderRepository.save(new Folder());
        Photo photo = photoRepository.save(new Photo(1, folder,"NotExistingFile", "description"));
        boolean isDeleted = storageService.deletePhoto(photo);
        assertFalse(isDeleted);
    }

    @Test
    void testDeletePhoto_ExistingFile() throws IOException {
        Folder folder = folderRepository.save(new Folder());
        Photo photo = photoRepository.save(new Photo(1, folder,"testDeletePhoto_ExistingFile.jpg", "description"));
        String photoLocation = storageProperties.getPhotoLocation();
        Path fileNameAndPath = Paths.get(photoLocation, photo.getName());
        Files.write(fileNameAndPath, "fake file".getBytes());
        boolean isDeleted = storageService.deletePhoto(photo);
        assertTrue(isDeleted);
    }

    /*@Test
    void testDeletePhoto_IOException() throws IOException, InterruptedException {
        Folder folder = folderRepository.save(new Folder());
        Photo photo = photoRepository.save(new Photo(1, folder,"testDeletePhoto_IOException.jpg", "description"));

        Runtime rt = Runtime.getRuntime();
        Process proc = rt.exec(String.format("attrib -r %s%s",
                storageProperties.getPhotoLocation(), photo.getName()));
        int exitVal = proc.waitFor();

        boolean isDeleted = storageService.deletePhoto(photo);
        assertTrue(isDeleted);
    }*/

    @Test
    void testLoadPhotoAsResource_NotExistingPhoto() {
        Folder folder = folderRepository.save(new Folder());
        photoRepository.save(new Photo(1, folder, "name", "description"));
        byte[] byteArray = storageService.loadPhotoAsResource(photoRepository, 2);
        assertNull(byteArray);
    }

    @Test
    void testLoadPhotoAsResource_EmptyPhotoName() {
        Folder folder = folderRepository.save(new Folder());
        photoRepository.save(new Photo(1, folder, "", "description"));
        byte[] byteArray = storageService.loadPhotoAsResource(photoRepository, 1);
        assertNull(byteArray);
    }

    @Test
    void testLoadPhotoAsResource_InvalidPathException() {
        Folder folder = folderRepository.save(new Folder());
        photoRepository.save(new Photo(1, folder,"/D:/;foldername", "description"));
        byte[] byteArray = storageService.loadPhotoAsResource(photoRepository, 1);
        assertNull(byteArray);
    }

    @Test
    void testLoadPhotoAsResource_NotExistingFile() {
        Folder folder = folderRepository.save(new Folder());
        photoRepository.save(new Photo(1, folder,"NotExistingFile", "description"));
        byte[] byteArray = storageService.loadPhotoAsResource(photoRepository, 1);
        assertNull(byteArray);
    }




    /*void testLoadPhotoAsResource_NotReadableFile() throws IOException {
        Folder folder = folderRepository.save(new Folder());
        Photo photo = photoRepository.save(new Photo(1, folder,"test2.jpg", "description"));
        String photoLocation = storageProperties.getPhotoLocation();
        Path fileNameAndPath = Paths.get(photoLocation, photo.getName());

        File file = new File(fileNameAndPath.toString());
        //boolean created = file.createNewFile();
        boolean isReadOnly = file.setReadable(true, false);
        boolean isReadable = Files.isReadable(fileNameAndPath);
        byte[] byteArray = storageService.loadPhotoAsResource(photoRepository, photo.getIdentifier());
        assertNull(byteArray);
    }*/

    @Test
    void testLoadPhotoAsResource_ExistingFile() {
        Folder folder = folderRepository.save(new Folder());
        photoRepository.save(new Photo(1, folder,"test.jpg", "description"));
        byte[] byteArray = storageService.loadPhotoAsResource(photoRepository, 1);
        assertNotNull(byteArray);
    }



   /* @Test
    void testLoadPhotoAsResource_IOExceptionForWindows() throws IOException, InterruptedException {
        Folder folder = folderRepository.save(new Folder());
        Photo photo = photoRepository.save(new Photo(1, folder,"test3.jpg", "description"));
        Runtime rt = Runtime.getRuntime();

        Process proc = rt.exec(String.format("attrib -r %s%s",
                storageProperties.getPhotoLocation(), photo.getName()));
        int exitVal = proc.waitFor();
        byte[] byteArray = storageService.loadPhotoAsResource(photoRepository, 1);
        assertNull(byteArray);
    }*/


}
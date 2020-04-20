package afanasievald.datasource;

import afanasievald.databaseEntity.Folder;
import afanasievald.databaseEntity.Photo;
import afanasievald.repository.FolderRepository;
import afanasievald.repository.PhotoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
@SpringJUnitConfig
//@SpringBootTest
@DataJpaTest
class DatasourceServiceTest {

    @Autowired
    private PhotoRepository photoRepository;

    @Autowired
    private FolderRepository folderRepository;


    @Test /*Без папок вернуть пустой объект>*/
    void getFoldersWithPhotoHashcode_EmptyFolders() {
        DatasourceService datasourceService = new DatasourceService();
        LinkedHashMap<String, Integer> folders = datasourceService.getFoldersWithPhotoHashcode(folderRepository, photoRepository);

        assertNotNull(folders);
        assertEquals(true, folders.isEmpty());
    }

    /*Добавить виртуальные папки, папки вернуть отсортированные по дате*/
    @Test
    void getFoldersWithPhotoHashcode_SortedFolders() {
        DatasourceService datasourceService = new DatasourceService();

        Folder folder2 = folderRepository.save(new Folder("test2", new Date(2000L)));
        Folder folder1 = folderRepository.save(new Folder("test1", new Date(1000L)));

        LinkedHashMap<String, Integer> foundFolders = datasourceService.getFoldersWithPhotoHashcode(folderRepository, photoRepository);

        assertNotNull(foundFolders);
        assertEquals(2, foundFolders.size());

        Iterator<String> keys = foundFolders.keySet().iterator();
        assertEquals(folder1.getName(), keys.next());
    }

    /*Добавить виртуальные папки без фото, вернуть папки с пустыми хешкодами*/
    @Test
    void getFoldersWithPhotoHashcode_EmptyPhotos() {
        DatasourceService datasourceService = new DatasourceService();

        folderRepository.save(new Folder("test"));

        LinkedHashMap<String, Integer> foundFolders = datasourceService.getFoldersWithPhotoHashcode(folderRepository, photoRepository);

        assertNotNull(foundFolders);
        assertEquals(1, foundFolders.size());

        Iterator<Integer> values = foundFolders.values().iterator();
        assertNull(values.next());
    }

    /*Добавить 1 виртуальную папку, вирт фото вернуть c наименьшей датой*/
    @Test
    void getFoldersWithPhotoHashcode_SortedPhotos() {
        DatasourceService datasourceService = new DatasourceService();

        Folder folder = folderRepository.save(new Folder("test"));
        Photo photo2 = photoRepository.save(new Photo(2, folder, "test", null, new Date(2000L)));
        Photo photo1 = photoRepository.save(new Photo(1, folder, "test", null, new Date(1000L)));

        LinkedHashMap<String, Integer> foundFolders = datasourceService.getFoldersWithPhotoHashcode(folderRepository, photoRepository);

        assertNotNull(foundFolders);
        assertEquals(1, foundFolders.size());

        Iterator<Integer> values = foundFolders.values().iterator();
        assertEquals(1, values.next());
    }

    @Test
    void getPhotosFromFolder_EmptyFolder() {
        Folder folder = folderRepository.save(new Folder("test"));

        DatasourceService datasourceService = new DatasourceService();
        List<Photo> photos = datasourceService.getPhotosFromFolder(folderRepository, photoRepository, folder.getName());

        assertNotNull(photos);
        assertEquals(true, photos.isEmpty());
    }

    @Test
    void getPhotosFromFolder_SortedPhotos() {
        Folder folder = folderRepository.save(new Folder("test"));

        Photo photo2 = photoRepository.save(new Photo(2, folder, "test", null, new Date(2000L)));
        Photo photo1 = photoRepository.save(new Photo(1, folder, "test", null, new Date(1000L)));

        DatasourceService datasourceService = new DatasourceService();
        List<Photo> photos = datasourceService.getPhotosFromFolder(folderRepository, photoRepository, folder.getName());

        assertNotNull(photos);
        assertEquals(2, photos.size());
        assertEquals(photo1.getId(), photos.get(0).getId());
        assertEquals(photo2.getId(), photos.get(1).getId());
    }

    @Test
    void savePhotoToFolder_NotExistedFolder() throws Exception {
        DatasourceService datasourceService = new DatasourceService();
        Exception exception = assertThrows(Exception.class, () -> {
            datasourceService.savePhotoToFolder(folderRepository, photoRepository, 1, "not existed folder", "filename");
        });
    }

    @Test
    void savePhotoToFolder_DuplicatePhotoHashcode() throws Exception {
        Folder folder = folderRepository.save(new Folder("test"));
        Photo photo = photoRepository.save(new Photo(1, folder, "filename1", null, new Date(1000L)));

        DatasourceService datasourceService = new DatasourceService();
        Exception exception = assertThrows(Exception.class, () -> {
            datasourceService.savePhotoToFolder(folderRepository, photoRepository, photo.getHashcode(), folder.getName(), photo.getName());
        });
    }

    @Test
    void savePhotoToFolder() throws Exception {
        Folder folder = folderRepository.save(new Folder("test"));

        DatasourceService datasourceService = new DatasourceService();
        datasourceService.savePhotoToFolder(folderRepository, photoRepository, 1, folder.getName(), "filename");

        Optional<Photo> photo = photoRepository.findByHashcode(1);
        assertEquals(true, photo.isPresent());
    }

    @Test
    void changeDescription_NotExistedPhoto() {
        DatasourceService datasourceService = new DatasourceService();
        Exception exception = assertThrows(Exception.class, () -> {
            datasourceService.changeDescription(photoRepository, 1, "not existed photo");
        });
    }

    @Test
    void changeDescription() throws Exception{
        Folder folder = folderRepository.save(new Folder("test1002"));
        Photo photo = photoRepository.save(new Photo(2, folder, "test", null, new Date(1000L)));

        DatasourceService datasourceService = new DatasourceService();
        String newdescription = "new description";
        datasourceService.changeDescription(photoRepository, photo.getHashcode(), newdescription);

        Optional<Photo> photoWithNewDescription = photoRepository.findByHashcode(photo.getHashcode());
        assertEquals(newdescription, photoWithNewDescription.get().getDescription());
    }
}
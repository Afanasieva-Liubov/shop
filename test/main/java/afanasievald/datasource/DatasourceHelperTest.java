package afanasievald.datasource;

import afanasievald.databaseEntity.Folder;
import afanasievald.databaseEntity.Photo;
import afanasievald.repository.DatasourceHelper;
import afanasievald.repository.FolderRepository;
import afanasievald.repository.PhotoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
@SpringJUnitConfig
@DataJpaTest
class DatasourceHelperTest {

    @Autowired
    private PhotoRepository photoRepository;

    @Autowired
    private FolderRepository folderRepository;


    @Test /*Без папок вернуть пустой объект>*/
    void getFoldersWithPhotoIdentifier_EmptyFolders() {
        DatasourceHelper datasourceHelper = new DatasourceHelper();
        LinkedHashMap<String, Integer> folders = datasourceHelper.getFoldersWithPhotoIdentifier(folderRepository, photoRepository);

        assertNotNull(folders);
        assertEquals(true, folders.isEmpty());
    }

    /*Добавить виртуальные папки, папки вернуть отсортированные по дате*/
    @Test
    void getFoldersWithPhotoIdentifier_SortedFolders() {
        DatasourceHelper datasourceHelper = new DatasourceHelper();

        Folder folder2 = folderRepository.save(new Folder("test2", new Date(2000L)));
        Folder folder1 = folderRepository.save(new Folder("test1", new Date(1000L)));

        LinkedHashMap<String, Integer> foundFolders = datasourceHelper.getFoldersWithPhotoIdentifier(folderRepository, photoRepository);

        assertNotNull(foundFolders);
        assertEquals(2, foundFolders.size());

        Iterator<String> keys = foundFolders.keySet().iterator();
        assertEquals(folder1.getName(), keys.next());
    }

    /*Добавить виртуальные папки без фото, вернуть папки с пустыми хешкодами*/
    @Test
    void getFoldersWithPhotoIdentifier_EmptyPhotos() {
        DatasourceHelper datasourceHelper = new DatasourceHelper();

        folderRepository.save(new Folder("test"));

        LinkedHashMap<String, Integer> foundFolders = datasourceHelper.getFoldersWithPhotoIdentifier(folderRepository, photoRepository);

        assertNotNull(foundFolders);
        assertEquals(1, foundFolders.size());

        Iterator<Integer> values = foundFolders.values().iterator();
        assertNull(values.next());
    }

    /*Добавить 1 виртуальную папку, вирт фото вернуть c наименьшей датой*/
    @Test
    void getFoldersWithPhotoIdentifier_SortedPhotos() {
        DatasourceHelper datasourceHelper = new DatasourceHelper();

        Folder folder = folderRepository.save(new Folder("test"));
        Photo photo2 = photoRepository.save(new Photo(2, folder, "test", null, new Date(2000L)));
        Photo photo1 = photoRepository.save(new Photo(1, folder, "test", null, new Date(1000L)));

        LinkedHashMap<String, Integer> foundFolders = datasourceHelper.getFoldersWithPhotoIdentifier(folderRepository, photoRepository);

        assertNotNull(foundFolders);
        assertEquals(1, foundFolders.size());

        Iterator<Integer> values = foundFolders.values().iterator();
        assertEquals(1, values.next());
    }

    @Test
    void getPhotosFromFolder_EmptyFolder() {
        Folder folder = folderRepository.save(new Folder("test"));

        DatasourceHelper datasourceHelper = new DatasourceHelper();
        List<Photo> photos = datasourceHelper.getPhotosFromFolder(folderRepository, photoRepository, folder.getName());

        assertNotNull(photos);
        assertEquals(true, photos.isEmpty());
    }

    @Test
    void getPhotosFromFolder_SortedPhotos() {
        Folder folder = folderRepository.save(new Folder("test"));

        Photo photo2 = photoRepository.save(new Photo(2, folder, "test", null, new Date(2000L)));
        Photo photo1 = photoRepository.save(new Photo(1, folder, "test", null, new Date(1000L)));

        DatasourceHelper datasourceHelper = new DatasourceHelper();
        List<Photo> photos = datasourceHelper.getPhotosFromFolder(folderRepository, photoRepository, folder.getName());

        assertNotNull(photos);
        assertEquals(2, photos.size());
        assertEquals(photo1.getId(), photos.get(0).getId());
        assertEquals(photo2.getId(), photos.get(1).getId());
    }

    @Test
    void savePhotoToFolder_NotExistedFolder() throws Exception {
        DatasourceHelper datasourceHelper = new DatasourceHelper();
        Exception exception = assertThrows(Exception.class, () -> {
            datasourceHelper.savePhotoToFolder(folderRepository, photoRepository, 1, "not existed folder", "filename");
        });
    }

    @Test
    void savePhotoToFolder_DuplicatePhotoIdentifier() throws Exception {
        Folder folder = folderRepository.save(new Folder("test"));
        Photo photo = photoRepository.save(new Photo(1, folder, "filename1", null, new Date(1000L)));

        DatasourceHelper datasourceHelper = new DatasourceHelper();
        Exception exception = assertThrows(Exception.class, () -> {
            datasourceHelper.savePhotoToFolder(folderRepository, photoRepository, photo.getIdentifier(), folder.getName(), photo.getName());
        });
    }

    @Test
    void savePhotoToFolder() throws Exception {
        Folder folder = folderRepository.save(new Folder("test"));

        DatasourceHelper datasourceHelper = new DatasourceHelper();
        datasourceHelper.savePhotoToFolder(folderRepository, photoRepository, 1, folder.getName(), "filename");

        Optional<Photo> photo = photoRepository.findByIdentifier(1);
        assertEquals(true, photo.isPresent());
    }

    @Test
    void changeDescription_NotExistedPhoto() {
        DatasourceHelper datasourceHelper = new DatasourceHelper();
        Exception exception = assertThrows(Exception.class, () -> {
            datasourceHelper.changeDescription(photoRepository, 1, "not existed photo");
        });
    }

    @Test
    void changeDescription() throws Exception{
        Folder folder = folderRepository.save(new Folder("test1002"));
        Photo photo = photoRepository.save(new Photo(2, folder, "test", null, new Date(1000L)));

        DatasourceHelper datasourceHelper = new DatasourceHelper();
        String newdescription = "new description";
        datasourceHelper.changeDescription(photoRepository, photo.getIdentifier(), newdescription);

        Optional<Photo> photoWithNewDescription = photoRepository.findByIdentifier(photo.getIdentifier());
        assertEquals(newdescription, photoWithNewDescription.get().getDescription());
    }
}
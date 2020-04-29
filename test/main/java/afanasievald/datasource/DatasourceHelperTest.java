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

import java.nio.file.LinkOption;
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
        LinkedHashMap<String, Long> folders = DatasourceHelper.getFoldersWithPhotoIdentifier(folderRepository, photoRepository);

        assertNotNull(folders);
        assertEquals(true, folders.isEmpty());
    }

    /*Добавить виртуальные папки, папки вернуть отсортированные по дате*/
    @Test
    void getFoldersWithPhotoIdentifier_SortedFolders() {
        Folder folder2 = folderRepository.save(new Folder("test2", new Date(2000L)));
        Folder folder1 = folderRepository.save(new Folder("test1", new Date(1000L)));

        LinkedHashMap<String, Long> foundFolders = DatasourceHelper.getFoldersWithPhotoIdentifier(folderRepository, photoRepository);

        assertNotNull(foundFolders);
        assertEquals(2, foundFolders.size());

        Iterator<String> keys = foundFolders.keySet().iterator();
        assertEquals(folder1.getName(), keys.next());
    }

    /*Добавить виртуальные папки без фото, вернуть папки с пустыми хешкодами*/
    @Test
    void getFoldersWithPhotoIdentifier_EmptyPhotos() {
        folderRepository.save(new Folder("test"));

        LinkedHashMap<String, Long> foundFolders = DatasourceHelper.getFoldersWithPhotoIdentifier(folderRepository, photoRepository);

        assertNotNull(foundFolders);
        assertEquals(1, foundFolders.size());

        Iterator<Long> values = foundFolders.values().iterator();
        assertNull(values.next());
    }

    /*Добавить 1 виртуальную папку, вирт фото вернуть c наименьшей датой*/
    @Test
    void getFoldersWithPhotoIdentifier_SortedPhotos() {
        Folder folder = folderRepository.save(new Folder("test"));
        Photo photo2 = photoRepository.save(new Photo(2, folder, "test", null, new Date(2000L)));
        Photo photo1 = photoRepository.save(new Photo(1, folder, "test", null, new Date(1000L)));

        LinkedHashMap<String, Long> foundFolders = DatasourceHelper.getFoldersWithPhotoIdentifier(folderRepository, photoRepository);

        assertNotNull(foundFolders);
        assertEquals(1, foundFolders.size());

        Iterator<Long> values = foundFolders.values().iterator();
        assertEquals(1, values.next());
    }

    @Test
    void getPhotosFromFolder_EmptyFolder() {
        Folder folder = folderRepository.save(new Folder("test"));

        List<Photo> photos = DatasourceHelper.getPhotosFromFolder(folderRepository, photoRepository, folder.getName());

        assertNotNull(photos);
        assertEquals(true, photos.isEmpty());
    }

    @Test
    void getPhotosFromFolder_SortedPhotos() {
        Folder folder = folderRepository.save(new Folder("test"));

        Photo photo2 = photoRepository.save(new Photo(2, folder, "test", null, new Date(2000L)));
        Photo photo1 = photoRepository.save(new Photo(1, folder, "test", null, new Date(1000L)));

        List<Photo> photos = DatasourceHelper.getPhotosFromFolder(folderRepository, photoRepository, folder.getName());

        assertNotNull(photos);
        assertEquals(2, photos.size());
        assertEquals(photo1.getIdentifier(), photos.get(0).getIdentifier());
        assertEquals(photo2.getIdentifier(), photos.get(1).getIdentifier());
    }

    @Test
    void savePhotoToFolder_NotExistedFolder() throws Exception {
        Exception exception = assertThrows(Exception.class, () -> {
            Photo photo = new Photo();
            photo.setIdentifier(1l);
            photo.setName("filename");
            DatasourceHelper.savePhotoToFolder(folderRepository, photoRepository, "not existed folder", photo);
        });
    }

    @Test
    void savePhotoToFolder_DuplicatePhotoIdentifier() throws Exception {
        Folder folder = folderRepository.save(new Folder("test"));
        Photo photo = photoRepository.save(new Photo(1l, folder, "filename1", null, new Date(1000L)));

        Exception exception = assertThrows(Exception.class, () -> {
            DatasourceHelper.savePhotoToFolder(folderRepository, photoRepository, folder.getName(), photo);
        });
    }

    @Test
    void savePhotoToFolder() throws Exception {
        Folder folder = folderRepository.save(new Folder("test"));
        Photo photo = new Photo();
        photo.setIdentifier(1l);
        photo.setName("filename");

        DatasourceHelper.savePhotoToFolder(folderRepository, photoRepository, folder.getName(), photo);

        Optional<Photo> photoRezult = photoRepository.findByIdentifier(1l);
        assertEquals(true, photoRezult.isPresent());
    }

    @Test
    void changeDescription_NotExistedPhoto() {
        Exception exception = assertThrows(Exception.class, () -> {
            DatasourceHelper.changeDescription(photoRepository, 1l, "not existed photo");
        });
    }

    @Test
    void changeDescription() throws Exception{
        Folder folder = folderRepository.save(new Folder("test1002"));
        Photo photo = photoRepository.save(new Photo(2, folder, "test", null, new Date(1000L)));

        String newdescription = "new description";
        DatasourceHelper.changeDescription(photoRepository, photo.getIdentifier(), newdescription);

        Optional<Photo> photoWithNewDescription = photoRepository.findByIdentifier(photo.getIdentifier());
        assertEquals(newdescription, photoWithNewDescription.get().getDescription());
    }
}
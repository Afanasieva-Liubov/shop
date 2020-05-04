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
        Map<String, Long> folders = DatasourceHelper.getFoldersWithPhotoIdentifier(folderRepository, photoRepository);

        assertNotNull(folders);
        assertTrue(folders.isEmpty());
    }

    /*Добавить виртуальные папки, папки вернуть отсортированные по дате*/
    @Test
    void getFoldersWithPhotoIdentifier_SortedFolders() {
        folderRepository.save(new Folder("test2", new Date(2000L)));
        Folder folder1 = folderRepository.save(new Folder("test1", new Date(1000L)));

        Map<String, Long> foundFolders = DatasourceHelper.getFoldersWithPhotoIdentifier(folderRepository, photoRepository);

        assertNotNull(foundFolders);
        assertEquals(2, foundFolders.size());

        Iterator<String> keys = foundFolders.keySet().iterator();
        assertEquals(folder1.getName(), keys.next());
    }

    /*Добавить виртуальные папки без фото, вернуть папки с пустыми фото*/
    @Test
    void getFoldersWithPhotoIdentifier_EmptyPhotos() {
        folderRepository.save(new Folder("test"));

        Map<String, Long> foundFolders = DatasourceHelper.getFoldersWithPhotoIdentifier(folderRepository, photoRepository);

        assertNotNull(foundFolders);
        assertEquals(1, foundFolders.size());

        Iterator<Long> values = foundFolders.values().iterator();
        assertNull(values.next());
    }

    /*Добавить 1 виртуальную папку, вирт фото вернуть c наименьшей датой*/
    @Test
    void getFoldersWithPhotoIdentifier_SortedPhotos() {
        Folder folder = folderRepository.save(new Folder("test"));
        photoRepository.save(new Photo(2L, folder, "test", "description", new Date(2000L)));
        Photo photo1 = photoRepository.save(new Photo(1L, folder, "test", "description", new Date(1000L)));

        Map<String, Long> foundFolders = DatasourceHelper.getFoldersWithPhotoIdentifier(folderRepository, photoRepository);

        assertNotNull(foundFolders);
        assertEquals(1, foundFolders.size());

        Iterator<Long> values = foundFolders.values().iterator();
        assertEquals(photo1.getIdentifier(), values.next());
    }

    @Test
    void getPhotosFromFolder_NotExistingFolder() {
        List<Photo> photo = DatasourceHelper.getPhotosFromFolder(folderRepository, photoRepository, "not existing folder");
        assertNull(photo);
    }

    @Test
    void getPhotosFromFolder_EmptyFolder() {
        Folder folder = folderRepository.save(new Folder("test"));
        List<Photo> photos = DatasourceHelper.getPhotosFromFolder(folderRepository, photoRepository, folder.getName());
        assertNotNull(photos);
        assertTrue(photos.isEmpty());
    }

    @Test
    void getPhotosFromFolder_SortedPhotos() {
        Folder folder = folderRepository.save(new Folder("test"));

        Photo photo2 = photoRepository.save(new Photo(2L, folder, "test", "description", new Date(2000L)));
        Photo photo1 = photoRepository.save(new Photo(1L, folder, "test", "description", new Date(1000L)));

        List<Photo> photos = DatasourceHelper.getPhotosFromFolder(folderRepository, photoRepository, folder.getName());

        assertNotNull(photos);
        assertEquals(2, photos.size());
        assertEquals(photo1.getIdentifier(), photos.get(0).getIdentifier());
        assertEquals(photo2.getIdentifier(), photos.get(1).getIdentifier());
    }

    @Test
    void savePhotoToFolder_NotExistedFolder() {
        boolean isSaved = DatasourceHelper.savePhotoToFolder(folderRepository, photoRepository, "not existed folder", new Photo());
        assertFalse(isSaved);
    }

    @Test
    void savePhotoToFolder_DuplicatePhotoIdentifier() {
        Folder folder = folderRepository.save(new Folder("test"));
        Photo photo = photoRepository.save(new Photo(1L, folder, "filename1", "description", new Date(1000L)));
        boolean isSaved = DatasourceHelper.savePhotoToFolder(folderRepository, photoRepository, folder.getName(), photo);
        assertFalse(isSaved);
    }

    @Test
    void savePhotoToFolder() {
        Folder folder = folderRepository.save(new Folder("test"));
        Photo photo = new Photo();
        photo.setIdentifier(1L);
        photo.setName("filename");

        boolean isSaved = DatasourceHelper.savePhotoToFolder(folderRepository, photoRepository, folder.getName(), photo);

        assertTrue(isSaved);
        Optional<Photo> photoResult = photoRepository.findByIdentifier(photo.getIdentifier());
        assertTrue(photoResult.isPresent());
        assertEquals(folder.getId(), photo.getFolder().getId());
    }

    @Test
    void changeDescription_NotExistedPhoto() {
        boolean isChanged = DatasourceHelper.changeDescription(photoRepository, new Photo());
        assertFalse(isChanged);
    }

    @Test
    void changeDescription() {
        Folder folder = folderRepository.save(new Folder("test"));
        Photo photo = photoRepository.save(new Photo(1L, folder, "test", "description", new Date(1000L)));
        String newDescription = "new description";
        photo.setDescription(newDescription);
        boolean isChanged = DatasourceHelper.changeDescription(photoRepository, photo);
        assertTrue(isChanged);

        Optional<Photo> photoWithNewDescription = photoRepository.findByIdentifier(photo.getIdentifier());
        assertTrue(photoWithNewDescription.isPresent());
        assertEquals(newDescription, photoWithNewDescription.get().getDescription());
    }
}
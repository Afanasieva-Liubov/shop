package afanasievald.datasource;

import afanasievald.databaseEntity.Folder;
import afanasievald.databaseEntity.Photo;
import afanasievald.repository.DatasourceHelper;
import afanasievald.repository.FolderRepository;
import afanasievald.repository.PhotoRepository;
import org.hibernate.JDBCException;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringJUnitConfig
@DataJpaTest
class DatasourceHelperTest {

    @Autowired
    private PhotoRepository photoRepository;

    @Autowired
    private FolderRepository folderRepository;

    @Test /*Без папок вернуть пустой объект>*/
    void testGetFoldersWithPhoto_EmptyFolders() {
        Map<String, Long> folders = DatasourceHelper.getFoldersWithPhoto(folderRepository, photoRepository);

        assertNotNull(folders);
        assertTrue(folders.isEmpty());
    }

    /*Добавить виртуальные папки, папки вернуть отсортированные по дате*/
    @Test
    void testGetFoldersWithPhoto_SortedFolders() throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        folderRepository.save(new Folder("test2", simpleDateFormat.parse("2000-01-01")));
        Folder folder1 = folderRepository.save(new Folder("test1", simpleDateFormat.parse("1000-01-01")));

        Map<String, Long> foundFolders = DatasourceHelper.getFoldersWithPhoto(folderRepository, photoRepository);

        assertNotNull(foundFolders);
        assertEquals(2, foundFolders.size());

        Iterator<String> keys = foundFolders.keySet().iterator();
        assertEquals(folder1.getName(), keys.next());
    }

    /*Добавить виртуальные папки без фото, вернуть папки с пустыми фото*/
    @Test
    void testGetFoldersWithPhoto_EmptyPhotos() {
        Folder folder = folderRepository.save(new Folder("test"));

        Map<String, Long> foundFolders = DatasourceHelper.getFoldersWithPhoto(folderRepository, photoRepository);

        assertNotNull(foundFolders);
        assertEquals(1, foundFolders.size());

        assertNull(foundFolders.get(folder.getName()));
    }

    /*Добавить 1 виртуальную папку, вирт фото вернуть c наименьшей датой*/
    @Test
    void testGetFoldersWithPhoto_SortedPhotos() throws ParseException {
        Folder folder = folderRepository.save(new Folder("test"));
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        photoRepository.save(new Photo(2L, folder, "test", "description", simpleDateFormat.parse("2000-01-01")));
        Photo photo1 = photoRepository.save(new Photo(1L, folder, "test", "description", simpleDateFormat.parse("1000-01-01")));

        Map<String, Long> foundFolders = DatasourceHelper.getFoldersWithPhoto(folderRepository, photoRepository);

        assertNotNull(foundFolders);
        assertEquals(1, foundFolders.size());

        Iterator<Long> values = foundFolders.values().iterator();
        assertEquals(photo1.getIdentifier(), values.next());
    }

    @Test
    void testGetPhotosFromFolder_NotExistingFolder() {
        folderRepository.save(new Folder("test1"));
        folderRepository.save(new Folder("test2"));
        List<Photo> photo = DatasourceHelper.getPhotosFromFolder(folderRepository, photoRepository, "not existing folder");
        assertNull(photo);
    }

    @Test
    void testGetPhotosFromFolder_EmptyFolder() {
        Folder folder = folderRepository.save(new Folder("test"));
        List<Photo> photos = DatasourceHelper.getPhotosFromFolder(folderRepository, photoRepository, folder.getName());
        assertNotNull(photos);
        assertTrue(photos.isEmpty());
    }

    @Test
    void testGetPhotosFromFolder_SortedPhotos() throws ParseException {
        Folder folder = folderRepository.save(new Folder("test"));
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Photo photo2 = photoRepository.save(new Photo(2L, folder, "test", "description", simpleDateFormat.parse("2000-01-01")));
        Photo photo1 = photoRepository.save(new Photo(1L, folder, "test", "description", simpleDateFormat.parse("1000-01-01")));

        List<Photo> photos = DatasourceHelper.getPhotosFromFolder(folderRepository, photoRepository, folder.getName());

        assertNotNull(photos);
        assertEquals(2, photos.size());
        assertEquals(photo1.getIdentifier(), photos.get(0).getIdentifier());
        assertEquals(photo2.getIdentifier(), photos.get(1).getIdentifier());
    }

    @Test
    void testSavePhotoToFolder_NotExistedFolder() {
        boolean isSaved = DatasourceHelper.savePhotoToFolder(folderRepository, photoRepository, "not existed folder", new Photo());
        assertFalse(isSaved);
    }

    @Test
    void testSavePhotoToFolder_DuplicatePhotoIdentifier() {
        Folder folder = folderRepository.save(new Folder("test"));
        photoRepository.save(new Photo(1L, folder, "filename1", "description"));
        Photo photo2 = new Photo(1L, folder, "filename1", "description");

        boolean isSaved = DatasourceHelper.savePhotoToFolder(folderRepository, photoRepository, folder.getName(), photo2);
        assertFalse(isSaved);
    }

    @Test
    void testSavePhotoToFolder_JDBCException() {
        Folder folder = folderRepository.save(new Folder("test"));
        Photo photo = new Photo();
        photo.setIdentifier(1L);
        photo.setName("filename");

        PhotoRepository fakePhotoRepository = new PhotoRepository() {
            @Override
            public List<Photo> findByFolder(@NotNull Folder folder) {
                return null;
            }

            @Override
            public Optional<Photo> findByIdentifier(long identifier) {
                return Optional.empty();
            }

            @Override
            public <S extends Photo> S save(S entity) {
                throw new JDBCException("JDBCException", new SQLException());
            }

            @Override
            public <S extends Photo> Iterable<S> saveAll(Iterable<S> entities) {
                return null;
            }

            @Override
            public Optional<Photo> findById(Integer integer) {
                return Optional.empty();
            }

            @Override
            public boolean existsById(Integer integer) {
                return false;
            }

            @Override
            public Iterable<Photo> findAll() {
                return null;
            }

            @Override
            public Iterable<Photo> findAllById(Iterable<Integer> integers) {
                return null;
            }

            @Override
            public long count() {
                return 0;
            }

            @Override
            public void deleteById(Integer integer) {

            }

            @Override
            public void delete(Photo entity) {

            }

            @Override
            public void deleteAll(Iterable<? extends Photo> entities) {

            }

            @Override
            public void deleteAll() {

            }
        };

        boolean isSaved = DatasourceHelper.savePhotoToFolder(folderRepository, fakePhotoRepository, folder.getName(), photo);
        assertFalse(isSaved);
    }

    @Test
    void testSavePhotoToFolder() {
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
    void testChangeDescription_NotExistedPhoto() {
        boolean isChanged = DatasourceHelper.changeDescription(photoRepository, new Photo());
        assertFalse(isChanged);
    }

    @Test
    void testChangeDescription_JDBCException() {
        Photo photo = new Photo(1L, new Folder("test"), "test", "description");
        PhotoRepository fakePhotoRepository = new PhotoRepository() {
            @Override
            public List<Photo> findByFolder(@NotNull Folder folder) {
                return null;
            }

            @Override
            public Optional<Photo> findByIdentifier(long identifier) {
                return Optional.of(photo);
            }

            @Override
            public <S extends Photo> S save(S entity) {
                throw new JDBCException("JDBCException", new SQLException());
            }

            @Override
            public <S extends Photo> Iterable<S> saveAll(Iterable<S> entities) {
                return null;
            }

            @Override
            public Optional<Photo> findById(Integer integer) {
                return Optional.empty();
            }

            @Override
            public boolean existsById(Integer integer) {
                return false;
            }

            @Override
            public Iterable<Photo> findAll() {
                return null;
            }

            @Override
            public Iterable<Photo> findAllById(Iterable<Integer> integers) {
                return null;
            }

            @Override
            public long count() {
                return 0;
            }

            @Override
            public void deleteById(Integer integer) {

            }

            @Override
            public void delete(Photo entity) {

            }

            @Override
            public void deleteAll(Iterable<? extends Photo> entities) {

            }

            @Override
            public void deleteAll() {

            }
        };
        String newDescription = "new description";
        photo.setDescription(newDescription);
        boolean isChanged = DatasourceHelper.changeDescription(fakePhotoRepository, photo);
        assertFalse(isChanged);
    }

    @Test
    void testChangeDescription() {
        Folder folder = folderRepository.save(new Folder("test"));
        Photo photo = photoRepository.save(new Photo(1L, folder, "test", "description"));
        String newDescription = "new description";
        photo.setDescription(newDescription);
        boolean isChanged = DatasourceHelper.changeDescription(photoRepository, photo);
        assertTrue(isChanged);

        Optional<Photo> photoWithNewDescription = photoRepository.findByIdentifier(photo.getIdentifier());
        assertTrue(photoWithNewDescription.isPresent());
        assertEquals(newDescription, photoWithNewDescription.get().getDescription());
    }
}
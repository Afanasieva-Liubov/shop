package afanasievald.repository;

import afanasievald.databaseEntity.Folder;
import afanasievald.databaseEntity.Photo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.JDBCException;
import org.jetbrains.annotations.NotNull;
import java.util.*;

public class DatasourceHelper {
    @NotNull
    private static final Logger LOGGER = LogManager.getLogger(DatasourceHelper.class.getName());

    private DatasourceHelper() {
    }

    /**
     * @param folderRepository
     * @param photoRepository
     * @return Map<String, Long>, where String is Name of Folder, Long is Identifier of Photo with min CreatedDate in this Folder.
     * Map is sorted by CreatedDate of Folder.
     */
    public static Map<String, Long> getFoldersWithPhoto(@NotNull FolderRepository folderRepository,
                                                        @NotNull PhotoRepository photoRepository) {
        Iterable<Folder> folders = folderRepository.findByOrderByCreatedDateAsc();
        Map<String, Long> foldersWithOnePhoto = new LinkedHashMap<>();
        for (Folder folder : folders) {
            List<Photo> photos = photoRepository.findByFolder(folder);
            if (!photos.isEmpty()) {
                photos.sort(Comparator.comparing(Photo::getCreatedDate));
                foldersWithOnePhoto.put(folder.getName(), photos.get(0).getIdentifier());
            } else {
                foldersWithOnePhoto.put(folder.getName(), null);
            }
        }
        return foldersWithOnePhoto;
    }

    /**
     * @param folderRepository
     * @param photoRepository
     * @param folderName
     * @return photos sorted by CreatedDate
     */
    public static List<Photo> getPhotosFromFolder(@NotNull FolderRepository folderRepository,
                                                  @NotNull PhotoRepository photoRepository,
                                                  @NotNull String folderName) {
        Optional<Folder> folder = folderRepository.findByName(folderName);
        if (!folder.isPresent()) {
            LOGGER.info(String.format("Folder %s doesn't exist in DB", folderName));
            return null;
        }

        List<Photo> sortedPhotos = photoRepository.findByFolder(folder.get());
        if (sortedPhotos.isEmpty()) {
            return sortedPhotos;
        }

        sortedPhotos.sort(Comparator.comparing(Photo::getCreatedDate));

        return sortedPhotos;
    }


    public static boolean savePhotoToFolder(@NotNull FolderRepository folderRepository,
                                            @NotNull PhotoRepository photoRepository,
                                            @NotNull String folderName,
                                            @NotNull Photo photo) {
        Optional<Folder> folder = folderRepository.findByName(folderName);
        if (!folder.isPresent()) {
            LOGGER.info(String.format("Folder %s doesn't exist", folderName));
            return false;
        }

        Optional<Photo> optPhoto = photoRepository.findByIdentifier(photo.getIdentifier());
        if (optPhoto.isPresent()) {
            LOGGER.info(String.format("Photo with identifier %d exists", photo.getIdentifier()));
            return false;
        }

        photo.setFolder(folder.get());
        try {
            photoRepository.save(photo);
            return true;
        } catch(JDBCException e){
            LOGGER.error(e.getMessage(), e);
            return false;
        }
    }

    public static boolean changeDescription(@NotNull PhotoRepository photoRepository,
                                            @NotNull Photo photo) {
        Optional<Photo> photoOptional = photoRepository.findByIdentifier(photo.getIdentifier());
        if (!photoOptional.isPresent()) {
            LOGGER.info(String.format("Photo with identifier %d doesn't exist", photo.getIdentifier()));
            return false;
        }

        Photo realPhoto = photoOptional.get();
        realPhoto.setDescription(photo.getDescription());
        try {
            photoRepository.save(realPhoto);
            return true;
        } catch(JDBCException e){
            LOGGER.error(e.getMessage(), e);
            return false;
        }
    }
}

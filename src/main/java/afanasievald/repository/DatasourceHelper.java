package afanasievald.repository;

import afanasievald.databaseEntity.Folder;
import afanasievald.databaseEntity.Photo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class DatasourceHelper {
    @NotNull
    private static final Logger logger = LogManager.getLogger(DatasourceHelper.class.getName());

    private DatasourceHelper() {
    }

    public static Map<String, Long> getFoldersWithPhotoIdentifier(@NotNull FolderRepository folderRepository,
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

    public static List<Photo> getPhotosFromFolder(@NotNull FolderRepository folderRepository,
                                                  @NotNull PhotoRepository photoRepository,
                                                  @NotNull String folderName) {
        Optional<Folder> folder = folderRepository.findByName(folderName);
        if (!folder.isPresent()) {
            String exceptionString = String.format("Folder %s doesn't exist in DB", folderName);
            logger.error(exceptionString);
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
            String exceptionString = String.format("Folder %s doesn't exist", folderName);
            logger.error(exceptionString);
            return false;
        }

        Optional<Photo> optPhoto = photoRepository.findByIdentifier(photo.getIdentifier());
        if (optPhoto.isPresent()) {
            String exceptionString = String.format("Photo with identifier %d exists", photo.getIdentifier());
            logger.error(exceptionString);
            return false;
        }

        photo.setFolder(folder.get());
        photoRepository.save(photo);
        return true;
    }

    public static boolean changeDescription(@NotNull PhotoRepository photoRepository,
                                            @NotNull Photo photo) {
        Optional<Photo> photoOptional = photoRepository.findByIdentifier(photo.getIdentifier());
        if (!photoOptional.isPresent()) {
            String exceptionString = String.format("Photo with identifier %d doesn't exist", photo.getIdentifier());
            logger.error(exceptionString);
            return false;
        }

        Photo realPhoto = photoOptional.get();
        realPhoto.setDescription(photo.getDescription());
        photoRepository.save(realPhoto);
        return true;
    }
}

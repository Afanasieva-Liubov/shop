package afanasievald.repository;

import afanasievald.databaseEntity.Folder;
import afanasievald.databaseEntity.Photo;

import java.util.*;

public class DatasourceHelper {
    public DatasourceHelper() {
    }

    public static Map<String, Long> getFoldersWithPhotoIdentifier(FolderRepository folderRepository,
                                                                            PhotoRepository photoRepository) {
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

    public static List<Photo> getPhotosFromFolder(FolderRepository folderRepository,
                                                  PhotoRepository photoRepository,
                                                  String folderName) {
        Optional<Folder> folder = folderRepository.findByName(folderName);
        if (!folder.isPresent()) {
            return null;
        }

        List<Photo> sortedPhotos = photoRepository.findByFolder(folder.get());
        if (sortedPhotos.isEmpty()) {
            return null;
        }

        sortedPhotos.sort(Comparator.comparing(Photo::getCreatedDate));

        return sortedPhotos;
    }


    public static boolean savePhotoToFolder(FolderRepository folderRepository,
                                            PhotoRepository photoRepository,
                                            String folderName,
                                            Photo photo) throws Exception {
        Optional<Folder> folder = folderRepository.findByName(folderName);
        if (!folder.isPresent()) {
            throw new Exception(String.format("Folder %s doesn't exist", folderName));
        }

        if (photo == null) {
            throw new Exception("Photo is null");
        }

        Optional<Photo> optPhoto = photoRepository.findByIdentifier(photo.getIdentifier());
        if (optPhoto.isPresent()) {
            throw new Exception(String.format("Photo with identifier %d exists", photo.getIdentifier()));
        }

        photo.setFolder(folder.get());
        photoRepository.save(photo);
        return true;
    }

    public static void changeDescription(PhotoRepository photoRepository,
                                         long identifier,
                                         String newDescription) throws Exception {
        Optional<Photo> photoOptional = photoRepository.findByIdentifier(identifier);

        if (!photoOptional.isPresent()) {
            throw new Exception(String.format("Photo with identifier %d doesn't exist", identifier));
        }

        Photo photo = photoOptional.get();
        photo.setDescription(newDescription);
        photoRepository.save(photo);
    }
}

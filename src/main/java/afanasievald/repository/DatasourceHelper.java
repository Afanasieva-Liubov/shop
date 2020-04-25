package afanasievald.repository;

import afanasievald.databaseEntity.Folder;
import afanasievald.databaseEntity.Photo;

import java.util.*;

public class DatasourceHelper {
    public DatasourceHelper(){
    }


    public static LinkedHashMap<String, Integer> getFoldersWithPhotoIdentifier(FolderRepository folderRepository,
                                                                               PhotoRepository photoRepository){
        Iterable<Folder> folders = folderRepository.findByOrderByCreatedDateAsc();
        LinkedHashMap<String, Integer> foldersWithOnePhoto = new LinkedHashMap<>();
        for(Folder folder: folders){
             List<Photo> photos = photoRepository.findByFolder(folder);
            if (!photos.isEmpty()){
                photos.sort(Comparator.comparing(Photo::getCreatedDate));
                foldersWithOnePhoto.put(folder.getName(), photos.get(0).getIdentifier());
            } else{
                foldersWithOnePhoto.put(folder.getName(), null);
            }
        }
        return foldersWithOnePhoto;
    }

    public static List<Photo> getPhotosFromFolder(FolderRepository folderRepository,
                                                          PhotoRepository photoRepository,
                                                          String foldername){

        List<Photo> sortedPhotos = new ArrayList<>();
        Optional<Folder> folder = folderRepository.findByName(foldername);
        if (folder.isPresent()) {
            sortedPhotos = photoRepository.findByFolder(folder.get());
            sortedPhotos.sort(Comparator.comparing(Photo::getCreatedDate));
        }
        return sortedPhotos;
    }


    public static void savePhotoToFolder(FolderRepository folderRepository,
                                  PhotoRepository photoRepository,
                                  int identifier,
                                  String foldername,
                                  String filename) throws Exception{
        Optional<Folder> folder = folderRepository.findByName(foldername);
        if (!folder.isPresent()) {
            throw new Exception(String.format("Folder %s doesn't exist", foldername));
        }

        Optional<Photo> optPhoto = photoRepository.findByIdentifier(identifier);
        if (optPhoto.isPresent()){
            throw new Exception(String.format("Photo with identifier %d exists", identifier));
        }

        Photo photo = new Photo(identifier, folder.get(), filename, null);
        photoRepository.save(photo);
    }

    public static void changeDescription(PhotoRepository photoRepository,
                                    int identifier,
                                    String newDescription) throws Exception{
        Optional<Photo> photoOptional= photoRepository.findByIdentifier(identifier);

        if (!photoOptional.isPresent()) {
            throw new Exception(String.format("Photo with identifier %d doesn't exist", identifier));
        }

        Photo photo = photoOptional.get();
        photo.setDescription(newDescription);
        photoRepository.save(photo);
    }
}

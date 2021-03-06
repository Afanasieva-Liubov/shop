package afanasievald.repository;

import afanasievald.databaseEntity.Folder;
import afanasievald.databaseEntity.Photo;
import afanasievald.repository.FolderRepository;
import afanasievald.repository.PhotoRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

public class DatasourceHelper {
    public DatasourceHelper(){
    }


    public static LinkedHashMap<String, Integer> getFoldersWithPhotoHashcode(FolderRepository folderRepository,
                                                                PhotoRepository photoRepository){
        Iterable<Folder> folders = folderRepository.findByOrderByCreatedDateAsc();
        LinkedHashMap<String, Integer> foldersWithOnePhoto = new LinkedHashMap<>();
        for(Folder folder: folders){
             List<Photo> photos = photoRepository.findByFolder(folder);
            if (!photos.isEmpty()){
                photos.sort((x1,x2) -> (x1.getCreatedDate().compareTo(x2.getCreatedDate())));
                foldersWithOnePhoto.put(folder.getName(), photos.get(0).getHashcode());
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
            sortedPhotos.sort((x1,x2) -> (x1.getCreatedDate().compareTo(x2.getCreatedDate())));
        }
        return sortedPhotos;
    }


    public static void savePhotoToFolder(FolderRepository folderRepository,
                                  PhotoRepository photoRepository,
                                  int hashcode,
                                  String foldername,
                                  String filename) throws Exception{
        Optional<Folder> folder = folderRepository.findByName(foldername);
        if (!folder.isPresent()) {
            throw new Exception(String.format("Folder %s doesn't exist", foldername));
        }

        Optional<Photo> optPhoto = photoRepository.findByHashcode(hashcode);
        if (optPhoto.isPresent()){
            throw new Exception(String.format("Photo with hashcode %d exists", hashcode));
        }

        Photo photo = new Photo(hashcode, folder.get(), filename, null);
        photoRepository.save(photo);
    }

    public static void changeDescription(PhotoRepository photoRepository,
                                    int hashcode,
                                    String newDescription) throws Exception{
        Optional<Photo> photoOptional= photoRepository.findByHashcode(hashcode);

        if (!photoOptional.isPresent()) {
            throw new Exception(String.format("Photo with hashcode %d doesn't exist", hashcode));
        }

        Photo photo = photoOptional.get();
        photo.setDescription(newDescription);
        photoRepository.save(photo);
    }
}

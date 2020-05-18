package afanasievald.controller;

import afanasievald.databaseEntity.Photo;
import afanasievald.repository.DatasourceHelper;
import afanasievald.repository.FolderRepository;
import afanasievald.repository.PhotoRepository;
import afanasievald.uploadingPhoto.StorageService;
import org.jetbrains.annotations.NotNull;

public class WorkControllerHelper {

    private WorkControllerHelper() {
    }

    public static boolean uploadOnePhoto(@NotNull StorageService storageService,
                                         @NotNull FolderRepository folderRepository,
                                         @NotNull PhotoRepository photoRepository,
                                         @NotNull String folderName,
                                         @NotNull String fileName,
                                         @NotNull byte[] bytesArray) {
        Photo photo = storageService.uploadPhotos(fileName, bytesArray);
        if (photo == null) {
            return false;
        }

        boolean isSavedToDB = DatasourceHelper.savePhotoToFolder(folderRepository,
                photoRepository,
                folderName,
                photo);

        boolean isOk = true;
        if (!isSavedToDB) {
            storageService.deletePhoto(photo);
            isOk = false;
        }

        return isOk;
    }
}

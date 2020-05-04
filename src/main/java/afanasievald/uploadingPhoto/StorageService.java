package afanasievald.uploadingPhoto;

import afanasievald.databaseEntity.Photo;
import afanasievald.repository.PhotoRepository;
import org.jetbrains.annotations.NotNull;

public interface StorageService {
    Photo uploadPhotos(@NotNull String fileName, @NotNull byte[] byteArray);

    boolean deletePhoto(@NotNull Photo photo);

    byte[] loadPhotoAsResource(@NotNull PhotoRepository photoRepository, long identifier);
}

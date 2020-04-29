package afanasievald.uploadingPhoto;

import afanasievald.databaseEntity.Photo;

import java.io.IOException;

public interface StorageService {
    Photo uploadPhotos(String fileName,
                       byte[] byteArray) throws Exception;

    void deletePhoto(Photo photo) throws IOException;

    byte[] loadPhotoAsResource(String fileName) throws Exception;
}

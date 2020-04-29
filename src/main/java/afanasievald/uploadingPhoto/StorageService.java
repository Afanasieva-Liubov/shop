package afanasievald.uploadingPhoto;

import afanasievald.databaseEntity.Photo;

public interface StorageService {
    Photo uploadPhotos(String fileName,
                       byte[] byteArray) throws Exception;

    byte[] loadPhotoAsResource(String fileName) throws Exception;
}

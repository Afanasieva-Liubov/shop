package afanasievald.uploadingPhoto;

public interface StorageService {
    String uploadPhotos(String folderName,
                        String fileName,
                        byte[] byteArray) throws Exception;

    byte[] loadPhotoAsResource(String folderName,
                               String fileName) throws Exception;
}

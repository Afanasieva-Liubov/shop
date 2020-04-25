package afanasievald.uploadingPhoto;

import afanasievald.repository.FolderRepository;
import afanasievald.repository.PhotoRepository;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    void uploadPhotos(FolderRepository folderRepository,
                      PhotoRepository photoRepository,
                      String foldername,
                      MultipartFile[] files) throws Exception;

    Resource loadPhotoAsResource(PhotoRepository photoRepository,
                                 String foldername,
                                 int identifier);

}

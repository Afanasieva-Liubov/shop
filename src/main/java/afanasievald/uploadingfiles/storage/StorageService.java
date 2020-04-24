package afanasievald.uploadingfiles.storage;

import afanasievald.repository.DatasourceHelper;
import afanasievald.repository.FolderRepository;
import afanasievald.repository.PhotoRepository;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Path;
import java.util.stream.Stream;

public interface StorageService {
    Stream<String> findAllFolders(Path location);

    Stream<String> findAllPhotos(Path location);

    StringBuilder uploadPhotos(FolderRepository folderRepository,
                               PhotoRepository photoRepository,
                               String foldername,
                               MultipartFile[] files) throws Exception;

    Resource loadPhotoAsResource(PhotoRepository photoRepository,
                                 String foldername,
                                 Integer hashcode);

    void init();
}

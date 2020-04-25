package afanasievald.uploadingPhoto;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

import afanasievald.databaseEntity.Photo;
import afanasievald.repository.DatasourceHelper;
import afanasievald.uploadingPhoto.image.ImageRotation;
import afanasievald.repository.FolderRepository;
import afanasievald.repository.PhotoRepository;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.MetadataException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class PhotoSystemStorageService implements StorageService {

    private final Path photoLocationPath;
    private final String photoLocation;

    @Autowired
    public PhotoSystemStorageService(StorageProperties properties) {
        this.photoLocation = properties.getPhotoLocation();
        this.photoLocationPath = Paths.get(this.photoLocation);
    }

    @Override
    public void uploadPhotos(FolderRepository folderRepository,
                                      PhotoRepository photoRepository,
                                      String foldername,
                                      MultipartFile[] files)throws Exception {

        for (MultipartFile file : files) {
            try {
                Path fileNameAndPath = Paths.get(String.format("%s/%s", photoLocation, foldername), file.getOriginalFilename());
                String mimeType = Files.probeContentType(fileNameAndPath);
                if (mimeType.startsWith("image/")) {
                    byte[] content = file.getBytes();
                    String newFileName = String.format("%s.%s", Arrays.hashCode(content), Objects.requireNonNull(file.getOriginalFilename()).split("\\.")[1]);
                    Path newFileNameAndPath = Paths.get(String.format("%s/%s", photoLocation, foldername),
                            newFileName);
                    Files.write(newFileNameAndPath, content);
                    DatasourceHelper.savePhotoToFolder(folderRepository,
                            photoRepository,
                            Arrays.hashCode(content),
                            foldername,
                            newFileName);
                    ImageRotation imageRotation = new ImageRotation();
                    imageRotation.rotateImage(newFileNameAndPath.toString());
                }

            } catch (IOException | MetadataException | ImageProcessingException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public Resource loadPhotoAsResource(PhotoRepository photoRepository,
                                        String foldername,
                                        int identifier) {

        if (foldername == null || foldername.isEmpty()) {
            return null;
        }

        Optional<Photo> photo = photoRepository.findByIdentifier(identifier);
        if (!photo.isPresent()) {
            return null;
        }

        String filename = String.format("%s/%s", foldername, photo.get().getName());
        Resource fileResource;
        try {
            Path file = photoLocationPath.resolve(filename);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                fileResource = resource;
            }
            else {
                throw new StorageFileNotFoundException(
                        "Could not read file: " + filename);
            }
        }
        catch (MalformedURLException e) {
            throw new StorageFileNotFoundException("Could not read file: " + filename, e);
        }
        return fileResource;
    }
}

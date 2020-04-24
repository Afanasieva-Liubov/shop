package afanasievald.uploadingPhoto;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.stream.Stream;

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
    private final String photoDisc;
    private final String photoLocation;

    @Autowired
    public PhotoSystemStorageService(StorageProperties properties) {
        this.photoLocation = properties.getPhotoLocation();
        this.photoLocationPath = Paths.get(this.photoLocation);
        this.photoDisc = properties.getPhotoDisc();
    }

    @Override
    public Stream<String> findAllFolders(Path location) {
        if (location == null ||
           !Files.exists(location)) {
            return null;
        }

        Stream<String> folders = null;
        try {
            folders = Files.walk(location, 1)
                    .filter(f->!Files.isRegularFile(f))
                    .filter(path -> !path.equals(location))
                    .map(location::relativize)
                    .map(path -> new StringBuilder().append(path.toString()).toString());
        } catch (IOException e) {
            throw new StorageException("Failed to read stored folders", e);
        }
        finally {
            return folders;
        }
    }

    @Override
    public Stream<String> findAllPhotos(Path location) {
        if (location == null ||
            !Files.exists(location)) {
            return null;
        }

        Stream<String> paths = null;
        try {
            paths = Files.walk(location, 1)
                    .filter(Files::isRegularFile)
                    .filter(path -> !path.equals(location))
                    .map(location::relativize)
                    .map(path -> new StringBuilder().append(path.toString()).toString());
        } catch (IOException e) {
            throw new StorageException("Failed to read stored files", e);
        }
        finally {
            return paths;
        }
    }

    @Override
    public StringBuilder uploadPhotos(FolderRepository folderRepository,
                                      PhotoRepository photoRepository,
                                      String foldername,
                                      MultipartFile[] files)throws Exception {
        StringBuilder fileNames = new StringBuilder();
        for (MultipartFile file : files) {
            try {
                Path fileNameAndPath = Paths.get(String.format("%s/%s", photoLocation, foldername), file.getOriginalFilename());
                String mimeType = Files.probeContentType(fileNameAndPath);
                if (mimeType.startsWith("image/")) {
                    fileNames.append("Photo ")
                            .append(file.getOriginalFilename())
                            .append(" is uploaded ")
                            .append(System.getProperty("line.separator"));
                    byte[] content = file.getBytes();
                    String newFileName = String.format("%s.%s", content.hashCode(), file.getOriginalFilename().split("\\.")[1]);
                    Path newFileNameAndPath = Paths.get(String.format("%s/%s", photoLocation, foldername),
                            newFileName);
                    Files.write(newFileNameAndPath, content);
                    DatasourceHelper.savePhotoToFolder(folderRepository,
                            photoRepository,
                            content.hashCode(),
                            foldername,
                            newFileName);
                    ImageRotation imageRotation = new ImageRotation();
                    imageRotation.rotateImage(newFileNameAndPath.toString());
                } else {
                    fileNames.append("Photo ")
                            .append(file.getOriginalFilename())
                            .append(" is not uploaded")
                            .append(System.getProperty("line.separator"));
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            catch (MetadataException e) {
            e.printStackTrace();
            }
            catch (ImageProcessingException e) {
                e.printStackTrace();
            }
        }
        return fileNames;
    }

    @Override
    public Resource loadPhotoAsResource(PhotoRepository photoRepository,
                                        String foldername,
                                        Integer hashcode) {
        Optional<Photo> photo = photoRepository.findByHashcode(hashcode);
        if (foldername == null ||
                foldername.compareTo("") == 1 ||
                !photo.isPresent()) {
            return null;
        }

        String filename = String.format("%s/%s", foldername, photo.get().getName());
        Resource fileResource = null;
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
        finally {
            return fileResource;
        }
    }
}

package afanasievald.uploadingPhoto;

import java.io.IOException;
import java.nio.file.*;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import afanasievald.databaseEntity.Photo;
import afanasievald.repository.PhotoRepository;
import afanasievald.uploadingPhoto.image.ImageRotation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PhotoStorageService implements StorageService {
    @NotNull
    private final String photoLocation;

    @NotNull
    private final Logger logger = LogManager.getLogger(PhotoStorageService.class.getName());

    @Autowired
    public PhotoStorageService(StorageProperties properties) throws IOException {
        this.photoLocation = properties.getPhotoLocation();
        Path photoLocationPath = Paths.get(this.photoLocation);
        if (!Files.exists(photoLocationPath)) {
            Files.createDirectories(photoLocationPath);
        } else {
            if (!Files.isDirectory(photoLocationPath)) {
                String exceptionString = String.format("Application is closed, because photoLocationPath %s isn't directory", this.photoLocation);
                logger.error(exceptionString);
                throw new IllegalArgumentException(exceptionString);
            }
        }
    }

    @Override
    public Photo uploadPhotos(@NotNull String fileName, @NotNull byte[] byteArray){
        Path fileNameAndPath = Paths.get(photoLocation, fileName);
        Photo photo = new Photo();
        try {
            String mimeType = Files.probeContentType(fileNameAndPath);
            if (!mimeType.startsWith("image/")) {
                String exceptionString = String.format("File %s isn't image", fileNameAndPath);
                logger.error(exceptionString);
                return null;
            }

            byte[] normalizedByteArray = ImageRotation.normalizeOrientation(byteArray);

            photo.setIdentifier(Arrays.hashCode(byteArray) + (new Date()).hashCode());
            String fileExtension = Objects.requireNonNull(fileName).split("\\.")[1];
            photo.setName(String.format("%s.%s", photo.getIdentifier(), fileExtension));

            Path newFileNameAndPath = Paths.get(photoLocation, photo.getName());
            Files.write(newFileNameAndPath, normalizedByteArray);
        } catch (IOException e) {
            logger.error(e.getMessage());
            return null;
        }
        return photo;
    }

    @Override
    public boolean deletePhoto(@NotNull Photo photo){
        Path fileNameAndPath = Paths.get(photoLocation, photo.getName());
        if (!Files.exists(fileNameAndPath)) {
            String exceptionString = String.format("Photo %s doesn't exist", fileNameAndPath.toString());
            logger.error(exceptionString);
            return false;
        }
        try {
            Files.delete(fileNameAndPath);
        } catch (IOException e) {
            logger.error(e.getMessage());
            return false;
        }

        return true;
    }

    @Override
    public byte[] loadPhotoAsResource(@NotNull PhotoRepository photoRepository, long identifier){
        Optional<Photo> photo = photoRepository.findByIdentifier(identifier);
        if (!photo.isPresent()) {
            String exceptionString = String.format("Photo with identifier %d doesn't exist", identifier);
            logger.error(exceptionString);
            return null;
        }

        String fileName = photo.get().getName();
        if (fileName.isEmpty()) {
            String exceptionString = String.format("Photo %s is empty", fileName);
            logger.error(exceptionString);
            return null;
        }

        try {
            Path filePath = Paths.get(photoLocation, fileName);
            if (!Files.exists(filePath)) {
                String exceptionString = String.format("File %s doesn't exist", filePath);
                logger.error(exceptionString);
                return null;
            }

            if (!Files.isReadable(filePath)) {
                String exceptionString = String.format("File %s isn't readable", filePath);
                logger.error(exceptionString);
                return null;
            }

            return Files.readAllBytes(filePath);
        } catch (InvalidPathException | IOException e) {
            logger.error(e.getMessage());
            return null;
        }
    }
}

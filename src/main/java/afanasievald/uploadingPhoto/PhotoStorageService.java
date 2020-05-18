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
    private final Logger LOGGER = LogManager.getLogger(PhotoStorageService.class.getName());

    @Autowired
    public PhotoStorageService(StorageProperties properties) throws IOException {
        this.photoLocation = properties.getPhotoLocation();
        Path photoLocationPath = Paths.get(this.photoLocation);
        if (!Files.exists(photoLocationPath)) {
            Files.createDirectories(photoLocationPath);
        } else {
            if (!Files.isDirectory(photoLocationPath)) {
                LOGGER.error(String.format("Application is closed, because photoLocationPath %s isn't directory", this.photoLocation));
                throw new IllegalArgumentException(String.format("Application is closed, because photoLocationPath %s isn't directory", this.photoLocation));
            }
        }
    }

    @Override
    public Photo uploadPhotos(@NotNull String fileName, @NotNull byte[] byteArray){
        Path fileNameAndPath = Paths.get(photoLocation, fileName);
        Photo photo = new Photo();
        try {
            String mimeType = Files.probeContentType(fileNameAndPath);
            if (mimeType == null || !mimeType.startsWith("image/")) {
                LOGGER.info(String.format("File %s isn't image", fileNameAndPath));
                return null;
            }

            byte[] normalizedByteArray = ImageRotation.normalizeOrientation(byteArray);
            photo.setIdentifier(Arrays.hashCode(byteArray) + (new Date()).hashCode());
            String fileExtension = Objects.requireNonNull(fileName).split("\\.")[1];
            photo.setName(String.format("%s.%s", photo.getIdentifier(), fileExtension));
            Path newFileNameAndPath = Paths.get(photoLocation, photo.getName());
            Files.write(newFileNameAndPath, normalizedByteArray);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            return null;
        }
        return photo;
    }

    @Override
    public boolean deletePhoto(@NotNull Photo photo){
        Path fileNameAndPath = Paths.get(photoLocation, photo.getName());
        if (!Files.exists(fileNameAndPath)) {
            LOGGER.info(String.format("Photo %s doesn't exist", fileNameAndPath.toString()));
            return false;
        }
        try {
            Files.delete(fileNameAndPath);

        } catch (IOException e) {
            LOGGER.error(String.format("Photo %s with identifier %d isn't deleted from disk",
                    fileNameAndPath.toString(), photo.getIdentifier()), e);
            return false;
        }

        return true;
    }

    @Override
    public byte[] loadPhotoAsResource(@NotNull PhotoRepository photoRepository, long identifier){
        Optional<Photo> photo = photoRepository.findByIdentifier(identifier);
        if (!photo.isPresent()) {
            LOGGER.info(String.format("Photo with identifier %d doesn't exist", identifier));
            return null;
        }

        String fileName = photo.get().getName();
        if (fileName.isEmpty()) {
            LOGGER.info(String.format("Photo %s is empty", fileName));
            return null;
        }

        try {
            Path filePath = Paths.get(photoLocation, fileName);
            if (!Files.exists(filePath)) {
                LOGGER.info(String.format("File %s doesn't exist", filePath));
                return null;
            }

            if (!Files.isReadable(filePath)) {
                LOGGER.info(String.format("File %s isn't readable", filePath));
                return null;
            }

            return Files.readAllBytes(filePath);
        } catch (InvalidPathException | IOException e) {
            LOGGER.error(e.getMessage(), e);
            return null;
        }
    }
}

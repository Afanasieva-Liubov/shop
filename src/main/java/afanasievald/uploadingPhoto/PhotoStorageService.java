package afanasievald.uploadingPhoto;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;

import afanasievald.databaseEntity.Photo;
import afanasievald.uploadingPhoto.image.ImageRotation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

@Service
public class PhotoStorageService implements StorageService {
    private final String photoLocation;

    @Autowired
    public PhotoStorageService(StorageProperties properties) throws Exception {
        this.photoLocation = properties.getPhotoLocation();
        Path photoLocationPath = Paths.get(this.photoLocation);
        try {
            if (!Files.exists(photoLocationPath)) {
                Files.createDirectories(photoLocationPath);
            } else {
                if (!Files.isDirectory(photoLocationPath, LinkOption.NOFOLLOW_LINKS)) {
                    throw new Exception(String.format("Application is closed, because photoLocationPath %s isn't directory", this.photoLocation.toString()));
                }
            }
        } catch (IOException e) {
            throw new Exception(String.format("Application is closed, because photoLocationPath %s isn't correct", this.photoLocation.toString()));
        }
    }

    @Override
    public Photo uploadPhotos(String fileName,
                              byte[] byteArray) throws Exception {
        Path fileNameAndPath = Paths.get(photoLocation, fileName);
        String mimeType = Files.probeContentType(fileNameAndPath);
        if (!mimeType.startsWith("image/")) {
            throw new Exception(String.format("File %s isn't image", fileNameAndPath));
        }

        byte[] normalizedByteArray = ImageRotation.normalizeOrientation(byteArray);

        Photo photo = new Photo();
        photo.setIdentifier(Arrays.hashCode(byteArray) + (new Date()).hashCode());
        photo.setName(String.format("%s.%s", photo.getIdentifier(), Objects.requireNonNull(fileName).split("\\.")[1]));

        Path newFileNameAndPath = Paths.get(photoLocation, photo.getName());
        Files.write(newFileNameAndPath, normalizedByteArray);
        return photo;
    }

    @Override
    public void deletePhoto(Photo photo) throws IOException {
        Path fileNameAndPath = Paths.get(photoLocation, photo.getName());
        Files.delete(fileNameAndPath);
    }

    @Override
    public byte[] loadPhotoAsResource(String fileName) throws MalformedURLException {
        if (fileName == null || fileName.isEmpty()) {
            return null;
        }

        try {
            Path file = Paths.get(photoLocation, fileName);
            Resource resource = new UrlResource(file.toUri());
            if (!resource.exists() && !resource.isReadable()) {
                throw new StorageFileNotFoundException(String.format("Could not read file: %s", fileName));
            }

            InputStream initialStream = resource.getInputStream();
            byte[] targetArray = new byte[initialStream.available()];
            if (initialStream.read(targetArray) <= 0) {
                return null;
            }
            return targetArray;
        } catch (InvalidPathException | MalformedURLException e) {
            throw e;
        } catch (
                IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}

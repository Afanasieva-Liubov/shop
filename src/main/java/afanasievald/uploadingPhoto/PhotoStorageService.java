package afanasievald.uploadingPhoto;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.Arrays;
import java.util.Objects;

import afanasievald.uploadingPhoto.image.ImageRotation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

@Service
public class PhotoStorageService implements StorageService {

    private final Path photoLocationPath;
    private final String photoLocation;

    @Autowired
    public PhotoStorageService(StorageProperties properties) throws Exception {
        this.photoLocation = properties.getPhotoLocation();
        this.photoLocationPath = Paths.get(this.photoLocation);
        try {
            if (!Files.exists(this.photoLocationPath)) {
                Files.createDirectories(this.photoLocationPath);
            } else {
                if (!Files.isDirectory(this.photoLocationPath, LinkOption.NOFOLLOW_LINKS)) {
                    throw new Exception(String.format("Application is closed, because photoLocationPath %s isn't directory", this.photoLocationPath.toString()));
                }
            }
        } catch (IOException e) {
            throw new Exception(String.format("Application is closed, because photoLocationPath %s isn't correct", this.photoLocationPath.toString()));
        }
    }

    @Override
    public String uploadPhotos(String folderName,
                               String fileName,
                               byte[] byteArray) throws Exception {
        Path fileNameAndPath = Paths.get(String.format("%s/%s", photoLocation, folderName), fileName);
        String mimeType = Files.probeContentType(fileNameAndPath);
        if (!mimeType.startsWith("image/")) {
            throw new Exception(String.format("File %s isn't image", fileNameAndPath));
        }

        byte[] normalizedByteArray = ImageRotation.normalizeOrientation(byteArray);

        String newFileName = String.format("%s.%s", Arrays.hashCode(byteArray), Objects.requireNonNull(fileName).split("\\.")[1]);
        Path newFileNameAndPath = Paths.get(String.format("%s/%s", photoLocation, folderName), newFileName);
        Files.write(newFileNameAndPath, normalizedByteArray);
        return newFileName;
    }

    @Override
    public byte[] loadPhotoAsResource(String folderName,
                                      String fileName) throws MalformedURLException {
        if (folderName == null || folderName.isEmpty()) {
            return null;
        }

        if (fileName == null || fileName.isEmpty()) {
            return null;
        }

        try {
            String filename = String.format("%s/%s", folderName, fileName);
            Path file = photoLocationPath.resolve(filename);
            Resource resource = new UrlResource(file.toUri());
            if (!resource.exists() && !resource.isReadable()) {
                throw new StorageFileNotFoundException(String.format("Could not read file: %s", filename));
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

package afanasievald.uploadingPhoto;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PhotoSystemStorageServiceTest {

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

    @Test
    void findAllFolders() {
    }

    @Test
    void findAllPhotos() {
    }

    @Test
    void uploadPhotos() {
    }

    @Test
    void loadPhotoAsResource() {
    }
}
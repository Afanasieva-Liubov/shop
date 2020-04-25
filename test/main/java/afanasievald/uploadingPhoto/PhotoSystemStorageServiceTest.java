package afanasievald.uploadingPhoto;

import afanasievald.databaseEntity.Photo;
import afanasievald.repository.PhotoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PhotoSystemStorageServiceTest {

    @Autowired
    PhotoSystemStorageService photoSystemStorageService;

    @Test
    void uploadPhotos() {
    }

    @Test
    void loadPhotoAsResource() {
    }
}
package afanasievald.uploadingPhoto;

import org.jetbrains.annotations.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ConfigurationProperties(prefix = "server")
@PropertySource("classpath:application.properties")
public class StorageProperties {

    /**
     * Folder location for storing files
     */
    @NotNull
    private String photoLocation;

    public StorageProperties() {
    }

    public StorageProperties(@NotNull String photoLocation) {
        this.photoLocation = photoLocation;
    }

    public @NotNull String getPhotoLocation() {
        return photoLocation;
    }

    public void setPhotoLocation(@NotNull String photoLocation) {
        this.photoLocation = photoLocation;
    }
}

package afanasievald.uploadingPhoto;

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
    private String photoLocation;
;
    private String photoDisc;

    public StorageProperties() {
    }

    public StorageProperties(String photoLocation, String photoDisc) {
        this.photoLocation = photoLocation;
        this.photoDisc = photoDisc;
    }

    public String getPhotoLocation() {
        return photoLocation;
    }

    public void setPhotoLocation(String photoLocation) {
        this.photoLocation = photoLocation;
    }

    public String getPhotoDisc() {
        return photoDisc;
    }

    public void setPhotoDisc(String photoDisc) {
        this.photoDisc = photoDisc;
    }
}

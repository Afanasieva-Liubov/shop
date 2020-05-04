/*https://stackoverflow.com/questions/5905868/how-to-rotate-jpeg-images-based-on-the-orientation-metadata*/

package afanasievald.uploadingPhoto.image;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.jpeg.JpegDirectory;
import javaxt.io.Image;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import java.io.ByteArrayInputStream;
import java.io.IOException;

public class ImageRotation {
    @NotNull
    private static final Logger logger = LogManager.getLogger(ImageRotation.class.getName());

    private static ImageInformation readImageInformation(@NotNull byte[] byteArray) {
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(new ByteArrayInputStream(byteArray));
            Directory directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
            JpegDirectory jpegDirectory = metadata.getFirstDirectoryOfType(JpegDirectory.class);

            if (directory == null) {
                String exceptionString = "Directory is null";
                logger.warn(exceptionString);
                return null;
            }

            int orientation = directory.getInt(ExifIFD0Directory.TAG_ORIENTATION);
            int width = jpegDirectory.getImageWidth();
            int height = jpegDirectory.getImageHeight();

            return new ImageInformation(orientation, width, height);
        } catch (MetadataException | ImageProcessingException | IOException e) {
            logger.error(e.getMessage());
            return null;
        }
    }

    public static byte[] normalizeOrientation(@NotNull byte[] byteArray) {
        Image image = new Image(byteArray);
        ImageInformation imageInformation = readImageInformation(byteArray);
        if (imageInformation != null) {
            rotate(imageInformation, image);
        }
        image.setOutputQuality(99);
        return image.getByteArray();
    }

    private static void rotate(@NotNull ImageInformation info, @NotNull Image image) {
        switch (info.orientation) {
            case 1:
                return;
            case 2:
                image.flip();
                break;
            case 3:
                image.rotate(180.0D);
                break;
            case 4:
                image.flip();
                image.rotate(180.0D);
                break;
            case 5:
                image.flip();
                image.rotate(270.0D);
                break;
            case 6:
                image.rotate(90.0D);
                break;
            case 7:
                image.flip();
                image.rotate(90.0D);
                break;
            case 8:
                image.rotate(270.0D);
        }
    }
}

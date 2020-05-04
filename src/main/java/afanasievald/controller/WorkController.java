package afanasievald.controller;

import afanasievald.databaseEntity.Photo;
import afanasievald.repository.DatasourceHelper;
import afanasievald.repository.FolderRepository;
import afanasievald.repository.PhotoRepository;
import afanasievald.uploadingPhoto.PhotoStorageService;
import afanasievald.uploadingPhoto.StorageService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.*;

@Controller
public class WorkController {
    @NotNull
    private final Logger logger = LogManager.getLogger(PhotoStorageService.class.getName());

    @NotNull
    private final StorageService storageService;

    @NotNull
    private final FolderRepository folderRepository;

    @NotNull
    private final PhotoRepository photoRepository;

    @Autowired
    public WorkController(@NotNull StorageService storageService,
                          @NotNull FolderRepository folderRepository,
                          @NotNull PhotoRepository photoRepository) {
        this.storageService = storageService;
        this.folderRepository = folderRepository;
        this.photoRepository = photoRepository;
    }

    @GetMapping("/gallery")
    public String viewPhoto(Model model) {
        Map<String, Long> folders = DatasourceHelper.getFoldersWithPhotoIdentifier(folderRepository, photoRepository);
        if (!folders.isEmpty()) {
            model.addAttribute("folders", folders.keySet());
            model.addAttribute("foldersAndPhotos", folders);
        } else {
            model.addAttribute("folders", null);
            model.addAttribute("foldersAndPhotos", null);
        }
        return "gallery";
    }

    @GetMapping("/gallery/showOnePhoto/{identifier}")
    public ResponseEntity<byte[]> showOnePhoto(@PathVariable long identifier) {
        byte[] byteArray = storageService.loadPhotoAsResource(photoRepository, identifier);
        if (byteArray == null) {
            return null;
        }

        return ResponseEntity
                .ok()
                .contentType(MediaType.IMAGE_JPEG)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; identifier=\"" + identifier + "\"")
                .body(byteArray);
    }

    @GetMapping("/gallery/folder/{foldername}")
    public String viewPhotoInFolder(@PathVariable String foldername,
                                    Model model) {
        List<Photo> photos = DatasourceHelper.getPhotosFromFolder(folderRepository, photoRepository, foldername);
        if (photos == null) {
            String exceptionString = String.format("Folder with name %s doesn't exist", foldername);
            logger.error(exceptionString);
            model.addAttribute("operationStatus", exceptionString);
            model.addAttribute("isUploadable", false);
            return "folder";

        }

        model.addAttribute("photos", photos);
        model.addAttribute("isUploadable", true);
        return "folder";
    }

    @PostMapping("/gallery/folder/upload/{foldername}")
    public String uploadPhoto(@PathVariable String foldername,
                              @RequestParam("files") MultipartFile[] files,
                              RedirectAttributes redirectAttributes) {
        StringBuilder exceptionStringBuilder = new StringBuilder();
        boolean isOk = true;
        for (MultipartFile file : files) {
            Photo photo = null;
            boolean isSaved = false;
            try {//меня тут смущает, что я ловлю ексепшин,
                // в других методах у меня ексепшины обрабатываются
                // внутри вызываемых методов, не в контролере
                if (file == null || file.getOriginalFilename() == null) {
                    isOk = false;
                    String exceptionString = "Photo is nullable, photo isn't downloaded";
                    exceptionStringBuilder.append(exceptionString).append(System.lineSeparator());
                    logger.error(exceptionString);
                    continue;
                }

                photo = storageService.uploadPhotos(file.getOriginalFilename(), file.getBytes());
                if (photo == null) {
                    isOk = false;
                    String exceptionString = String.format("Photo %s isn't downloaded", file.getOriginalFilename());
                    exceptionStringBuilder.append(exceptionString).append(System.lineSeparator());
                    logger.error(exceptionString);
                    continue;
                }

                isSaved = DatasourceHelper.savePhotoToFolder(folderRepository,
                        photoRepository,
                        foldername,
                        photo);
                if (!isSaved) {
                    isOk = false;
                    String exceptionString = String.format("Photo %s with identifier %d isn't saved in DB",
                            file.getOriginalFilename(), photo.getIdentifier());
                    exceptionStringBuilder.append(exceptionString).append(System.lineSeparator());
                    logger.error(exceptionString);
                }
            } catch (IOException e) {
                isOk = false;
                logger.error(e.getMessage());
                String exceptionString = String.format("Photo %s isn't uploaded to disk", file.getOriginalFilename());
                logger.error(exceptionString);
                exceptionStringBuilder.append(exceptionString).append(System.lineSeparator());
            } finally {
                if (photo != null && !isSaved) {
                    boolean isDeleted = storageService.deletePhoto(photo);
                    if (isDeleted) {
                        isOk = false;
                        String exceptionString = String.format("Photo %s with identifier %d is deleted from disk",
                                file.getOriginalFilename(), photo.getIdentifier());
                        logger.info(exceptionString);
                        exceptionStringBuilder.append(exceptionString).append(System.lineSeparator());
                    } else{
                        isOk = false;
                        String exceptionString = String.format("Photo %s with identifier %d isn't deleted from disk",
                                file.getOriginalFilename(), photo.getIdentifier());
                        exceptionStringBuilder.append(exceptionString).append(System.lineSeparator());
                        logger.error(exceptionString);
                    }
                }
            }
        }

        if (isOk) {
            redirectAttributes.addFlashAttribute("operationStatus", "Download was correct");
        } else {
            redirectAttributes.addFlashAttribute("operationStatus", exceptionStringBuilder.toString());
        }
        return "redirect:/gallery/folder/{foldername}";
    }


    @PostMapping("/photo/changedescription")
    public ResponseEntity changeDescription(@NotNull @RequestBody Photo photo) {
        boolean isChanged = DatasourceHelper.changeDescription(photoRepository, photo);
        if (isChanged) {
            return new ResponseEntity<>("", HttpStatus.OK);
        } else {
            String exceptionString = String.format("Error in changing description to %s in photo with identifier %d",
                    photo.getDescription(),
                    photo.getIdentifier());
            logger.error(exceptionString);
            return new ResponseEntity<>("", HttpStatus.BAD_REQUEST);
        }
    }
}
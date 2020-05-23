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
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.*;

@Controller
public class WorkController {
    @NotNull
    private final Logger LOGGER = LogManager.getLogger(PhotoStorageService.class.getName());

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

    @GetMapping("/test")
    public String test() {
        return "test";
    }

    @GetMapping("/gallery")
    public String viewPhoto(Model model) {
        Map<String, Long> folders = DatasourceHelper.getFoldersWithPhoto(folderRepository, photoRepository);
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
    public ResponseEntity<?> showOnePhoto(@PathVariable long identifier) {
        byte[] byteArray = storageService.loadPhotoAsResource(photoRepository, identifier);
        if (byteArray == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Error loading photo");
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
            model.addAttribute("operationStatus", String.format("Folder with name %s doesn't exist", foldername));
            model.addAttribute("isUploadable", false);
            return "folder";
        }

        model.addAttribute("photos", photos);
        model.addAttribute("isUploadable", true);
        return "folder";
    }

    @PostMapping("/gallery/folder/upload/{folderName}")
    public String uploadPhoto(@PathVariable String folderName,
                              @RequestParam(value = "files") List<MultipartFile> files,
                              RedirectAttributes redirectAttributes) {
        boolean isOk = true;
        for (MultipartFile file : files) {
            try {
                if (file == null || file.getOriginalFilename() == null || file.getOriginalFilename() == "" || file.getBytes() == null) {
                    LOGGER.info("Photo is nullable, photo isn't downloaded");
                    isOk = false;
                    continue;
                }

                isOk = isOk && WorkControllerHelper.uploadOnePhoto(storageService,
                        folderRepository,
                        photoRepository,
                        folderName,
                        file.getOriginalFilename(),
                        file.getBytes());
            } catch (IOException e) {
                isOk = false;
                LOGGER.error(String.format("Photo %s isn't uploaded to disk", file.getOriginalFilename()), e);
            }
        }

        if (isOk) {
            redirectAttributes.addFlashAttribute("operationStatus", "Download was correct");
        } else {
            redirectAttributes.addFlashAttribute("operationStatus", "Download wasn't correct");
        }
        return "redirect:/gallery/folder/{folderName}";
    }

    @PostMapping("/photo/changedescription")
    public ResponseEntity<?> changeDescription(@RequestBody Photo photo) {
        boolean isChanged = DatasourceHelper.changeDescription(photoRepository, photo);
        if (isChanged) {
            return ResponseEntity.ok("{}");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("error");
        }
    }

    @ExceptionHandler({ Exception.class})
    public ModelAndView handleException(Exception e)
    {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("error");
        modelAndView.addObject("message", "В программе произошла ошибка, обратитесь к администратору");
        LOGGER.error(e.getMessage(), e);
        return modelAndView;
    }
}
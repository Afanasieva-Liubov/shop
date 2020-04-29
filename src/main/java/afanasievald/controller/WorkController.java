package afanasievald.controller;

import afanasievald.databaseEntity.Photo;
import afanasievald.repository.DatasourceHelper;
import afanasievald.repository.FolderRepository;
import afanasievald.repository.PhotoRepository;
import afanasievald.uploadingPhoto.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Controller
public class WorkController {
    private final StorageService storageService;

    @Autowired
    private PhotoRepository photoRepository;

    @Autowired
    private FolderRepository folderRepository;

    @Autowired
    public WorkController(StorageService storageService) {
        this.storageService = storageService;
    }

    @GetMapping("/test")
    public String test() {
        return "test";
    }

    @GetMapping("/gallery")
    public String viewPhoto(Model model) {
        Map<String, Long> folders = DatasourceHelper.getFoldersWithPhotoIdentifier(folderRepository, photoRepository);
        if (!folders.isEmpty()) {
            model.addAttribute("folders", folders.keySet());
            model.addAttribute("foldersAndPhotos", folders);
        }
        return "gallery";
    }

    @GetMapping("/gallery/showOneFoto/{identifier}")
    public ResponseEntity<byte[]> showOneFoto(@PathVariable Long identifier) throws Exception {
        Optional<Photo> photo = photoRepository.findByIdentifier(identifier);
        if (!photo.isPresent()) {
            return null;
        }

        byte[] byteArray = storageService.loadPhotoAsResource(photo.get().getName());
        if (byteArray == null) {
            return null;
        }

        return ResponseEntity
                .ok()
                .contentType(MediaType.IMAGE_JPEG)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + photo.get().getName() + "\"")
                .body(byteArray);
    }

    @GetMapping("/gallery/folder/{foldername}")
    public String viewPhotoInFolder(@PathVariable String foldername,
                                    Model model) {
        List<Photo> photos = DatasourceHelper.getPhotosFromFolder(folderRepository, photoRepository, foldername);
        if (!photos.isEmpty()) {
            model.addAttribute("photos", photos);
        }

        return "folder";
    }

    @PostMapping("/gallery/folder/upload/{foldername}")
    public String uploadPhoto(@PathVariable String foldername,
                              @RequestParam("files") MultipartFile[] files) throws Exception {
        for (MultipartFile file : files) {
            Photo photo = null;
            boolean isSaved = false;
            try {
                byte[] content = file.getBytes();
                photo = storageService.uploadPhotos(file.getOriginalFilename(), content);

                isSaved = DatasourceHelper.savePhotoToFolder(folderRepository,
                        photoRepository,
                        foldername,
                        photo);
            } finally {
                if (photo != null && isSaved == false) {
                    storageService.deletePhoto(photo);
                }

            }
        }
        return "redirect:/gallery/folder/{foldername}";
    }


    @PostMapping("/photo/changedescription")
    public ResponseEntity changeDescription(@RequestBody Photo photo) throws Exception {
        DatasourceHelper.changeDescription(photoRepository,
                photo.getIdentifier(),
                photo.getDescription());
        return new ResponseEntity<>("", HttpStatus.OK);
    }

}
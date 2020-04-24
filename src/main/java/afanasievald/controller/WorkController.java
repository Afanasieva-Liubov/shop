package afanasievald.controller;

import afanasievald.databaseEntity.Photo;
import afanasievald.repository.DatasourceHelper;
import afanasievald.repository.FolderRepository;
import afanasievald.repository.PhotoRepository;
import afanasievald.uploadingPhoto.StorageFileNotFoundException;
import afanasievald.uploadingPhoto.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.LinkedHashMap;
import java.util.List;

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
    public String test(Model model) {
        return "test";
    }

    @GetMapping("/gallery")
    public String viewPhoto(Model model) {
        LinkedHashMap<String, Integer> folders = DatasourceHelper.getFoldersWithPhotoHashcode(folderRepository, photoRepository);
        if (!folders.isEmpty()) {
            model.addAttribute("folders", folders.keySet());
            model.addAttribute("foldersAndPhotos", folders);
        }
        return "gallery";
    }

    @GetMapping("/gallery/folder/{foldername}/{hashcode}")
    public ResponseEntity<Resource> showOneFoto(@PathVariable String foldername,
                                                @PathVariable Integer hashcode) {
            Resource fileResource = storageService.loadPhotoAsResource(photoRepository, foldername, hashcode);
            if (fileResource == null){
                return null;
            }
            return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"" + fileResource.getFilename() + "\"").body(fileResource);
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
                            @RequestParam("files") MultipartFile[] files,
                              RedirectAttributes redirectAttributes) throws Exception {
        StringBuilder fileNames = storageService.uploadPhotos(folderRepository,
                photoRepository,
                foldername,
                files);
        return "redirect:/gallery/folder/{foldername}";
    }


    @PostMapping("/photo/changedescription")
    public ResponseEntity changeDescription(@RequestBody Photo photo,
                                            Errors errors) throws Exception{
        DatasourceHelper.changeDescription(photoRepository,
                photo.getHashcode(),
                photo.getDescription());
        return new ResponseEntity<>("", HttpStatus.OK);
    }

    @ExceptionHandler(StorageFileNotFoundException.class)
    public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exc) {
        return ResponseEntity.notFound().build();
    }
}
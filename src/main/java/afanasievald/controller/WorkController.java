package afanasievald.controller;

import afanasievald.databaseEntity.Photo;
import afanasievald.datasource.DatasourceService;
import afanasievald.repository.FolderRepository;
import afanasievald.repository.PhotoRepository;
import afanasievald.uploadingfiles.storage.StorageFileNotFoundException;
import afanasievald.uploadingfiles.storage.StorageService;
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
import java.util.Optional;

@Controller
public class WorkController {
    private final StorageService storageService;
    private final DatasourceService datasourceService;

    @Autowired
    private PhotoRepository photoRepository;

    @Autowired
    private FolderRepository folderRepository;

    @Autowired
    public WorkController(StorageService storageService) {
        this.storageService = storageService;
        this.datasourceService = new DatasourceService();
    }

    @GetMapping("/test")
    public String test(Model model) {
        return "test";
    }

    @GetMapping("/gallery")
    public String viewPhoto(Model model) {
        LinkedHashMap<String, Integer> folders = datasourceService.getFoldersWithPhotoHashcode(folderRepository, photoRepository);
        if (!folders.isEmpty()) {
            model.addAttribute("folders", folders.keySet());
            model.addAttribute("foldersAndPhotos", folders);
        }
        return "gallery";
    }

    @GetMapping("/gallery/folder/{foldername}/{hashcode}")
    public ResponseEntity<Resource> showOneFoto(@PathVariable String foldername,
                                                @PathVariable Integer hashcode,
                                                Model model) {
        Optional<Photo> photo = photoRepository.findByHashcode(hashcode);
        if (photo.isPresent()) {
            Resource fileResource = storageService.loadPhotoAsResource(String.format("%s/%s", foldername, photo.get().getName()));
            return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"" + fileResource.getFilename() + "\"").body(fileResource);
        }
        return null;
    }

    @GetMapping("/gallery/folder/{foldername}")
    public String viewPhotoInFolder(@PathVariable String foldername,
                                    Model model) {
        List<Photo> photos = datasourceService.getPhotosFromFolder(folderRepository, photoRepository, foldername);
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
                files,
                datasourceService);
        return "redirect:/gallery/folder/{foldername}";
    }


    @PostMapping("/photo/changedescription")
    public ResponseEntity changeDescription(@RequestBody Photo photo,
                                            Errors errors) throws Exception{
        datasourceService.changeDescription(photoRepository,
                photo.getHashcode(),
                photo.getDescription());
        return new ResponseEntity<>("", HttpStatus.OK);
    }

    @ExceptionHandler(StorageFileNotFoundException.class)
    public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exc) {
        return ResponseEntity.notFound().build();
    }
}
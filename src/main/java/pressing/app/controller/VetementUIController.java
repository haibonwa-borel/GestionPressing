package pressing.app.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pressing.app.dto.VetementDTO;
import pressing.app.repository.VetementRepository;
import pressing.app.service.FileStorageService;
import pressing.app.service.VetementService;

@Controller
@RequestMapping("/vetements")
public class VetementUIController {

    private final VetementRepository vetementRepository;
    private final VetementService vetementService;
    private final FileStorageService fileStorageService;

    public VetementUIController(VetementRepository vetementRepository,
                                VetementService vetementService,
                                FileStorageService fileStorageService) {
        this.vetementRepository = vetementRepository;
        this.vetementService = vetementService;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping
    public String vetements(Model model) {
        model.addAttribute("activeMenu", "vetements");
        model.addAttribute("vetements", vetementRepository.findAll());
        return "vetements";
    }

    @PostMapping
    public String creerVetement(
            @ModelAttribute VetementDTO dto,
            @RequestParam(value = "photo", required = false) MultipartFile photo) {
        gererUploadPhoto(dto, photo);
        vetementService.creer(dto);
        return "redirect:/vetements";
    }

    @PutMapping("/{id}")
    public String modifierVetement(
            @PathVariable Long id,
            @ModelAttribute VetementDTO dto,
            @RequestParam(value = "photo", required = false) MultipartFile photo) {
        gererUploadPhoto(dto, photo);
        vetementService.modifier(id, dto);
        return "redirect:/vetements";
    }

    @DeleteMapping("/{id}")
    public String supprimerVetement(@PathVariable Long id) {
        vetementService.supprimer(id);
        return "redirect:/vetements";
    }

    private void gererUploadPhoto(VetementDTO dto, MultipartFile photo) {
        if (photo != null && !photo.isEmpty()) {
            String fileName = fileStorageService.stockerFichier(photo);
            String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/uploads/")
                    .path(fileName)
                    .toUriString();
            dto.setPhotoUrl(fileDownloadUri);
        }
    }
}

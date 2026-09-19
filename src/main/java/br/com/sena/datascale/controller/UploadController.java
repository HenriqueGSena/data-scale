package br.com.sena.datascale.controller;


import br.com.sena.datascale.dto.UploadResponse;
import br.com.sena.datascale.entities.enums.IngestionStatus;
import br.com.sena.datascale.exceptions.InvalidUploadException;
import br.com.sena.datascale.service.UploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UploadController {

    private final UploadService uploadService;

    @PostMapping("/upload")
    public ResponseEntity<UploadResponse> upload(@RequestParam("file") MultipartFile file) {
        UploadResponse response = uploadService.processUpload(file);
        return ResponseEntity.accepted().body(response);
    }

    @ExceptionHandler(InvalidUploadException.class)
    public ResponseEntity<UploadResponse> handleInvalidUpload(InvalidUploadException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new UploadResponse(null, IngestionStatus.FAILED, e.getMessage()));
    }
}

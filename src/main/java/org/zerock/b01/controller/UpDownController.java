package org.zerock.b01.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.log4j.Log4j2;
import net.coobird.thumbnailator.Thumbnailator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.zerock.b01.dto.UploadResultDTO;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@RestController
@Log4j2
public class UpDownController {

    @Value("${org.zerock.upload.path}")
    private String uploadPath;

    @Operation(summary = "upload 파일", description = "POST 방식으로 파일 등록")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public List<UploadResultDTO> upload(@RequestPart MultipartFile[] multipartFile) {

//        log.info();

        final List<UploadResultDTO> list = new ArrayList<>();

        for (MultipartFile uploadImage : multipartFile) {
            String uploadImageName = uploadImage.getOriginalFilename();
            String uuid = UUID.randomUUID().toString();

            Path savePath = Paths.get(uploadPath, uuid + "_" + uploadImageName);

            boolean image = false;

            try {
                // void transferTo(File dest) throws IOException 업로드한 파일 데이터를 지정한 파일에 저장
                uploadImage.transferTo(savePath);

                // 이미지 파일 형식만 걸러내기
                if (Files.probeContentType(savePath).startsWith("image")) {

                    image = true;

                    File thumbFile = new File(uploadPath, "s_" + uuid + "_" + uploadImageName);
                    Thumbnailator.createThumbnail(savePath.toFile(), thumbFile,200, 200);
                }

                log.info("Image Saved : " + savePath);
            } catch (Exception e) {
                e.printStackTrace();
            }

            list.add(UploadResultDTO.builder()
                    .uuid(uuid)
                    .fileName(uploadImageName)
                    .img(image)
                    .build());





        }
        return list;


//                for (MultipartFile uploadImage : multipartFile) {
//            String uploadImageName = uploadImage.getOriginalFilename();
//            String uuid = UUID.randomUUID().toString();
//
//            File saveImage = new File(uuid + "_" + uploadImageName);
//
//            try {
//                // void transferTo(File dest) throws IOException 업로드한 파일 데이터를 지정한 파일에 저장
//                uploadImage.transferTo(saveImage);
//
//                // 이미지 파일 형식만 걸러내기
//
//
//                log.info("Image Saved : " + saveImage);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
    }

    @Operation(summary = "view 파일", description = "GET 방식으로 첨부파일 조회")
    @GetMapping("/view/{fileName}")
    public ResponseEntity<Resource> viewFileGET(@PathVariable String fileName) {

        Resource resource = new FileSystemResource(uploadPath + File.separator + fileName);

        String resourceName = resource.getFilename();
        HttpHeaders headers = new HttpHeaders();

        try {
            headers.add("Content-Type", Files.probeContentType(resource.getFile().toPath()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }

        return ResponseEntity.ok().headers(headers).body(resource);
    }

    @Operation(summary = "remove 파일", description = "DELETE 방식으로 파일 삭제")
    @DeleteMapping("/remove/{fileName}")
    public Map<String, Boolean> removeFile(@PathVariable String fileName) {

        Resource resource = new FileSystemResource(uploadPath + File.separator + fileName);

        String resourceName = resource.getFilename();

        Map<String, Boolean> resultMap = new HashMap<>();
        boolean removed = false;

        try {
            String ContentType = Files.probeContentType(resource.getFile().toPath());
            removed = resource.getFile().delete();

            // 썸네일이 존재할 경우 함께 처리
            if (ContentType.startsWith("image")) {
                File thumbnailFile = new File(uploadPath + File.separator + "s_" + fileName);

                thumbnailFile.delete();
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        resultMap.put("result", removed);

        return  resultMap;
    }

}

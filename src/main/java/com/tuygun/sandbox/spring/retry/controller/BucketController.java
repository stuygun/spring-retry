package com.tuygun.sandbox.spring.retry.controller;

import com.tuygun.sandbox.spring.retry.service.AmazonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/storage/")
public class BucketController {
  private AmazonService amazonService;

  @Autowired
  BucketController(AmazonService amazonService) {
    this.amazonService = amazonService;
  }

  @PostMapping("/uploadFile")
  public ResponseEntity<String> uploadFile(@RequestPart(value = "file") MultipartFile file) {
    try {
      return new ResponseEntity<>(this.amazonService.uploadFileWithRetry(file), HttpStatus.OK);
    } catch (Exception exception) {
      return new ResponseEntity<>("File couldn't be saved!", HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @DeleteMapping("/deleteFile")
  public String deleteFile(@RequestPart(value = "url") String fileUrl) {
    return this.amazonService.deleteFileFromS3Bucket(fileUrl);
  }
}

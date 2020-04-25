package com.tuygun.sandbox.spring.retry.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
public class AmazonService {
  private AmazonS3 s3client;

  private RetryTemplate retryTemplate;

  @Value("${amazonProperties.endpointUrl}")
  private String endpointUrl;

  @Value("${amazonProperties.bucketName}")
  private String bucketName;

  @Autowired
  AmazonService(AmazonS3 s3client, RetryTemplate retryTemplate) {
    this.s3client = s3client;
    this.retryTemplate = retryTemplate;
  }

  @Retryable(
      value = {RuntimeException.class},
      maxAttempts = 6,
      backoff = @Backoff(delay = 100, multiplier = 2),
      listeners = {"defaultListenerSupport"})
  public String uploadFileWithRetry(MultipartFile multipartFile) throws Exception {
    return uploadFile(multipartFile);
  }

  // Recovery method called after all the retry made
  // Should have the same return type and Optional throwable + same parameters with Retryable method
  @Recover
  public String recover(RuntimeException runtimeException, MultipartFile multipartFile)
      throws Exception {
    log.warn("Entering recovery method due to error=\"{}\"", runtimeException.getMessage());
    try {
      File file = convertMultiPartToFile(multipartFile);
      log.warn("File has been saved locally due to S3 connectivity issues.");
      return "Locally Stored, not to S3!";
    } catch (Exception exception) {
      log.error(
          "File even couldn't be stored locally, recovery is unsuccessful for error={} due to error={}",
          runtimeException.getMessage(),
          exception.getMessage());
      throw exception;
    }
  }

  public String uploadFile(MultipartFile multipartFile) throws Exception {
    String fileUrl;
    File file = null;

    try {
      file = convertMultiPartToFile(multipartFile);
      String fileName = generateFileName(multipartFile);
      fileUrl = endpointUrl + "/" + bucketName + "/" + fileName;
      uploadFileTos3bucket(fileName, file);
    } catch (Exception exception) {
      if (file != null) {
        boolean deleteResult = file.delete();
        log.debug(
            "File(name:{}) delete operation successful(result={}).", file.getName(), deleteResult);
      }
      throw exception;
    }

    return fileUrl;
  }

  // Via RetryTemplate
  public String deleteFileFromS3Bucket(String fileUrl) {
    AtomicReference<String> returnMessage = new AtomicReference<>("");

    String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
    retryTemplate.execute(
        retryContext -> {
          s3client.deleteObject(new DeleteObjectRequest(bucketName + "/", fileName));
          returnMessage.set("Successfully deleted");
          return null;
        });

    return returnMessage.get();
  }

  private File convertMultiPartToFile(MultipartFile file) throws IOException {
    File convertedFile = new File(Objects.requireNonNull(file.getOriginalFilename()));
    FileOutputStream fos = new FileOutputStream(convertedFile);
    fos.write(file.getBytes());
    fos.close();
    return convertedFile;
  }

  private String generateFileName(MultipartFile multiPart) {
    return new Date().getTime()
        + "-"
        + Objects.requireNonNull(multiPart.getOriginalFilename()).replace(" ", "_");
  }

  private void uploadFileTos3bucket(String fileName, File file) {
    s3client.putObject(
        new PutObjectRequest(bucketName, fileName, file).withCannedAcl(CannedAccessControlList.PublicRead));
  }
}

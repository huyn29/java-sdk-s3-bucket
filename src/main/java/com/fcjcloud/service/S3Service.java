package com.fcjcloud.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.util.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
@Service
public class S3Service implements FileServiceImpl {
    @Value("${aws.bucket.name}")
    private String bucketName;
    private final AmazonS3 amazonS3;

    public S3Service(AmazonS3 amazonS3) {
        this.amazonS3 = amazonS3;
    }

    @Override
    public String saveFile(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        try {
            File fileAfterConvert = convertMultiPartToFile(file);
            PutObjectResult putObjectResult = amazonS3.putObject(bucketName, originalFilename, fileAfterConvert);
            return putObjectResult.getContentMd5();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] downloadFile(String fileName) {
        S3Object object = amazonS3.getObject(bucketName, fileName);
        S3ObjectInputStream s3ObjectContent = object.getObjectContent();
        try {
            return IOUtils.toByteArray(s3ObjectContent);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String deleteFile(String fileName) {
        amazonS3.deleteObject(bucketName, fileName);
        return "File deleted!";
    }

    @Override
    public List<String> listAllFile() {
        ListObjectsV2Result listObjects = amazonS3.listObjectsV2(bucketName);
        return listObjects.getObjectSummaries().stream().map(S3ObjectSummary::getKey).collect(Collectors.toList());
    }

    private File convertMultiPartToFile(MultipartFile file) throws IOException {
        File convertFile = new File(file.getOriginalFilename());
        FileOutputStream fos = new FileOutputStream(convertFile);
        fos.close();
        return convertFile;
    }
}

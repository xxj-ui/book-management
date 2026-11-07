package com.bookmgt.controller;

import com.bookmgt.common.result.Result;
import com.bookmgt.common.config.MinIoConfig;
import io.minio.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/files")
public class FileController {

    private final MinioClient minioClient;
    private final MinIoConfig minIoConfig;

    public FileController(MinioClient minioClient, MinIoConfig minIoConfig) {
        this.minioClient = minioClient;
        this.minIoConfig = minIoConfig;
    }

    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        try {
            // 确保桶存在
            String bucketName = minIoConfig.getBucketName();
            boolean found = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(bucketName).build());
            if (!found) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder().bucket(bucketName).build());
            }

            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String ext = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                ext = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String objectName = UUID.randomUUID().toString() + ext;

            // 上传
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build());

            // 构造直接访问 URL（桶已设置公开读策略）
            String endpoint = minIoConfig.getEndpoint().replaceAll("/$", "");
            String url = endpoint + "/" + bucketName + "/" + objectName;

            return Result.success("上传成功", url);
        } catch (Exception e) {
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }
    }
}

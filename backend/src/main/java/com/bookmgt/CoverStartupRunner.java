package com.bookmgt;

import com.bookmgt.common.config.MinIoConfig;
import com.bookmgt.service.BookCoverService;
import com.bookmgt.util.MinioUtil;
import io.minio.MinioClient;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class CoverStartupRunner implements CommandLineRunner {

    private final BookCoverService bookCoverService;
    private final MinioClient minioClient;
    private final MinIoConfig minIoConfig;

    public CoverStartupRunner(BookCoverService bookCoverService,
                              MinioClient minioClient,
                              MinIoConfig minIoConfig) {
        this.bookCoverService = bookCoverService;
        this.minioClient = minioClient;
        this.minIoConfig = minIoConfig;
    }

    @Override
    public void run(String... args) {
        if (args.length > 0 && "generate-covers".equals(args[0])) {
            System.out.println("===== 设置 MinIO 桶公开读策略 =====");
            try {
                MinioUtil.setPublicReadPolicy(minioClient, minIoConfig.getBucketName());
                System.out.println("===== MinIO 桶公开读策略设置成功 =====");
            } catch (Exception e) {
                System.out.println("===== MinIO 桶策略设置失败: " + e.getMessage() + " =====");
            }

            System.out.println("===== 开始批量生成图书封面 =====");
            var ids = bookCoverService.fetchAllMissingCovers();
            System.out.println("===== 批量生成完成: " + ids.size() + " 本 =====");
        }
    }
}

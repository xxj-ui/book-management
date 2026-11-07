package com.bookmgt.service.impl;

import com.bookmgt.common.config.MinIoConfig;
import com.bookmgt.entity.Book;
import com.bookmgt.mapper.BookMapper;
import com.bookmgt.service.BookCoverService;
import com.bookmgt.util.MinioUtil;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class BookCoverServiceImpl implements BookCoverService {

    private static final Logger log = LoggerFactory.getLogger(BookCoverServiceImpl.class);

    /** 封面尺寸 */
    private static final int COVER_WIDTH = 300;
    private static final int COVER_HEIGHT = 400;

    /** 预设背景色（按分类ID） */
    private static final Color[] BG_COLORS = {
            new Color(0x5B, 0x7D, 0xB1), // 0 蓝色
            new Color(0x7B, 0xA0, 0x6B), // 1 绿色
            new Color(0xC0, 0x7C, 0x5A), // 2 橙色
            new Color(0x8E, 0x6C, 0xA5), // 3 紫色
            new Color(0x5A, 0x8F, 0x8C), // 4 青色
            new Color(0xB0, 0x6E, 0x6E), // 5 红色
            new Color(0x6E, 0x8B, 0x9E), // 6 灰蓝
            new Color(0x8B, 0x7D, 0x6B), // 7 棕色
    };

    private final BookMapper bookMapper;
    private final MinioClient minioClient;
    private final MinIoConfig minIoConfig;

    public BookCoverServiceImpl(BookMapper bookMapper, MinioClient minioClient, MinIoConfig minIoConfig) {
        this.bookMapper = bookMapper;
        this.minioClient = minioClient;
        this.minIoConfig = minIoConfig;
        // 启动时确保 MinIO bucket 有公开读策略
        ensureBucketPublic();
    }

    /**
     * 启动时确保 MinIO bucket 有公开读策略
     */
    private void ensureBucketPublic() {
        try {
            MinioUtil.setPublicReadPolicy(minioClient, minIoConfig.getBucketName());
            log.info("MinIO bucket 公开读策略已设置: {}", minIoConfig.getBucketName());
        } catch (Exception e) {
            log.warn("设置 MinIO bucket 公开读策略失败: {}", e.getMessage());
        }
    }

    @Override
    public boolean fetchCoverForBook(Long bookId) {
        Book book = bookMapper.selectById(bookId);
        if (book == null) {
            log.warn("图书不存在, id={}", bookId);
            return false;
        }
        if (book.getCoverUrl() != null && !book.getCoverUrl().isBlank()) {
            log.info("图书 {} 已有封面, 跳过", book.getIsbn());
            return true;
        }
        return generateAndUpload(book);
    }

    @Override
    public List<Long> fetchAllMissingCovers() {
        List<Book> allBooks = bookMapper.selectList(null);
        List<Long> successIds = new ArrayList<>();
        for (Book book : allBooks) {
            if (book.getCoverUrl() == null || book.getCoverUrl().isBlank()) {
                boolean ok = generateAndUpload(book);
                if (ok) {
                    successIds.add(book.getId());
                }
            }
        }
        log.info("批量生成封面完成: 成功 {} / 总计 {}", successIds.size(), allBooks.size());
        return successIds;
    }

    /**
     * 生成封面图片并上传到 MinIO
     */
    private boolean generateAndUpload(Book book) {
        try {
            // 1. 生成封面图片
            byte[] imageBytes = generateCoverImage(book);
            if (imageBytes == null) return false;

            // 2. 上传到 MinIO
            String bucketName = minIoConfig.getBucketName();
            String objectName = "cover_" + UUID.randomUUID() + ".jpg";

            // 确保 bucket 存在
            boolean found = minioClient.bucketExists(
                    io.minio.BucketExistsArgs.builder().bucket(bucketName).build());
            if (!found) {
                minioClient.makeBucket(
                        io.minio.MakeBucketArgs.builder().bucket(bucketName).build());
            }
            // 确保公开读策略（每次都尝试设置，如果已存在则忽略）
            MinioUtil.setPublicReadPolicy(minioClient, bucketName);
            // 上传
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(new ByteArrayInputStream(imageBytes), imageBytes.length, -1)
                            .contentType("image/jpeg")
                            .build());
            // 构造可访问 URL
            String endpoint = minIoConfig.getEndpoint().replaceAll("/$", "");
            String publicUrl = endpoint + "/" + bucketName + "/" + objectName;
            // 更新数据库
            bookMapper.updateCoverUrl(book.getId(), publicUrl);
            log.info("封面生成成功: id={}, isbn={}, url={}", book.getId(), book.getIsbn(), publicUrl);
            return true;
        } catch (Exception e) {
            log.error("生成封面临异常, id={}, isbn={}", book.getId(), book.getIsbn(), e);
            return false;
        }
    }

    /**
     * 用 Java 2D 绘制图书封面
     */
    private byte[] generateCoverImage(Book book) throws Exception {
        BufferedImage img = new BufferedImage(COVER_WIDTH, COVER_HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = img.createGraphics();

        // 抗锯齿
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // 背景色（按分类取色，分类为空则随机）
        Color bgColor = BG_COLORS[(int) (book.getId() % BG_COLORS.length)];
        if (book.getCategoryId() != null) {
            bgColor = BG_COLORS[(int) (book.getCategoryId() % BG_COLORS.length)];
        }
        g2d.setColor(bgColor);
        g2d.fillRect(0, 0, COVER_WIDTH, COVER_HEIGHT);

        // 装饰条纹
        g2d.setColor(new Color(bgColor.getRed(), bgColor.getGreen(), bgColor.getBlue(), 40));
        g2d.fillRect(20, 20, COVER_WIDTH - 40, COVER_HEIGHT - 40);
        g2d.setColor(new Color(255, 255, 255, 30));
        g2d.fillRect(30, 30, COVER_WIDTH - 60, COVER_HEIGHT - 60);

        // 顶部装饰条
        g2d.setColor(new Color(255, 255, 255, 60));
        g2d.fillRect(0, 50, COVER_WIDTH, 4);

        // 底部装饰条
        g2d.fillRect(0, COVER_HEIGHT - 80, COVER_WIDTH, 2);

        // 书名（自动换行）
        g2d.setColor(Color.WHITE);
        String title = book.getTitle();
        if (title == null || title.isBlank()) title = "未命名图书";

        // 根据标题长度调整字号
        int titleFontSize = title.length() > 15 ? 22 : title.length() > 8 ? 26 : 30;
        Font titleFont = new Font("Microsoft YaHei", Font.BOLD, titleFontSize);
        g2d.setFont(titleFont);

        FontRenderContext frc = g2d.getFontRenderContext();
        String[] lines = wrapText(title, titleFont, frc, COVER_WIDTH - 80);
        int titleY = COVER_HEIGHT / 2 - (lines.length * 35) / 2;
        for (String line : lines) {
            Rectangle2D bounds = titleFont.getStringBounds(line, frc);
            int x = (COVER_WIDTH - (int) bounds.getWidth()) / 2;
            g2d.drawString(line, x, titleY);
            titleY += 35;
        }

        // 作者
        String author = book.getAuthor();
        if (author != null && !author.isBlank()) {
            g2d.setFont(new Font("Microsoft YaHei", Font.PLAIN, 16));
            g2d.setColor(new Color(255, 255, 255, 200));
            Rectangle2D bounds = g2d.getFontMetrics().getStringBounds(author, g2d);
            int x = (COVER_WIDTH - (int) bounds.getWidth()) / 2;
            g2d.drawString(author, x, titleY + 30);
        }

        // 底部 ISBN
        String isbn = book.getIsbn();
        if (isbn != null && !isbn.isBlank()) {
            g2d.setFont(new Font("Consolas", Font.PLAIN, 11));
            g2d.setColor(new Color(255, 255, 255, 120));
            String isbnDisplay = isbn.length() > 17 ? isbn.substring(0, 17) + "..." : isbn;
            Rectangle2D bounds = g2d.getFontMetrics().getStringBounds(isbnDisplay, g2d);
            int x = (COVER_WIDTH - (int) bounds.getWidth()) / 2;
            g2d.drawString(isbnDisplay, x, COVER_HEIGHT - 30);
        }

        g2d.dispose();

        // 输出为 JPEG 字节数组
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "JPEG", baos);
        return baos.toByteArray();
    }

    /**
     * 自动换行
     */
    private String[] wrapText(String text, Font font, FontRenderContext frc, int maxWidth) {
        if (text.isEmpty()) return new String[]{""};
        List<String> lines = new ArrayList<>();
        StringBuilder currentLine = new StringBuilder();
        for (char c : text.toCharArray()) {
            String test = currentLine.toString() + c;
            if (font.getStringBounds(test, frc).getWidth() > maxWidth) {
                lines.add(currentLine.toString());
                currentLine = new StringBuilder();
            }
            currentLine.append(c);
        }
        if (!currentLine.isEmpty()) lines.add(currentLine.toString());
        return lines.toArray(new String[0]);
    }
}

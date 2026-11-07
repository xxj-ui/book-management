package com.bookmgt.controller;

import com.bookmgt.common.result.Result;
import com.bookmgt.entity.*;
import com.bookmgt.mapper.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/reports")
public class ReportController {

    private final BorrowRecordMapper borrowRecordMapper;
    private final BookMapper bookMapper;
    private final BookCopyMapper bookCopyMapper;
    private final ClassMapper classMapper;
    private final StudentMapper studentMapper;

    public ReportController(BorrowRecordMapper borrowRecordMapper, BookMapper bookMapper,
                            BookCopyMapper bookCopyMapper, ClassMapper classMapper,
                            StudentMapper studentMapper) {
        this.borrowRecordMapper = borrowRecordMapper;
        this.bookMapper = bookMapper;
        this.bookCopyMapper = bookCopyMapper;
        this.classMapper = classMapper;
        this.studentMapper = studentMapper;
    }

    @GetMapping("/overview")
    public Result<Map<String, Object>> overview(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long majorId,
            @RequestParam(required = false) String className) {
        List<BorrowRecord> records = filterRecords(startDate, endDate, departmentId, majorId, className);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalBorrows", records.size());
        stats.put("totalUsers", records.stream().map(BorrowRecord::getUserId).collect(Collectors.toSet()).size());
        stats.put("totalBooks", records.stream().map(BorrowRecord::getBookTitle).filter(Objects::nonNull).collect(Collectors.toSet()).size());
        stats.put("overdueCount", records.stream().filter(r -> r.getStatus() == 3).count());

        long overdueCount = records.stream().filter(r -> r.getStatus() == 3).count();
        stats.put("overdueRate", records.isEmpty() ? "0%" :
                String.format("%.1f%%", (double) overdueCount / records.size() * 100));

        return Result.success(stats);
    }

    @GetMapping("/monthly-trend")
    public Result<Map<String, Object>> monthlyTrend(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long majorId,
            @RequestParam(required = false) String className) {
        LocalDate sd = startDate != null ? LocalDate.parse(startDate) : LocalDate.of(2026, 3, 1);
        LocalDate ed = endDate != null ? LocalDate.parse(endDate) : LocalDate.of(2026, 7, 15);

        List<BorrowRecord> allRecords = filterRecords(startDate, endDate, departmentId, majorId, className);

        // 生成月份列表
        List<String> months = new ArrayList<>();
        List<Long> counts = new ArrayList<>();
        List<Long> overdueCounts = new ArrayList<>();

        LocalDate cursor = sd.withDayOfMonth(1);
        while (!cursor.isAfter(ed)) {
            String monthLabel = cursor.getMonthValue() + "月";
            months.add(monthLabel);

            String monthStr = String.format("%02d", cursor.getMonthValue());
            long count = allRecords.stream()
                    .filter(r -> r.getBorrowDate().toString().substring(5, 7).equals(monthStr))
                    .count();
            long overdue = allRecords.stream()
                    .filter(r -> r.getBorrowDate().toString().substring(5, 7).equals(monthStr) && r.getStatus() == 3)
                    .count();

            counts.add(count);
            overdueCounts.add(overdue);
            cursor = cursor.plusMonths(1);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("months", months);
        result.put("counts", counts);
        result.put("overdueCounts", overdueCounts);
        return Result.success(result);
    }

    @GetMapping("/category-distribution")
    public Result<List<Map<String, Object>>> categoryDistribution(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long majorId,
            @RequestParam(required = false) String className) {
        List<BorrowRecord> records = filterRecords(startDate, endDate, departmentId, majorId, className);
        Map<String, Long> countMap = new HashMap<>();

        for (BorrowRecord r : records) {
            if (r.getIsbn() != null) {
                Book book = bookMapper.selectDetailByIsbn(r.getIsbn());
                String cat = (book != null && book.getCategoryName() != null)
                        ? book.getCategoryName() : "未分类";
                countMap.merge(cat, 1L, Long::sum);
            }
        }

        List<Map<String, Object>> result = countMap.entrySet().stream()
                .map(e -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("name", e.getKey());
                    item.put("count", e.getValue());
                    return item;
                })
                .sorted((a, b) -> Long.compare((Long) b.get("count"), (Long) a.get("count")))
                .collect(Collectors.toList());

        return Result.success(result);
    }

    /** 根据学院/专业/班级筛选借阅记录 */
    private List<BorrowRecord> filterRecords(String startDate, String endDate,
                                              Long departmentId, Long majorId, String className) {
        List<BorrowRecord> all = borrowRecordMapper.selectAllWithDetail();
        // 日期过滤
        if (startDate != null && endDate != null) {
            LocalDate sd = LocalDate.parse(startDate);
            LocalDate ed = LocalDate.parse(endDate);
            all = all.stream()
                    .filter(r -> r.getBorrowDate() != null && !r.getBorrowDate().isBefore(sd) && !r.getBorrowDate().isAfter(ed))
                    .collect(Collectors.toList());
        }
        // 学院/专业/班级过滤（仅对学生有效）
        if (departmentId != null || majorId != null || (className != null && !className.isBlank())) {
            Set<Long> studentIds = filterStudentIds(departmentId, majorId, className);
            all = all.stream().filter(r -> studentIds.contains(r.getUserId())).collect(Collectors.toList());
        }
        return all;
    }

    /** 根据学院/专业/班级筛选学生 ID */
    private java.util.Set<Long> filterStudentIds(Long departmentId, Long majorId, String className) {
        // 获取所有班级
        java.util.Map<Long, ClassEntity> classMap = classMapper.selectList(null).stream()
                .collect(java.util.stream.Collectors.toMap(c -> c.getId(), c -> c));
        // 遍历所有学生，匹配条件
        java.util.Set<Long> result = new java.util.HashSet<>();
        for (com.bookmgt.entity.Student s : studentMapper.selectList(null)) {
            if (s.getClassId() == null) continue;
            ClassEntity c = classMap.get(s.getClassId());
            if (c == null) continue;
            if (departmentId != null) {
                boolean deptOk = (departmentId == 1 && c.getMajorId() != null && c.getMajorId() <= 3)
                        || (departmentId == 2 && c.getMajorId() != null && c.getMajorId() >= 4);
                if (!deptOk) continue;
            }
            if (majorId != null && !majorId.equals(c.getMajorId())) continue;
            if (className != null && !className.isBlank() && !className.equals(c.getName())) continue;
            result.add(s.getId());
        }
        return result;
    }

    @GetMapping("/export")
    public void export(HttpServletResponse response,
                       @RequestParam(required = false) String startDate,
                       @RequestParam(required = false) String endDate,
                       @RequestParam(required = false) Long departmentId,
                       @RequestParam(required = false) Long majorId,
                       @RequestParam(required = false) String className) throws Exception {
        // 获取筛选后的数据
        List<BorrowRecord> records = filterRecords(startDate, endDate, departmentId, majorId, className);
        Map<String, Object> stats = overview(startDate, endDate, departmentId, majorId, className).getData();

        // 创建工作簿
        org.apache.poi.xssf.usermodel.XSSFWorkbook wb = new org.apache.poi.xssf.usermodel.XSSFWorkbook();

        // 样式
        org.apache.poi.xssf.usermodel.XSSFCellStyle headerStyle = wb.createCellStyle();
        org.apache.poi.xssf.usermodel.XSSFFont headerFont = wb.createFont();
        headerFont.setBold(true); headerFont.setFontHeightInPoints((short) 12);
        headerStyle.setFont(headerFont);

        org.apache.poi.xssf.usermodel.XSSFCellStyle titleStyle = wb.createCellStyle();
        org.apache.poi.xssf.usermodel.XSSFFont titleFont = wb.createFont();
        titleFont.setBold(true); titleFont.setFontHeightInPoints((short) 14);
        titleStyle.setFont(titleFont);

        // Sheet 1: 概览
        org.apache.poi.xssf.usermodel.XSSFSheet sheet1 = wb.createSheet("概览统计");
        sheet1.createRow(0).createCell(0).setCellValue("图书管理系统 - 统计报表");
        sheet1.getRow(0).getCell(0).setCellStyle(titleStyle);
        sheet1.createRow(1).createCell(0).setCellValue("借阅总量: " + stats.getOrDefault("totalBorrows", 0));
        sheet1.createRow(2).createCell(0).setCellValue("借阅人数: " + stats.getOrDefault("totalUsers", 0));
        sheet1.createRow(3).createCell(0).setCellValue("涉及图书: " + stats.getOrDefault("totalBooks", 0));
        sheet1.createRow(4).createCell(0).setCellValue("超期率: " + stats.getOrDefault("overdueRate", "0%"));

        // Sheet 2: 借阅记录明细
        org.apache.poi.xssf.usermodel.XSSFSheet sheet2 = wb.createSheet("借阅明细");
        String[] cols = {"借阅单号", "读者", "身份", "图书", "ISBN", "借书日期", "应还日期", "实还日期", "状态", "滞纳金"};
        org.apache.poi.xssf.usermodel.XSSFRow headerRow = sheet2.createRow(0);
        for (int i = 0; i < cols.length; i++) {
            headerRow.createCell(i).setCellValue(cols[i]);
            headerRow.getCell(i).setCellStyle(headerStyle);
        }
        int rowIdx = 1;
        for (BorrowRecord r : records) {
            org.apache.poi.xssf.usermodel.XSSFRow row = sheet2.createRow(rowIdx++);
            row.createCell(0).setCellValue(r.getBorrowNo() != null ? r.getBorrowNo() : "");
            row.createCell(1).setCellValue(r.getUserName() != null ? r.getUserName() : "");
            row.createCell(2).setCellValue(r.getUserRoleName() != null ? mapRole(r.getUserRoleName()) : "");
            row.createCell(3).setCellValue(r.getBookTitle() != null ? r.getBookTitle() : "");
            row.createCell(4).setCellValue(r.getIsbn() != null ? r.getIsbn() : "");
            row.createCell(5).setCellValue(r.getBorrowDate() != null ? r.getBorrowDate().toString() : "");
            row.createCell(6).setCellValue(r.getDueDate() != null ? r.getDueDate().toString() : "");
            row.createCell(7).setCellValue(r.getReturnDate() != null ? r.getReturnDate().toString() : "—");
            row.createCell(8).setCellValue(r.getStatus() != null ? (r.getStatus() == 1 ? "借阅中" : r.getStatus() == 2 ? "已归还" : "超期") : "");
            row.createCell(9).setCellValue(r.getFineAmount() != null ? String.format("%.2f", r.getFineAmount()) : "0.00");
        }
        for (int i = 0; i < cols.length; i++) sheet2.autoSizeColumn(i);

        // Sheet 3: 月度借阅趋势
        Map<String, Object> trend = monthlyTrend(startDate, endDate, departmentId, majorId, className).getData();
        List<String> months = (List<String>) trend.getOrDefault("months", List.of());
        List<Long> counts = (List<Long>) trend.getOrDefault("counts", List.of());
        List<Long> overdueCounts = (List<Long>) trend.getOrDefault("overdueCounts", List.of());
        org.apache.poi.xssf.usermodel.XSSFSheet sheet3 = wb.createSheet("月度借阅趋势");
        sheet3.createRow(0).createCell(0).setCellValue("月份"); sheet3.getRow(0).createCell(1).setCellValue("借阅量"); sheet3.getRow(0).createCell(2).setCellValue("超期量");
        sheet3.getRow(0).getCell(0).setCellStyle(headerStyle); sheet3.getRow(0).getCell(1).setCellStyle(headerStyle); sheet3.getRow(0).getCell(2).setCellStyle(headerStyle);
        for (int i = 0; i < months.size(); i++) {
            sheet3.createRow(i + 1).createCell(0).setCellValue(months.get(i));
            sheet3.getRow(i + 1).createCell(1).setCellValue(counts.size() > i && counts.get(i) != null ? counts.get(i) : 0);
            sheet3.getRow(i + 1).createCell(2).setCellValue(overdueCounts.size() > i && overdueCounts.get(i) != null ? overdueCounts.get(i) : 0);
        }
        sheet3.autoSizeColumn(0); sheet3.autoSizeColumn(1); sheet3.autoSizeColumn(2);

        // Sheet 4: 图书分类分布
        List<Map<String, Object>> catDist = categoryDistribution(startDate, endDate, departmentId, majorId, className).getData();
        org.apache.poi.xssf.usermodel.XSSFSheet sheet4 = wb.createSheet("图书分类分布");
        sheet4.createRow(0).createCell(0).setCellValue("分类"); sheet4.getRow(0).createCell(1).setCellValue("册数");
        sheet4.getRow(0).getCell(0).setCellStyle(headerStyle); sheet4.getRow(0).getCell(1).setCellStyle(headerStyle);
        for (int i = 0; i < catDist.size(); i++) {
            sheet4.createRow(i + 1).createCell(0).setCellValue((String) catDist.get(i).getOrDefault("name", ""));
            sheet4.getRow(i + 1).createCell(1).setCellValue((Long) catDist.get(i).getOrDefault("count", 0L));
        }
        sheet4.autoSizeColumn(0); sheet4.autoSizeColumn(1);

        // 输出
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=report.xlsx");
        wb.write(response.getOutputStream());
        wb.close();
    }

    private String mapRole(String roleName) {
        if (roleName == null) return "";
        try { int v = Integer.parseInt(roleName);
            return java.util.Map.of(1, "高职", 2, "本科", 3, "硕士", 4, "博士").getOrDefault(v, "");
        } catch (NumberFormatException e) { return roleName; }
    }
}

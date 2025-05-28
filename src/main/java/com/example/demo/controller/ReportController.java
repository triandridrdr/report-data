package com.example.demo.controller;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.ooxml.JRDocxExporter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleXlsxReportConfiguration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.demo.repository.UserRepository;

import javax.sql.DataSource;
import jakarta.servlet.http.HttpServletResponse;


import com.example.demo.entity.User;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ReportController {

    @Autowired
    private DataSource dataSource;
    
    @Autowired
    UserRepository userRepository;

    @GetMapping("/")
    public String index() {
        return "index";
    }
    
    @GetMapping("/export-excell")
    public ResponseEntity<byte[]> exportToExcel() {
        try {
            // 1. Load report template (.jrxml)
            InputStream reportStream = new ClassPathResource("/laporan/user_report.jrxml").getInputStream();
            JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);

            // 2. Ambil data dari database
            List<User> users = userRepository.findAll();

            // 3. Buat data source Jasper
            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(users);

            // 4. Parameter tambahan (opsional)
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("createdBy", "Andrey");

            // 5. Fill laporan
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

            // 6. Export ke Excel
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            JRXlsxExporter exporter = new JRXlsxExporter();

            exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
            exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(baos));

            SimpleXlsxReportConfiguration configuration = new SimpleXlsxReportConfiguration();
            configuration.setOnePagePerSheet(false);
            configuration.setDetectCellType(true);
            configuration.setCollapseRowSpan(false);

            exporter.setConfiguration(configuration);
            exporter.exportReport();

            // 7. Kirim file Excel sebagai response
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=users.xlsx")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(baos.toByteArray());

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
}
    
    @GetMapping("/export-word")
    public void exportToWord(HttpServletResponse response) throws Exception {
        // Load file .jrxml
        InputStream jasperStream = getClass().getResourceAsStream("/laporan/user_report.jrxml");
        JasperReport jasperReport = JasperCompileManager.compileReport(jasperStream);

        // Ambil data dari database
        List<User> users = userRepository.findAll();
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(users);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("createdBy", "Laporan Spring Boot");

        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

        // Set response type untuk Word
        response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        response.setHeader("Content-Disposition", "attachment; filename=laporan.docx");

        // Export ke DOCX
        JRDocxExporter exporter = new JRDocxExporter();
        exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
        exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(response.getOutputStream()));
        exporter.exportReport();
    }


    @GetMapping("/laporan")
    public void cetakLaporan(HttpServletResponse response) throws Exception {
        InputStream jrxmlStream = getClass().getResourceAsStream("/laporan/user_report.jrxml");

        JasperReport jasperReport = JasperCompileManager.compileReport(jrxmlStream);
        JasperPrint jasperPrint = JasperFillManager.fillReport(
                jasperReport, new HashMap<>(), dataSource.getConnection());

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "inline; filename=laporan.pdf");

        JasperExportManager.exportReportToPdfStream(jasperPrint, response.getOutputStream());
    }
}

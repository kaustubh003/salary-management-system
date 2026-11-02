package com.acme.salary.service.impl;

import com.acme.salary.dto.PayslipBatchDto;
import com.acme.salary.dto.PayslipDto;
import com.acme.salary.entity.Employee;
import com.acme.salary.entity.Payment;
import com.acme.salary.exception.ResourceNotFoundException;
import com.acme.salary.payslip.PayslipPdfGenerator;
import com.acme.salary.repository.EmployeeRepository;
import com.acme.salary.repository.PaymentRepository;
import com.acme.salary.service.IPayslipService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Payslips are stored as files, one folder per employee: docs/{employeeCode}/payslip-{paymentId}.pdf.
 * The base folder can be changed with the app.payslip.directory property.
 */
@Service
public class PayslipServiceImpl implements IPayslipService {

    private static final Pattern FILE_NAME = Pattern.compile("payslip-(\\d{1,18})\\.pdf");
    private static final int PAGE_SIZE = 200;

    private final PaymentRepository paymentRepository;
    private final EmployeeRepository employeeRepository;
    private final PayslipPdfGenerator pdfGenerator;
    private final Path baseDirectory;

    public PayslipServiceImpl(PaymentRepository paymentRepository,
                              EmployeeRepository employeeRepository,
                              PayslipPdfGenerator pdfGenerator,
                              @Value("${app.payslip.directory:docs}") String directory) {
        this.paymentRepository = paymentRepository;
        this.employeeRepository = employeeRepository;
        this.pdfGenerator = pdfGenerator;
        this.baseDirectory = Path.of(directory).toAbsolutePath().normalize();
    }

    @Override
    public PayslipDto generatePayslip(Long paymentId) {
        Payment payment = findPayment(paymentId);
        return generate(findEmployee(payment.getEmployeeCode()), payment);
    }

    @Override
    public PayslipBatchDto generateAllPayslips() {
        Map<String, Employee> employees = new HashMap<>();
        int generated = 0;
        int skipped = 0;

        Pageable pageable = PageRequest.of(0, PAGE_SIZE, Sort.by("paymentID"));
        Page<Payment> page;
        do {
            page = paymentRepository.findAll(pageable);
            for (Payment payment : page.getContent()) {
                if (Files.exists(payslipPath(payment))) {
                    skipped++;
                    continue;
                }
                Employee employee = employees.computeIfAbsent(payment.getEmployeeCode(), this::findEmployee);
                generate(employee, payment);
                generated++;
            }
            pageable = page.nextPageable();
        } while (page.hasNext());

        return new PayslipBatchDto(generated, skipped);
    }

    @Override
    public Resource getPayslip(Long paymentId) {
        Path file = payslipPath(findPayment(paymentId));
        if (!Files.isRegularFile(file)) {
            throw new ResourceNotFoundException("Payslip", "paymentId", String.valueOf(paymentId));
        }
        return new FileSystemResource(file);
    }

    @Override
    public List<PayslipDto> getPayslips(String employeeCode) {
        if (!employeeRepository.existsById(employeeCode)) {
            throw new ResourceNotFoundException("Employee", "employeeCode", employeeCode);
        }
        Path directory = employeeDirectory(employeeCode);
        if (!Files.isDirectory(directory)) {
            return List.of();
        }
        try (Stream<Path> files = Files.list(directory)) {
            return files
                    .map(file -> toListedDto(employeeCode, file))
                    .filter(Objects::nonNull)
                    .sorted(Comparator.comparing(PayslipDto::getPaymentId))
                    .toList();
        } catch (IOException e) {
            throw new UncheckedIOException("Could not list the payslips of employee " + employeeCode, e);
        }
    }

    private PayslipDto generate(Employee employee, Payment payment) {
        byte[] pdf = pdfGenerator.generate(employee, payment);
        Path file = payslipPath(payment);
        try {
            Files.createDirectories(file.getParent());
            Files.write(file, pdf);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not store the payslip for payment " + payment.getPaymentID(), e);
        }
        return toDto(payment.getPaymentID(), payment.getEmployeeCode(), file);
    }

    private PayslipDto toListedDto(String employeeCode, Path file) {
        Matcher matcher = FILE_NAME.matcher(file.getFileName().toString());
        if (!matcher.matches()) {
            return null;
        }
        return toDto(Long.valueOf(matcher.group(1)), employeeCode, file);
    }

    private PayslipDto toDto(Long paymentId, String employeeCode, Path file) {
        PayslipDto dto = new PayslipDto();
        dto.setPaymentId(paymentId);
        dto.setEmployeeCode(employeeCode);
        dto.setFileName(file.getFileName().toString());
        dto.setGeneratedAt(lastModified(file));
        return dto;
    }

    private LocalDateTime lastModified(Path file) {
        try {
            return LocalDateTime.ofInstant(Files.getLastModifiedTime(file).toInstant(), ZoneId.systemDefault());
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read the payslip file " + file.getFileName(), e);
        }
    }

    private Path payslipPath(Payment payment) {
        return employeeDirectory(payment.getEmployeeCode())
                .resolve("payslip-" + payment.getPaymentID() + ".pdf");
    }

    /**
     * Resolves the employee's folder and makes sure it stays inside the base folder.
     */
    private Path employeeDirectory(String employeeCode) {
        Path directory = baseDirectory.resolve(employeeCode).normalize();
        if (!directory.startsWith(baseDirectory) || directory.equals(baseDirectory)) {
            throw new IllegalArgumentException("Invalid employee code");
        }
        return directory;
    }

    private Payment findPayment(Long paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "paymentId", String.valueOf(paymentId)));
    }

    private Employee findEmployee(String employeeCode) {
        return employeeRepository.findById(employeeCode)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "employeeCode", employeeCode));
    }
}

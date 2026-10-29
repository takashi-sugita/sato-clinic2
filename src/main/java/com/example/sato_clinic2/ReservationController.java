package com.example.sato_clinic2;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ReservationController {
    private static final List<String> TIMES = List.of(
            "09:00〜10:00", "10:00〜11:00", "11:00〜12:00", "13:00〜14:00",
            "14:00〜15:00", "15:00〜16:00", "16:00〜17:00", "17:00〜18:00");

    private final JdbcTemplate jdbc;

    public ReservationController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping("/availability")
    public ResponseEntity<?> availability(@RequestParam String date) {
        LocalDate reserveDate;
        try {
            reserveDate = LocalDate.parse(date);
        } catch (Exception e) {
            return error(HttpStatus.BAD_REQUEST, "予約日が正しくありません。");
        }
        List<String> booked = jdbc.queryForList(
                "SELECT reserve_time FROM reservations WHERE reserve_date = ? AND status = 'booked'",
                String.class, reserveDate);
        return ResponseEntity.ok(Map.of(
                "date", reserveDate.toString(),
                "slots", TIMES.stream().map(time -> Map.of("time", time, "available", !booked.contains(time))).toList()));
    }

    @PostMapping("/reservations")
    public ResponseEntity<?> create(@RequestBody ReservationRequest request) {
        String validationError = request.validate();
        if (validationError != null) return error(HttpStatus.BAD_REQUEST, validationError);

        try {
            jdbc.update("""
                    INSERT INTO reservations
                    (patient_type, card_number, department, patient_name, patient_phone, reserve_date, reserve_time)
                    VALUES (?, ?, ?, ?, ?, ?, ?)
                    """, request.patientType(), blankToNull(request.cardNumber()), request.department().trim(),
                    request.patientName().trim(), request.patientPhone().trim(), LocalDate.parse(request.reserveDate()),
                    request.reserveTime());
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "予約を受け付けました。"));
        } catch (DuplicateKeyException e) {
            return error(HttpStatus.CONFLICT, "その時間帯はすでに予約済みです。");
        }
    }

    @GetMapping("/admin/reservations")
    public ResponseEntity<?> list(org.springframework.web.context.request.WebRequest request) {
        if (!AdminAuthController.isLoggedIn(request)) return error(HttpStatus.UNAUTHORIZED, "ログインが必要です。");
        return ResponseEntity.ok(jdbc.queryForList("""
                SELECT id, patient_type AS patientType, card_number AS cardNumber, department,
                       patient_name AS name, patient_phone AS phone, reserve_date AS date,
                       reserve_time AS time, status, created_at AS createdAt
                FROM reservations WHERE status = 'booked' ORDER BY reserve_date, reserve_time
                """));
    }

    @DeleteMapping("/admin/reservations/{id}")
    public ResponseEntity<?> cancel(@PathVariable long id,
            org.springframework.web.context.request.WebRequest request) {
        if (!AdminAuthController.isLoggedIn(request)) return error(HttpStatus.UNAUTHORIZED, "ログインが必要です。");
        int updated = jdbc.update("UPDATE reservations SET status = 'cancelled' WHERE id = ? AND status = 'booked'", id);
        return updated == 0 ? error(HttpStatus.NOT_FOUND, "予約が見つかりません。") : ResponseEntity.noContent().build();
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private static ResponseEntity<Map<String, String>> error(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of("error", message));
    }

    public record ReservationRequest(String patientType, String cardNumber, String department,
            String patientName, String patientPhone, String reserveDate, String reserveTime) {
        String validate() {
            if (patientType == null || department == null || patientName == null || patientPhone == null
                    || reserveDate == null || reserveTime == null
                    || List.of(patientType, department, patientName, patientPhone, reserveDate, reserveTime)
                            .stream().anyMatch(String::isBlank)) return "必須項目を入力してください。";
            if (!patientType.equals("new") && !patientType.equals("existing")) return "患者区分が正しくありません。";
            if (patientType.equals("existing") && (cardNumber == null || !cardNumber.matches("\\d{8}")))
                return "診察券番号は8桁の数字で入力してください。";
            if (!patientPhone.replaceAll("[^0-9]", "").matches("0\\d{9,10}")) return "電話番号を正しく入力してください。";
            try {
                if (LocalDate.parse(reserveDate).isBefore(LocalDate.now())) return "本日以降の日付を選択してください。";
            } catch (Exception e) {
                return "予約日が正しくありません。";
            }
            return TIMES.contains(reserveTime) ? null : "予約時間が正しくありません。";
        }
    }
}

package com.example.sato_clinic2;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;

@RestController
public class NewsController {
    private final JdbcTemplate jdbc;

    public NewsController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping("/api/news")
    public List<NewsItem> publicList() {
        return readNews();
    }

    @GetMapping("/api/admin/news")
    public ResponseEntity<?> adminList(WebRequest request) {
        if (!AdminAuthController.isLoggedIn(request)) return unauthorized();
        return ResponseEntity.ok(readNews());
    }

    @PostMapping("/api/admin/news")
    public ResponseEntity<?> create(@RequestBody NewsRequest request, WebRequest session) {
        if (!AdminAuthController.isLoggedIn(session)) return unauthorized();
        String error = validate(request);
        if (error != null) return ResponseEntity.badRequest().body(Map.of("error", error));

        jdbc.update("INSERT INTO news_items (publish_date, title, tag, href) VALUES (?, ?, ?, ?)",
                parseDate(request.date()), request.title().trim(), request.tag().trim(), normalizeHref(request.href()));
        return ResponseEntity.status(HttpStatus.CREATED).body(readLatest());
    }

    @PutMapping("/api/admin/news/{id}")
    public ResponseEntity<?> update(@PathVariable long id, @RequestBody NewsRequest request, WebRequest session) {
        if (!AdminAuthController.isLoggedIn(session)) return unauthorized();
        String error = validate(request);
        if (error != null) return ResponseEntity.badRequest().body(Map.of("error", error));

        int updated = jdbc.update("UPDATE news_items SET publish_date = ?, title = ?, tag = ?, href = ? WHERE id = ?",
                parseDate(request.date()), request.title().trim(), request.tag().trim(), normalizeHref(request.href()), id);
        if (updated == 0) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(readOne(id));
    }

    @DeleteMapping("/api/admin/news/{id}")
    public ResponseEntity<?> delete(@PathVariable long id, WebRequest session) {
        if (!AdminAuthController.isLoggedIn(session)) return unauthorized();
        int deleted = jdbc.update("DELETE FROM news_items WHERE id = ?", id);
        return deleted == 0 ? ResponseEntity.notFound().build() : ResponseEntity.noContent().build();
    }

    private List<NewsItem> readNews() {
        return jdbc.query("SELECT id, publish_date, title, tag, href FROM news_items ORDER BY publish_date DESC, id DESC",
                (rs, row) -> new NewsItem(rs.getLong("id"), rs.getDate("publish_date").toLocalDate(),
                        rs.getString("title"), rs.getString("tag"), rs.getString("href")));
    }

    private NewsItem readOne(long id) {
        return jdbc.queryForObject("SELECT id, publish_date, title, tag, href FROM news_items WHERE id = ?",
                (rs, row) -> new NewsItem(rs.getLong("id"), rs.getDate("publish_date").toLocalDate(),
                        rs.getString("title"), rs.getString("tag"), rs.getString("href")), id);
    }

    private String validate(NewsRequest request) {
        if (request == null || request.date() == null || request.title() == null || request.tag() == null
                || request.date().isBlank() || request.title().isBlank() || request.tag().isBlank()) {
            return "日付、タイトル、タグを入力してください。";
        }
        try {
            parseDate(request.date());
        } catch (DateTimeParseException e) {
            return "日付は YYYY.MM.DD 形式で入力してください。";
        }
        return null;
    }

    private LocalDate parseDate(String value) {
        return LocalDate.parse(value.replace('.', '-'));
    }

    private String normalizeHref(String href) {
        return href == null || href.isBlank() ? "/information/index.html" : href.trim();
    }

    private NewsItem readLatest() {
        return jdbc.queryForObject("SELECT id, publish_date, title, tag, href FROM news_items ORDER BY id DESC LIMIT 1",
                (rs, row) -> new NewsItem(rs.getLong("id"), rs.getDate("publish_date").toLocalDate(),
                        rs.getString("title"), rs.getString("tag"), rs.getString("href")));
    }

    private ResponseEntity<Map<String, String>> unauthorized() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "ログインが必要です。"));
    }

    public record NewsItem(long id, LocalDate date, String title, String tag, String href) { }
    public record NewsRequest(String date, String title, String tag, String href) { }
}

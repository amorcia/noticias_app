package com.noticias.web.servicios;

import org.springframework.stereotype.Service;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;

@Service
public class LoggerService {

    private static final String LOG_DIR = "logs";
    private static final String ERROR_DIR = LOG_DIR + "/errors";
    private static final String SESSION_DIR = LOG_DIR + "/sessions";
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Tracks which users have already had their session log reset during this
    // server uptime
    private static final Set<String> initializedUsersInThisRun = new HashSet<>();

    public LoggerService() {
        createDirectories();
        scanAndCleanOldLogs();
    }

    private void createDirectories() {
        new File(ERROR_DIR).mkdirs();
        new File(SESSION_DIR).mkdirs();
    }

    /**
     * Initializes the session log for a user upon login.
     * Logic:
     * - If server just started (user not in set), OVERWRITE the old log.
     * - If user already logged in this session (user in set), APPEND (do nothing
     * here).
     */
    public void initSessionLog(String email) {
        if (email == null || email.trim().isEmpty())
            return;

        // Normalize email to filename
        String filename = getSessionFilename(email);
        File file = new File(SESSION_DIR, filename);

        synchronized (initializedUsersInThisRun) {
            if (!initializedUsersInThisRun.contains(email)) {
                // First login since server start -> Overwrite (Reset)
                try (PrintWriter writer = new PrintWriter(new FileWriter(file, false))) { // false = overwrite
                    writer.println("[" + LocalDateTime.now().format(DATE_FMT)
                            + "] [SESSION START] New server session started for: " + email);
                    writer.println("--------------------------------------------------");
                    initializedUsersInThisRun.add(email);
                } catch (IOException e) {
                    System.err.println("Error initializing session log for " + email + ": " + e.getMessage());
                }
            } else {
                // Already initialized in this boot -> Just mark as re-login (Append)
                logAction(email, "LOGIN", "User re-logged in (Server not restarted)");
            }
        }
    }

    /**
     * Appends an action to the user's session log.
     */
    public void logAction(String email, String action, String details) {
        if (email == null || email.trim().isEmpty())
            return;

        String filename = getSessionFilename(email);
        File file = new File(SESSION_DIR, filename);

        try (PrintWriter writer = new PrintWriter(new FileWriter(file, true))) { // true = append
            String entry = String.format("[%s] [%s] %s",
                    LocalDateTime.now().format(DATE_FMT),
                    action.toUpperCase(),
                    details != null ? details : "");
            writer.println(entry);
        } catch (IOException e) {
            System.err.println("Error writing to session log for " + email + ": " + e.getMessage());
        }
    }

    /**
     * Logs a system error with full stack trace.
     */
    public void logError(String context, Throwable t) {
        File file = new File(ERROR_DIR, "errors.log");

        try (PrintWriter writer = new PrintWriter(new FileWriter(file, true))) {
            writer.println("==================================================");
            writer.println("[" + LocalDateTime.now().format(DATE_FMT) + "] ERROR in: " + context);
            writer.println("Message: " + t.getMessage());
            t.printStackTrace(writer);
            writer.println("==================================================");
            writer.println(); // Empty line separator
        } catch (IOException e) {
            e.printStackTrace(); // Last resort
        }
    }

    /**
     * Deletes session logs older than 30 days.
     * Run once at startup.
     */
    private void scanAndCleanOldLogs() {
        File dir = new File(SESSION_DIR);
        File[] files = dir.listFiles();
        if (files == null)
            return;

        long thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000);

        for (File f : files) {
            if (f.isFile() && f.lastModified() < thirtyDaysAgo) {
                if (f.delete()) {
                    System.out.println("Deleted old session log: " + f.getName());
                }
            }
        }
    }

    private String getSessionFilename(String email) {
        // Replace special chars to be safe for filesystem
        return email.replaceAll("[^a-zA-Z0-9.@-]", "_") + ".log";
    }
}

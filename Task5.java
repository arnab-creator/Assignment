//FIX:

public class DocumentValidator {
    // FIX: Use SLF4J logger instead of printStackTrace for controlled logging
    private static final Logger logger =
            LoggerFactory.getLogger(DocumentValidator.class);

    public ValidationResult validate(Document doc) {
        try {
            // FIX: Issue 1 — Expected validation failures logged at WARN and returned directly.
            //      Throwing RuntimeException was flooding logs with unnecessary stack traces.
            if (doc == null) {
                logger.warn("Validation failed: document is null");
                return ValidationResult.invalid("Document is null");
            }
            String content = doc.extractContent();
            if (content == null || content.isEmpty()) {
                logger.warn("Validation failed: empty document content");
                return ValidationResult.invalid("Empty content");
            }
            return runValidationRules(content);
        } catch (Exception e) {
            // FIX: Issue 1 (continued) — Unexpected system errors logged at ERROR with stack trace.
            logger.error("Unexpected error during document validation", e);
            // FIX: Issue 2 — Never return null; caller invokes r.isValid() which would throw NPE.
            return ValidationResult.invalid("Validation processing error");
        }
    }

    public void validateBatch(List<Document> docs) {
        for (Document doc : docs) {
            try {
                ValidationResult r = validate(doc);
                // FIX: Issue 3 — validate() now always returns non-null; r != null retained as defensive guard.
                if (r != null && r.isValid()) {
                    saveResult(r);
                }
            } catch (Exception e) {
                // FIX: Issue 4 — Silent catch replaced with ERROR log; batch continues for remaining docs.
                logger.error("Failed to process document in batch", e);
            }
        }
    }
}
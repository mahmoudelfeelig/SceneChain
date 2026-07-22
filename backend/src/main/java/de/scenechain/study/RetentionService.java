package de.scenechain.study;

import de.scenechain.user.AccountRepository;
import java.time.OffsetDateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class RetentionService {
    private final AccountRepository accounts;
    private final String collectionClosedAt;
    private final String finalPublicationAt;

    public RetentionService(AccountRepository accounts,
            @Value("${scenechain.research.collection-closed-at:}") String collectionClosedAt,
            @Value("${scenechain.research.final-publication-at:}") String finalPublicationAt) {
        this.accounts = accounts; this.collectionClosedAt = collectionClosedAt; this.finalPublicationAt = finalPublicationAt;
    }

    @Scheduled(cron = "0 17 3 * * *", zone = "UTC")
    public void enforce() {
        OffsetDateTime cutoff = cutoff();
        if (cutoff != null && !OffsetDateTime.now().isBefore(cutoff)) accounts.deleteSubjectsBefore(cutoff);
    }

    OffsetDateTime cutoff() {
        return cutoff(collectionClosedAt, finalPublicationAt);
    }

    static OffsetDateTime cutoff(String collectionClosedAt, String finalPublicationAt) {
        OffsetDateTime collection = parse(collectionClosedAt);
        OffsetDateTime publication = parse(finalPublicationAt);
        OffsetDateTime byCollection = collection == null ? null : collection.plusMonths(24);
        OffsetDateTime byPublication = publication == null ? null : publication.plusMonths(6);
        if (byCollection == null) return byPublication;
        if (byPublication == null) return byCollection;
        return byCollection.isBefore(byPublication) ? byCollection : byPublication;
    }

    private static OffsetDateTime parse(String value) {
        try { return value == null || value.isBlank() ? null : OffsetDateTime.parse(value); }
        catch (RuntimeException error) { throw new IllegalStateException("Invalid retention timestamp", error); }
    }
}

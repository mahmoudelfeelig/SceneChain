package de.scenechain.study;

import de.scenechain.auth.Cookies;
import de.scenechain.auth.SessionStore;
import de.scenechain.crypto.CredentialCrypto;
import de.scenechain.user.AccountRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/participant")
public class ParticipantDataController {
    public record DeleteRequest(@NotBlank @Size(max=128) String password,
                                boolean understandDeletionIsPermanent) {}
    private final SessionStore sessions;
    private final AccountRepository accounts;
    private final CredentialCrypto crypto;
    private final Cookies cookies;

    public ParticipantDataController(SessionStore sessions, AccountRepository accounts,
                                     CredentialCrypto crypto, Cookies cookies) {
        this.sessions = sessions; this.accounts = accounts; this.crypto = crypto; this.cookies = cookies;
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@Valid @RequestBody DeleteRequest request, HttpServletRequest servletRequest,
                       HttpServletResponse response) {
        String token = Cookies.read(servletRequest.getCookies(), Cookies.SESSION);
        UUID accountId = sessions.get(token);
        if (accountId == null || !request.understandDeletionIsPermanent()) throw denied();
        var password = accounts.password(accountId).orElseThrow(this::denied);
        if (!crypto.verify(request.password().getBytes(StandardCharsets.UTF_8), password.salt(), accountId, password.verifier())) {
            throw denied();
        }
        sessions.delete(token);
        accounts.delete(accountId);
        cookies.clear(response, Cookies.SESSION);
    }

    private ResponseStatusException denied() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Deletion verification failed");
    }
}

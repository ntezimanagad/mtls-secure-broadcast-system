package com.example.mtlsserver;

import com.example.mtlsserver.model.User;
import com.example.mtlsserver.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RestController;

import java.security.cert.X509Certificate;
import java.time.Instant;

@RestController
public class PatchController {

    private final UserRepository userRepository;
    private final UdpBroadcastService udpBroadcastService;

    public PatchController(UserRepository userRepository,
            UdpBroadcastService udpBroadcastService) {
        this.userRepository = userRepository;
        this.udpBroadcastService = udpBroadcastService;
    }

    @PatchMapping("/heartbeat")
    public ResponseEntity<String> heartbeat(
            @RequestAttribute(name = "jakarta.servlet.request.X509Certificate", required = false) X509Certificate[] certs,
            HttpServletRequest request) {

        if (certs == null || certs.length == 0) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body("No client certificate provided");
        }

        X509Certificate clientCert = certs[0];
        String dn = clientCert.getSubjectX500Principal().getName();
        String cn = extractCN(dn);

        if (cn == null || !cn.contains("@")) {
            return ResponseEntity
                    .badRequest()
                    .body("Invalid CN format");
        }

        User user = userRepository.findByEmail(cn)
                .orElse(null);

        if (user == null) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body("User not registered");
        }

        Instant now = Instant.now();
        long lastSeenNano = (now.getEpochSecond() * 1_000_000_000L) + now.getNano();

        user.setLastSeen(lastSeenNano);
        user.setIp(request.getRemoteAddr());
        user.setPort(request.getRemotePort());

        userRepository.save(user);

        udpBroadcastService.broadcastUser(user);

        return ResponseEntity.ok("User updated: " + cn);
    }

    private String extractCN(String dn) {
        for (String part : dn.split(",")) {
            part = part.trim();
            if (part.startsWith("CN=")) {
                return part.substring(3);
            }
        }
        return null;
    }
}

package alfarius.yushinon.nosql1.database.controllers;

import alfarius.yushinon.nosql1.database.services.AuthService;
import alfarius.yushinon.nosql1.entity.requestsbody.AuthenticationRequest;
import alfarius.yushinon.nosql1.entity.requestsbody.JwtResponse;
import alfarius.yushinon.nosql1.entity.requestsbody.RegisterRequest;
import alfarius.yushinon.nosql1.entity.requestsbody.RegisterResponce;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("login")
    public ResponseEntity<JwtResponse> login(@RequestBody AuthenticationRequest authRequest) {
        final JwtResponse token = authService.login(authRequest);
        return ResponseEntity.ok(token);
    }

    @PostMapping("register")
    public ResponseEntity<RegisterResponce> register(@RequestBody RegisterRequest rr) {
        return ResponseEntity.ok(
                authService.register(rr)
        );
    }

}
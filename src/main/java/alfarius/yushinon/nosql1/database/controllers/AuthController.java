package alfarius.yushinon.nosql1.database.controllers;

import alfarius.yushinon.nosql1.database.services.AuthService;
import alfarius.yushinon.nosql1.entity.User;
import alfarius.yushinon.nosql1.util.AuthResponse;
import alfarius.yushinon.nosql1.util.RegisterRequest;
import alfarius.yushinon.nosql1.util.LoginRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

//https://habr.com/ru/articles/1059086/

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request){
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login (@RequestBody LoginRequest request){
        return ResponseEntity.ok(authService.login(request));
    }
}

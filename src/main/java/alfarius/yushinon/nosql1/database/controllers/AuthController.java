package alfarius.yushinon.nosql1.database.controllers;

import alfarius.yushinon.nosql1.database.services.AuthenticationService;
import alfarius.yushinon.nosql1.entity.AuthenticationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService authenticationService;

//    @PostMapping("/login")
//    public ResponseEntity<Map<String, String>> login(@RequestBody AuthenticationRequest request) {
//        Map<String, String> authResponse = authenticationService.authenticate(
//                request.getLogin(),
//                request.getPassword()
//        );
//        return ResponseEntity.ok(authResponse);
//    }
}
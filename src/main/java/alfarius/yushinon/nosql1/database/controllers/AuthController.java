package alfarius.yushinon.nosql1.database.controllers;

import alfarius.yushinon.nosql1.database.services.AuthService;
import alfarius.yushinon.nosql1.database.services.UserService;
import alfarius.yushinon.nosql1.entity.User;
import alfarius.yushinon.nosql1.utils.JWTUtil;
import alfarius.yushinon.nosql1.utils.SHAGenerator;
import com.sun.net.httpserver.HttpsServer;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody User user) {
        User userDB = userService.findByUsername(user.getUsername());
        if(userDB == null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String passwordHash = SHAGenerator.generateSHA256Hash(user.getPassword());
        if(!passwordEncoder.matches(passwordHash, userDB.getPassword())){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        String token = JWTUtil.generateToken(userDB.getUsername(), userDB.getRole().toString());
        return ResponseEntity.ok().body(Map.of(
                "token", token,
                "role", userDB.getRole().toString())
        );
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody User user) {
        if(userService.findByUsername(user.getUsername()) != null || user.getUsername().isEmpty()){
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        if(user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(SHAGenerator.generateSHA256Hash(user.getPassword()));
            userService.saveUser(user);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}
package alfarius.yushinon.nosql1.database.controllers;

import alfarius.yushinon.nosql1.database.services.UserService;
import alfarius.yushinon.nosql1.entity.User;
import alfarius.yushinon.nosql1.utils.JWTUtil;
import alfarius.yushinon.nosql1.utils.SHAGenerator;
import jakarta.servlet.http.HttpServletRequest;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private JWTUtil jwtUtils;

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

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            long remainingExpiration = jwtUtils.getRemainingExpirationMillis(token);

            if (remainingExpiration > 0) {
                RBucket<String> blacklistBucket = redissonClient.getBucket("blacklist:" + token);
                blacklistBucket.set("revoked", remainingExpiration, TimeUnit.MILLISECONDS);
            }
        }

        return ResponseEntity.ok("Successfully logged out");
    }
}
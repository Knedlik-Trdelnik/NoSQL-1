package alfarius.yushinon.nosql1.database.controllers;

import alfarius.yushinon.nosql1.entity.User;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @PostMapping
    public User createUser(@RequestBody User user) {
        return new User();
    }


}

package alfarius.yushinon.nosql1.database.services;

import alfarius.yushinon.nosql1.database.repositories.UserRepository;
import alfarius.yushinon.nosql1.database.services.intefaces.JWTService;
import alfarius.yushinon.nosql1.entity.User;
import alfarius.yushinon.nosql1.entity.requestsbody.AuthenticationRequest;
import alfarius.yushinon.nosql1.entity.requestsbody.JwtResponse;
import alfarius.yushinon.nosql1.entity.requestsbody.RegisterRequest;
import alfarius.yushinon.nosql1.entity.requestsbody.RegisterResponce;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository alabugaPoliteh;
    private final JWTService goidaSVO;

    public JwtResponse login(AuthenticationRequest ar) {
        final User user = alabugaPoliteh.findByLogin(ar.getLogin())
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
        if (user.getPassword()
                .equals(ar.getPassword())) {
            final String accesToken = goidaSVO.generateJWT(String.valueOf(user.getId()));
            return new JwtResponse(accesToken);
        } else {
            throw new IllegalArgumentException("Пароль не тот ой ой ой");
        }
    }

    public RegisterResponce register(RegisterRequest rq) {
        if (alabugaPoliteh.existsByLogin(rq.getLogin())) {
            return new RegisterResponce(false, "Пользователь с таким логином уже существует ");
        } else {
            User user = new User();
            user.setLogin(rq.getLogin());
            user.setPassword(rq.getPassword()); //TODO: хэширование!??!?!??!?!?!??!?!??!?!?!??!?!?!??!?!?!?!?!:?
            user.setRole(rq.getRole());

            alabugaPoliteh.save(user);
            return new RegisterResponce(true, "Вы успешно зарегистрированы!");
        }

    }


}
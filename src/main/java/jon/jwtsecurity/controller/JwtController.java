package jon.jwtsecurity.controller;

import jon.jwtsecurity.JwtTokenManager;
import jon.jwtsecurity.model.JwtRequestModel;
import jon.jwtsecurity.model.JwtResponseModel;
import jon.jwtsecurity.model.User;
import jon.jwtsecurity.service.IUserService;
import jon.jwtsecurity.service.JwtUserDetailsService;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@AllArgsConstructor
@NoArgsConstructor
public class JwtController {
    @Autowired
    private JwtUserDetailsService userDetailsService;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtTokenManager jwtTokenManager;
    @Autowired
    private IUserService userService;

    @PostMapping("/signup")
    public ResponseEntity<JwtResponseModel> signup(@RequestBody JwtRequestModel request){
        System.out.println("signup: username:" + request.getUsername() + " password: " + request.getPassword() );
        User user = new User(request.getUsername(),request.getPassword());
        if(userService.findByName(user.getUsername()).size()==0) {
            if (userService.save(user) != null) {
                return ResponseEntity.ok(new JwtResponseModel("created user: " + user.getUsername() + " pw: " + user.getPassword()));
            } else {
                return ResponseEntity.ok(new JwtResponseModel("error creating user: " + user.getUsername()));
            }
        }else {
                return ResponseEntity.ok(new JwtResponseModel("error: user exists: " + user.getUsername()));
        }
    }

    @PostMapping("/login") // Security, step 1: Incoming request
    public ResponseEntity<Map<String,String >> createToken(@RequestBody JwtRequestModel request, HttpServletResponse response) throws Exception {
        // HttpServletRequest servletRequest is available from Spring, if needed.
        System.out.println(" JwtController createToken Call: 4" + request.getUsername());
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(),
                            request.getPassword())
                    // Security, step 2:
                    // will call loadUserByUsername(uname, pw) from the object of JwtUserDetailsService class
            );
        } catch (DisabledException e) {
            throw new Exception("USER_DISABLED", e);
        } catch (BadCredentialsException e) {
            return ResponseEntity.ok(Map.of("message", "Bad Credentials"));
        }
        final UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
        final String jwtToken = jwtTokenManager.generateJwtToken(userDetails);
        // refactor: send token in cookie. response object is managed by Spring Security, therefore no return here.
        sendJwtAsCookie(response,jwtToken);
        return ResponseEntity.ok(Map.of("message", "Login successful"));
        //return ResponseEntity.ok(new JwtResponseModel(jwtToken));
    }

    public void sendJwtAsCookie(HttpServletResponse response, String jwt) {
        Cookie cookie = new Cookie("token", jwt);
        cookie.setHttpOnly(true); // er IKKE tilgængelig i javascript
        cookie.setSecure(false); // NOTICE: set to TRUE for production (HTTPS)
        cookie.setPath("/"); // here you can specify endpoints to get this particular cookie !
        cookie.setMaxAge(60 * 60); // 1 hour
        response.addCookie(cookie);
    }

    @PostMapping("/logout2")
    public ResponseEntity<String> logout(HttpServletResponse response) {
        System.out.println("Logout successful...");
        Cookie cookie = new Cookie("token", "");
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // true in production
        cookie.setPath("/");
        cookie.setMaxAge(0); // This deletes the cookie
        response.addCookie(cookie);
        //return ResponseEntity.ok(Map.of("message", "Logged out"));
        return ResponseEntity.ok("Logged out");
    }

    @PostMapping("/getSecret")
    public ResponseEntity<Map> getSecret() {
        System.out.println("getSecret is called");
        Map<String,String > map = new HashMap<>();
        map.put("message","this is secret from server, uuuuuhh");
        return ResponseEntity.ok(map);
    }

    @DeleteMapping("/deleteUser")
    public ResponseEntity<Map> deleteUser(@RequestBody User user) { // hvis man kommer hertil, er token OK
        System.out.println("deleteUser is called with user: " + user.getUsername());
        // evt. findById, som finder hele objektet fra MySQL, inkl. id.
        List<User> users =  userService.findByName(user.getUsername());
        User userToDelete = users.get(0);
        userService.delete(userToDelete);
        Map<String,String > map = new HashMap<>();
        map.put("message","user deleted, if found " + user.getUsername());
        return ResponseEntity.ok(map);
    }

    @GetMapping("/hello")
    public ResponseEntity<Map> getHello() {
        System.out.println("getHello is called");
        Map<String,String > map = new HashMap<>();
        map.put("message","this is HELLO from server");
        return ResponseEntity.ok(map);
    }

}
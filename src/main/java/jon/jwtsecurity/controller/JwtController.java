package jon.jwtsecurity.controller;

import jakarta.servlet.http.HttpSession;
import jon.jwtsecurity.JwtTokenManager;
import jon.jwtsecurity.model.JwtRequestModel;
import jon.jwtsecurity.model.JwtResponseModel;
import jon.jwtsecurity.model.User;
import jon.jwtsecurity.service.IUserService;
import jon.jwtsecurity.service.JwtUserDetailsService;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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

    // Ali: Denne udsteder udsteder JWT som HttpOnly-cookie
    // Ali: Tilføjet at den tager imod httpsession som parameter også
    @PostMapping("/login") // Security, step 1: Incoming request
    public ResponseEntity<Map<String,String >> createToken(@RequestBody JwtRequestModel request, HttpServletResponse response, HttpSession session) throws Exception {
        // HttpServletRequest servletRequest is available from Spring, if needed.
        System.out.println(" JwtController createToken Call: 4" + request.getUsername());
        Map<String, String> map = new HashMap<>();
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(),
                            request.getPassword())
                    // Security, step 2:
                    // will call loadUserByUsername(uname, pw) from the object of JwtUserDetailsService class
            );
        } catch (UsernameNotFoundException e) {
            map.put("message", "username not found " + e.getMessage());
            return ResponseEntity.ok(map);
        } catch (BadCredentialsException e) {
            map.put("message", "username or password incorrect");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(map);
        }catch (Exception e) {
            map.put("message", "something went wrong. General exception");
            return ResponseEntity.ok(map);
        }
        final UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
        final String jwtToken = jwtTokenManager.generateJwtToken(userDetails);
        // sends token in cookie. response object is managed by Spring Security, therefore no return here:
        sendJwtAsCookie(response,jwtToken);

        // Ali: Gem brugernavnet i sessionen
        session.setAttribute("username", request.getUsername());

        map.put("message", "login success");
        return ResponseEntity.ok(map);
    }

    public void sendJwtAsCookie(HttpServletResponse response, String jwt) {
        Cookie cookie = new Cookie("token", jwt);
        cookie.setHttpOnly(true); // er IKKE tilgængelig i javascript
        cookie.setSecure(false); // NOTICE: set to TRUE for production (HTTPS)
        cookie.setPath("/"); // here you can specify endpoints to get this particular cookie !
        cookie.setMaxAge(60 * 60); // 1 hour
        response.addCookie(cookie);
    }

    // Ali: denne metode fjerner tokens
    @PostMapping("/logout2") // named "logout2" because Spring Boot has "taken" logout
    public ResponseEntity<String> logout(HttpServletResponse response) {
        System.out.println("Logout successful...");
        Cookie cookie = new Cookie("token", "");
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // true in production
        cookie.setPath("/");
        cookie.setMaxAge(0); // This deletes the cookie
        response.addCookie(cookie);
        return ResponseEntity.ok("Logged out");
    }

    /*
    @PostMapping("/getSecret")
    public String getSecret() {
        Map<String,String > map = new HashMap<>();
        map.put("message","this is secret from server, uuuuuhh");
        //return ResponseEntity.ok(map);
        return "home/index";
    }
    */

    @PostMapping("/getSecret")
    public ResponseEntity<?> getSecret() {
        Map<String, String> map = new HashMap<>();
        map.put("message", "En besked");
        return ResponseEntity.ok(map);
    }

    @DeleteMapping("/deleteUser")
    public ResponseEntity<Map> deleteUser(@RequestBody User user) { // hvis man kommer hertil, er token OK
        System.out.println("deleteUser is called with user: " + user.getUsername());
        // evt. findById, som finder hele objektet fra MySQL, inkl. id.
        Map<String, String> map = new HashMap<>();
        try {
            List<User> users = userService.findByName(user.getUsername());
            if(users.size()==0){
                map.put("message", "user not found: " + user.getUsername());
            }else {
                User userToDelete = users.get(0);
                userService.delete(userToDelete);
                map.put("message", "user deleted: " + user.getUsername());
            }
        }catch (Exception e) {
            map.put("message", "error deleting user: " + user.getUsername() + " " + e.getMessage());
        }
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
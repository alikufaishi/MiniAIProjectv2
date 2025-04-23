package jon.jwtsecurity.service;

import jon.jwtsecurity.config.SecurityConfiguration;
import jon.jwtsecurity.model.User;
import jon.jwtsecurity.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

// Denne implementerer vores hjemmelavede IUserService interface

@AllArgsConstructor
@Service
public class UserService implements IUserService{

    private UserRepository userRepository;
    @Override
    public Set<User> findAll() {
        Set<User> set = new HashSet<>();
        userRepository.findAll().forEach(set::add);
        return set;
    }

    @Override
    public User save(User user) {
            PasswordEncoder pw = SecurityConfiguration.passwordEncoder();
            user.setPassword(pw.encode(user.getPassword()));
        return userRepository.save(user);
    }

    @Override
    public void delete(User object) {
            userRepository.delete(object);
    }

    @Override
    public void deleteById(Long aLong) {

    }

    @Override
    public Optional<User> findById(Long aLong) {
        return userRepository.findById(aLong);
    }

    @Override
    public List<User> findByName(String name) {
        System.out.println("Userservice called findByName with argument: " + name);
        return userRepository.findByUsername(name);
    }
}

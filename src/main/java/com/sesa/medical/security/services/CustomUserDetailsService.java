package com.sesa.medical.security.services;


import com.sesa.medical.security.UserPrincipal;
import com.sesa.medical.security.exception.ResourceNotFoundException;
import com.sesa.medical.users.entities.Users;
import com.sesa.medical.users.repository.IUsersRepository;
import com.sesa.medical.users.services.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by rajeevkumarsingh on 02/08/17.
 */

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    IUsersRepository userRepository;
  @Autowired
    IUserService userService;
    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {
        Users user = userRepository.findByUsernameOrEmailOrTel1(username,username,username)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found with username : " + username)
        );

        return UserPrincipal.create(user);
    }

    @Transactional
    public UserDetails loadUserByEmail(String email)
            throws UsernameNotFoundException {
        Users user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found with email : " + email)
                );

        return UserPrincipal.create(user);
    }

    @Transactional
    public UserDetails loadUserById(Long id) {
        Users user = userRepository.findById(id).orElseThrow(
            () -> new ResourceNotFoundException("User", "id", id)
        );

        return UserPrincipal.create(user);
    }
}

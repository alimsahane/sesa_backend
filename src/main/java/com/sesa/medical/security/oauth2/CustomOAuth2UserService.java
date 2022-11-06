package com.sesa.medical.security.oauth2;



import com.sesa.medical.security.UserPrincipal;
import com.sesa.medical.security.dto.AuthProvider;
import com.sesa.medical.security.exception.OAuth2AuthenticationProcessingException;
import com.sesa.medical.security.jwt.JwtUtils;
import com.sesa.medical.security.oauth2.user.OAuth2UserInfo;
import com.sesa.medical.security.oauth2.user.OAuth2UserInfoFactory;
import com.sesa.medical.users.entities.*;
import com.sesa.medical.users.repository.IRolesRepository;
import com.sesa.medical.users.repository.IStatusUsersRepository;
import com.sesa.medical.users.repository.IUsersRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collector;

@Slf4j
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private IUsersRepository userRepository;
    @Autowired
    IRolesRepository rolesRepository;

    @Autowired
    private IStatusUsersRepository statusRepo;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(oAuth2UserRequest);
        try {
            return processOAuth2User(oAuth2UserRequest, oAuth2User);
        } catch (AuthenticationException ex) {
            throw ex;
        } catch (Exception ex) {
            // Throwing an instance of AuthenticationException will trigger the OAuth2AuthenticationFailureHandler
            ex.printStackTrace();
            throw new InternalAuthenticationServiceException(ex.getMessage(), ex.getCause());
        }
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest oAuth2UserRequest, OAuth2User oAuth2User) {
        OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(oAuth2UserRequest.getClientRegistration().getRegistrationId(), oAuth2User.getAttributes());
        if(StringUtils.isEmpty(oAuth2UserInfo.getEmail())) {
            throw new OAuth2AuthenticationProcessingException("Email not found from OAuth2 provider");
        }

        Optional<Users> userOptional = userRepository.findByEmail(oAuth2UserInfo.getEmail());
        Users user;
        if(userOptional.isPresent()) {
            user = userOptional.get();
            if(!user.getProvider().equals(AuthProvider.valueOf(oAuth2UserRequest.getClientRegistration().getRegistrationId()))) {
                throw new OAuth2AuthenticationProcessingException("Looks like you're signed up with " +
                        user.getProvider() + " account. Please use your " + user.getProvider() +
                        " account to login.");
            }
            user = updateExistingUser(user, oAuth2UserInfo);
        } else {
            user = registerNewUser(oAuth2UserRequest, oAuth2UserInfo);
        }
        return UserPrincipal.create(user, oAuth2User.getAttributes());
    }

    private Users registerNewUser(OAuth2UserRequest oAuth2UserRequest, OAuth2UserInfo oAuth2UserInfo) {
        Users user = new Users();

        user.setProvider(AuthProvider.valueOf(oAuth2UserRequest.getClientRegistration().getRegistrationId()));
        user.setProviderId(oAuth2UserInfo.getId());
        user.setFirstName(oAuth2UserInfo.getName());
        user.setEmail(oAuth2UserInfo.getEmail());
        user.setImageUrl(oAuth2UserInfo.getImageUrl());
        // user.setTokenAuth(oAuth2UserRequest.getAccessToken().getTokenValue());
        Set<RolesUser> rolesList = new HashSet<>();
            RolesUser rolesUser = rolesRepository.findByName(ERoles.ROLE_USER).orElseThrow(()-> new ResourceNotFoundException("Role: ROLE_USER  not found"));
            rolesList.add(rolesUser);
           user.setRoles(rolesList);
        StatusUsers status = statusRepo.findByName(EStatusUser.USER_ENABLED)
                .orElseThrow(() -> new ResourceNotFoundException("Status: " + EStatusUser.USER_DISABLED + " not found"));
        user.setStatus(status);
        user.setCreatedAt(LocalDateTime.now());
        user.setUsing2FA(false);
        return userRepository.save(user);
    }

    private Users updateExistingUser(Users existingUser, OAuth2UserInfo oAuth2UserInfo) {
        existingUser.setFirstName(oAuth2UserInfo.getName());
        existingUser.setImageUrl(oAuth2UserInfo.getImageUrl());
        existingUser.setUsing2FA(false);
        return userRepository.save(existingUser);
    }

}

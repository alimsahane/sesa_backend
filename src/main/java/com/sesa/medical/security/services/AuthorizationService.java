package com.sesa.medical.security.services;

import com.sesa.medical.security.UserPrincipal;
import com.sesa.medical.users.entities.Users;
import com.sesa.medical.users.repository.IUsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AuthorizationService implements IAuthorizationService{
    @Autowired
    private IUsersRepository userRepo;

   /* @Autowired
    private IOfferJobService offerJobService;

    @Autowired
    private IMicroserviceService microservService;*/

    @Override
    public Users getUserInContextApp() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        UserPrincipal currentUser = (UserPrincipal) authentication.getPrincipal();
        return userRepo.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User: " + currentUser.getUsername() + " not found"));
    }

    public boolean canUpdateOwnerItem(Long itemId, String item ) {
        boolean authorized = false;
        Users currentUser = getUserInContextApp();
        switch (item) {
            case "User": {
                if (currentUser.getUserId().equals(itemId)) {
                    authorized = true;
                }
            }
            break;



            default:
                break;
        }
        return authorized;
    }

}

package com.sesa.medical.security.services;

import com.sesa.medical.users.entities.Users;

public interface IAuthorizationService {
    Users getUserInContextApp();
}

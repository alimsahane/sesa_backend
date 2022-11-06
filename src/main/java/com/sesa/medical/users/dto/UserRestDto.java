package com.sesa.medical.users.dto;

import com.sesa.medical.users.entities.RolesUser;
import com.sesa.medical.users.entities.StatusUsers;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UserRestDto{
    private Long userId;
    private String username;

    private String email;

    private List<RolesUser> roles;

    private StatusUsers status;

    private String token;
}

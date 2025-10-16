package com.mycom.myapp.user.service;

import com.mycom.myapp.user.entity.User;
import java.util.Map;

public interface UserService {
    public User getOrRegisterUser(Map<String, Object> userInfo);
}

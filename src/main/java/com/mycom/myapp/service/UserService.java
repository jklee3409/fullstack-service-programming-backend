package com.mycom.myapp.service;

import com.mycom.myapp.entity.User;
import java.util.Map;

public interface UserService {
    public User getOrRegisterUser(Map<String, Object> userInfo);
}

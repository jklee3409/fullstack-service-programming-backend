package com.mycom.myapp.user.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
@RequestMapping("/account")
public class AccountController {

    @GetMapping("/delete")
    public String showAccountDeletePage() {
        log.info("계정 삭제 페이지 접근 요청 발생!!!");
        return "account/delete";
    }
}
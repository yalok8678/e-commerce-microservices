package com.ecommerce.authentication.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RoleTestController {

    @GetMapping("/api/user/test")
    public String user() {
        return "USER access granted";
    }

    @GetMapping("/api/vendor/test")
    public String vendor() {
        return "VENDOR access granted";
    }

    @GetMapping("/api/admin/test")
    public String admin() {
        return "ADMIN access granted";
    }
}
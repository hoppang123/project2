package com.company.asset.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class SpaForwardController {

    // 점(.)이 없는 경로만 React 라우트로 간주하고 index.html로 forward
    // 예) /assets, /login, /inbox  ✅
    // 예) /index.html, /assets/app.js, /favicon.ico ❌ (정적 리소스는 제외)
    @RequestMapping(value = {
            "/{path:[^\\.]*}",
            "/**/{path:[^\\.]*}"
    })
    public String forward() {
        return "forward:/index.html";
    }
}
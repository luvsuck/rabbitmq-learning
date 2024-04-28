package com.luvsic.api;

import com.luvsic.service.DomainRegistrationInfoTemplateService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author: zyy
 * @Date: 2024/4/28 11:36
 * @Version:
 * @Description:
 */
@RestController
@RequestMapping("/task")
public class ReviewTaskApi {
    private final DomainRegistrationInfoTemplateService registrationInfoTemplateService;

    public ReviewTaskApi(DomainRegistrationInfoTemplateService registrationInfoTemplateService) {
        this.registrationInfoTemplateService = registrationInfoTemplateService;
    }

    @PostMapping("/review")
    void postReview() {
        registrationInfoTemplateService.reviewInfoTemplate("", "");
    }
}

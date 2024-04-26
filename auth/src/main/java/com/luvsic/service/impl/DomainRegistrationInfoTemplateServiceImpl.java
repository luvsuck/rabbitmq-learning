package com.luvsic.service.impl;

import com.luvsic.entity.InfoTemplateReview;
import com.luvsic.service.DomainRegistrationInfoTemplateService;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author: zyy
 * @Date: 2024/4/25 18:15
 * @Version:
 * @Description:
 */
@Service("domainRegistrationInfoTemplateService")
public class DomainRegistrationInfoTemplateServiceImpl implements DomainRegistrationInfoTemplateService {
    List<InfoTemplateReview> prepareInfoTemplates() {
        return List.of(new InfoTemplateReview("com", "阿里云", "荔枝"),
                new InfoTemplateReview("cn", "阿里云", "荔枝"),
                new InfoTemplateReview("com", "腾讯云", "荔枝"),
                new InfoTemplateReview("cn", "腾讯云", "荔枝"),
                new InfoTemplateReview("com", "GoDaddy", "荔枝"),
                new InfoTemplateReview("cn", "GoDaddy", "荔枝"),
                new InfoTemplateReview("xyz", "阿里云", "荔枝"));
    }

    @Override
    public void reviewInfoTemplate(String tldName, String rid) {
        List<InfoTemplateReview> waitToReview = prepareInfoTemplates();
        AmqpAdmin
    }
}

package com.luvsic.service.impl;

import com.luvsic.entity.InfoTemplateReview;
import com.luvsic.mq.DelayedReviewTaskSender;
import com.luvsic.service.DomainRegistrationInfoTemplateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @Author: zyy
 * @Date: 2024/4/25 18:15
 * @Version:
 * @Description:
 */
@Slf4j
@Service("domainRegistrationInfoTemplateService")
public class DomainRegistrationInfoTemplateServiceImpl implements DomainRegistrationInfoTemplateService {
    private final DelayedReviewTaskSender delayedReviewTaskSender;

    public DomainRegistrationInfoTemplateServiceImpl(DelayedReviewTaskSender delayedReviewTaskSender) {
        this.delayedReviewTaskSender = delayedReviewTaskSender;
    }

    @Override
    public void reviewInfoTemplate(String tldName, String rid) {
        int delayTime;
        for (int i = 1; i < 11; i++) {
            if (i % 3 == 0) delayTime = 0;
            else delayTime = 10000;
            delayedReviewTaskSender.sendMsg(new InfoTemplateReview(i, "com" + i, "阿里云" + i, "李四" + i, delayTime));
        }
    }
}

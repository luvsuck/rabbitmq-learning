package com.luvsic.service.impl;

import com.luvsic.entity.InfoTemplateReview;
import com.luvsic.mq.ReviewTaskProducer;
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
    private final ReviewTaskProducer taskProducer;

    public DomainRegistrationInfoTemplateServiceImpl(ReviewTaskProducer taskProducer) {
        this.taskProducer = taskProducer;
    }

    @Override
    public void reviewInfoTemplate(String tldName, String rid) {
        try {
            for (int i = 0; i < 10; i++) {
                if (i / 3 < 1)
                    Thread.sleep(5000);
                taskProducer.sendReviewTask(new InfoTemplateReview("com" + i, "阿里云" + i, "李四" + i));
                taskProducer.sendReviewTask(new InfoTemplateReview("cn" + i, "阿里云" + i, "王五" + i));
                taskProducer.sendReviewTask(new InfoTemplateReview("com" + i, "腾讯云" + i, "李四" + i));
                taskProducer.sendReviewTask(new InfoTemplateReview("cn" + i, "腾讯云" + i, "张三" + i));
                taskProducer.sendReviewTask(new InfoTemplateReview("com" + i, "GoDaddy" + i, "李四" + i));
                taskProducer.sendReviewTask(new InfoTemplateReview("cn" + i, "GoDaddy" + i, "王五" + i));
                taskProducer.sendReviewTask(new InfoTemplateReview("xyz" + i, "阿里云" + i, "王五" + i));
            }
        } catch (InterruptedException e) {
            log.error("线程中断:{}", e.getMessage());
        }
    }
}

package com.luvsic.constant;

/**
 * @Author: zyy
 * @Date: 2024/4/25 18:05
 * @Version:
 * @Description:
 */
public interface MessageConstant {
    /**
     * 域名注册信息模板审核 direct队列
     */
    String DOMAIN_REGISTRATION_INFO_TEMPLATE_REVIEW_DQUEUE = "domain_registration_info_template_review_dqueue";

    /**
     * 域名注册信息模板审核结果更新 direct队列
     */
    String DOMAIN_REGISTRATION_INFO_TEMPLATE_REVIEW_ANS_DQUEUE = "domain_registration_info_template_review_ans_dqueue";
}

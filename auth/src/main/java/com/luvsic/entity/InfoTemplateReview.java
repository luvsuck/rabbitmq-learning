package com.luvsic.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * @Author: zyy
 * @Date: 2024/4/25 18:17
 * @Version:
 * @Description:
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InfoTemplateReview implements Serializable {
    @Serial
    private static final long serialVersionUID = -5373812642947035365L;
    private String tldName;
    private String rid;
    private String ownerFullname;
}

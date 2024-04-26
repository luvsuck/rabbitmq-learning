import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Author: zyy
 * @Date: 2024/4/25 15:38
 * @Version:
 * @Description:
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthBody implements Serializable {
    private static final long serialVersionUID = 2824305791433845374L;
    private String tldName;
    private String rid;
    private String template;
}

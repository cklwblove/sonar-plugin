package cn.zhangbro.sonar.model;

import lombok.Data;

/**
 * @author zhangbro
 */
@Data
public class EsLintIssue {
    private String name;
    private String message;
    private String ruleId;
    private int severity;
    private int column;
    private int line = 1;
    private String source;
}

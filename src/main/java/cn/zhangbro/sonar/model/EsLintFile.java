package cn.zhangbro.sonar.model;

import lombok.Data;

import java.util.List;

/**
 * Defines the list of violations found by EsLint for a file.
 * 扫描某个文件后不符合规则的地方
 * @author zhangbro
 */
@Data
public class EsLintFile {

    private String filePath;

    private List<EsLintIssue> messages;

    private int errorCount;

    private int warningCount;

    private String source;

}

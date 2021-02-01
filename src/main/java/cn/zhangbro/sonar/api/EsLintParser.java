package cn.zhangbro.sonar.api;

import cn.zhangbro.sonar.model.EsLintIssue;

import java.util.List;
import java.util.Map;

public interface EsLintParser {
    Map<String, List<EsLintIssue>> parse(List<String> rawOutputBatches);
}

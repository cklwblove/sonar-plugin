package cn.zhangbro.sonar.model;


import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.sonar.api.server.debt.DebtRemediationFunction.Type;

/**
 * 规则
 *
 * @author zhangbro
 */
@Data
public class EsLintRule {
    private String key;

    private String name;

    private String severity;

    private String htmlDescription;

    private boolean hasDebtRemediation;

    private Type debtRemediationFunction;

    private String debtRemediationScalar;

    private String debtRemediationOffset;

    private String debtType;


    private String tags = "";

    public EsLintRule(String key, String severity, String name, String htmlDescription, String tags) {
        this.setKey(key);
        this.setSeverity(severity);
        this.setName(name);
        this.setHtmlDescription(htmlDescription);
        this.setTags(tags);
        setDebtRemediationFunction(Type.CONSTANT_ISSUE);
        setDebtRemediationScalar("0min");
        setDebtRemediationOffset("0min");
        setDebtType(null);
    }

    public EsLintRule(String key, String severity, String name, String htmlDescription, Type debtRemediationFunction, String debtRemediationScalar, String debtRemediationOffset,
                      String debtType, String tags) {
        this.setKey(key);
        this.setSeverity(severity);
        this.setName(name);
        this.setHtmlDescription(htmlDescription);
        this.setTags(tags);
        setHasDebtRemediation(true);
        this.setDebtRemediationFunction(debtRemediationFunction);
        this.setDebtRemediationScalar(debtRemediationScalar);
        this.setDebtRemediationOffset(debtRemediationOffset);
        this.setDebtType(debtType);
    }


    /**
     * Returns the list of tags as an array.
     *
     * @return the list of tags
     */
    public String[] getTagsAsArray() {
        if (StringUtils.isEmpty(this.tags)) {
            return new String[0];
        }
        return tags.trim().split(",");
    }
}

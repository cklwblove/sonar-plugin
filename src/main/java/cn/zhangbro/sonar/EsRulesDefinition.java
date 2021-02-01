/*
 * Copyright (C) 2017 Sylvain Leroy - BYOS Company All Rights Reserved
 * You may use, distribute and modify this code under the
 * terms of the MIT license, which unfortunately won't be
 * written for another century.
 *
 * You should have received a copy of the MIT license with
 * this file. If not, please write to: contact@sylvainleroy.com, or visit : https://sylvainleroy.com
 */
package cn.zhangbro.sonar;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.config.Configuration;
import org.sonar.api.rule.RuleStatus;
import org.sonar.api.rule.Severity;
import org.sonar.api.rules.RuleType;
import org.sonar.api.server.debt.DebtRemediationFunction;
import org.sonar.api.server.debt.DebtRemediationFunction.Type;
import org.sonar.api.server.rule.RulesDefinition;

import cn.zhangbro.sonar.model.EsLintRule;
@Slf4j
public class EsRulesDefinition implements RulesDefinition {
    public static final String REPOSITORY_NAME = "eslint";
    public static final String DEFAULT_RULE_SEVERITY = Severity.defaultSeverity();
    public static final String DEFAULT_RULE_DESCRIPTION = "No description provided for this ESLint rule";
    public static final String DEFAULT_RULE_DEBT_SCALAR = "0min";
    public static final String DEFAULT_RULE_DEBT_OFFSET = "0min";
    public static final String DEFAULT_RULE_DEBT_TYPE = RuleType.CODE_SMELL.name();
    /**
     * The SonarQube rule that will contain all unknown ESLint issues.
     */
    public static final EsLintRule ESLINT_UNKNOWN_RULE = new EsLintRule(
        "eslint-issue", Severity.MAJOR, "EsLint issues that are not yet known to the plugin", "No description for ESLint rule", "");
    //todo 自定义规则的配置文件路径
    @SuppressWarnings("HardcodedFileSeparator")
    private static final String CORE_RULES_CONFIG_RESOURCE_PATH = "/eslint/eslint-rules.properties";
    private static final String DEFAULT_TAGS = "eslint";
    private static final Logger LOGGER = LoggerFactory.getLogger(EsRulesDefinition.class);

    /**
     * Load the rules.
     *
     * @param stream          load the rules from a stream.
     * @param rulesCollection the rules collection.
     */
    public static void loadRules(InputStream stream, List<EsLintRule> rulesCollection) {
        final Properties properties = new Properties();

        try {
            properties.load(stream);
        } catch (final IOException e) {
            EsRulesDefinition.log.error("Error while loading ESLint rules: {}", e.getMessage(), e);
        }

        loadingRuleDefinitionsFromProps(rulesCollection, properties);
    }

    public static void loadingRuleDefinitionsFromProps(List<EsLintRule> rulesCollection, Properties properties) {
        for (final String propKey : properties.stringPropertyNames()) {

            if (propKey.contains(".")) {
                continue;
            }

            final String ruleEnabled = properties.getProperty(propKey);

            if (!"true".equals(ruleEnabled)) {
                continue;
            }

            final String ruleId = propKey;

            String tags = properties.getProperty(propKey + ".tags", EsRulesDefinition.DEFAULT_TAGS);

            final String ruleName = properties.getProperty(propKey + ".name", ruleId.replace("-", " "));
            final String ruleSeverity = properties.getProperty(propKey + ".severity", EsRulesDefinition.DEFAULT_RULE_SEVERITY);

            String defaultRuleDescriptionValue = EsRulesDefinition.DEFAULT_RULE_DESCRIPTION;

            if (!ruleId.contains("/")) {
                defaultRuleDescriptionValue = "See full Eslint rule description on the following link: https://eslint.org/docs/rules/" + ruleId;
            } else if (ruleId.indexOf("angular/") == 0) {
                tags += ",eslint-angular";
                defaultRuleDescriptionValue = "See full Eslint angular rule description on the following link: https://github.com/Gillespie59/eslint-plugin-angular/blob/master/docs/rules/" + ruleId.substring(8);
            } else if (ruleId.contains("/")) {
                tags += ",eslint-" + ruleId.substring(0, ruleId.indexOf("/"));
            }

            final String ruleDescription = properties.getProperty(propKey + ".description", defaultRuleDescriptionValue);


            final String debtRemediationFunction = properties.getProperty(propKey + ".debtFunc", null);
            final String debtRemediationScalar = properties
                .getProperty(propKey + ".debtScalar", EsRulesDefinition.DEFAULT_RULE_DEBT_SCALAR);
            final String debtRemediationOffset = properties
                .getProperty(propKey + ".debtOffset", EsRulesDefinition.DEFAULT_RULE_DEBT_OFFSET);
            final String debtType = properties.getProperty(propKey + ".debtType", EsRulesDefinition.DEFAULT_RULE_DEBT_TYPE);

            EsLintRule eslintRule = null;

            // try to apply the specified debt remediation function
            if (debtRemediationFunction != null) {
                final Type debtRemediationFunctionEnum = Type.valueOf(debtRemediationFunction);

                eslintRule = new EsLintRule(
                    ruleId, ruleSeverity, ruleName, ruleDescription, debtRemediationFunctionEnum, debtRemediationScalar,
                    debtRemediationOffset, debtType, tags);

            }

            // no debt remediation function specified
            if (eslintRule == null) {
                eslintRule = new EsLintRule(ruleId, ruleSeverity, ruleName, ruleDescription, tags);
            }
            eslintRule.setHtmlDescription(ruleDescription);
            rulesCollection.add(eslintRule);
        }

        rulesCollection.sort((final EsLintRule r1, final EsLintRule r2) -> r1.getKey().compareTo(r2.getKey()));
    }

    private static void createRule(RulesDefinition.NewRepository repository, EsLintRule eslintRule) {
        final RulesDefinition.NewRule sonarRule = repository
            .createRule(eslintRule.getKey()).setName(eslintRule.getName()).setSeverity(eslintRule.getSeverity())
            .setHtmlDescription(eslintRule.getHtmlDescription()).setStatus(RuleStatus.READY).setTags(eslintRule.getTagsAsArray());

        if (eslintRule.isHasDebtRemediation()) {
            DebtRemediationFunction debtRemediationFn = null;
            final RulesDefinition.DebtRemediationFunctions funcs = sonarRule.debtRemediationFunctions();

            switch (eslintRule.getDebtRemediationFunction()) {
                case LINEAR:
                    debtRemediationFn = funcs.linear(eslintRule.getDebtRemediationScalar());
                    break;

                case LINEAR_OFFSET:
                    debtRemediationFn = funcs.linearWithOffset(eslintRule.getDebtRemediationScalar(), eslintRule.getDebtRemediationOffset());
                    break;

                case CONSTANT_ISSUE:
                    debtRemediationFn = funcs.constantPerIssue(eslintRule.getDebtRemediationScalar());
                    break;
                default:
                    throw new UnsupportedOperationException("Unknown debt evaluation function " + eslintRule.getDebtRemediationFunction());
            }

            sonarRule.setDebtRemediationFunction(debtRemediationFn);
        }

        RuleType type = null;

        if (eslintRule.getDebtType() != null && RuleType.names().contains(eslintRule.getDebtType())) {
            // Try and parse it as a new-style rule type (since 5.5 SQALE's been
            // replaced
            // with something simpler, and there's really only three buckets)
            type = RuleType.valueOf(eslintRule.getDebtType());
        }

        if (type == null) {
            type = RuleType.CODE_SMELL;
        }

        sonarRule.setType(type);
    }

    private final Configuration settings;

    private final List<EsLintRule> eslintCoreRules = new ArrayList<>(100);

    private final List<EsLintRule> eslintRules = new ArrayList<>(100);

    public EsRulesDefinition() {
        this(null);
    }

    public EsRulesDefinition(Configuration settings) {

        this.settings = settings;

        loadCoreRules();
        loadCustomRules();
    }

    @Override
    public void define(RulesDefinition.Context context) {
        final RulesDefinition.NewRepository repository = context
            .createRepository(EsRulesDefinition.REPOSITORY_NAME, EsLintLanguage.LANGUAGE_KEY).setName("ESLint Analyzer");

//        createRule(repository, EsRulesDefinition.ESLINT_UNKNOWN_RULE);

        // add the ESLint builtin core rules
        for (final EsLintRule coreRule : eslintCoreRules) {
            createRule(repository, coreRule);
        }

        // add additional custom ESLint rules
        for (final EsLintRule customRule : eslintRules) {
            createRule(repository, customRule);
        }

        repository.done();
    }

    public List<EsLintRule> getCoreRules() {
        return eslintCoreRules;
    }

    public List<EsLintRule> getRules() {
        return eslintRules;
    }

    private void loadCoreRules() {
        final InputStream coreRulesStream = EsRulesDefinition.class.getResourceAsStream(EsRulesDefinition.CORE_RULES_CONFIG_RESOURCE_PATH);
        EsRulesDefinition.loadRules(coreRulesStream, eslintCoreRules);
    }

    private void loadCustomRules() {
        if (settings == null) {
            return;
        }

        final Optional<String> sonarRuleDefinitionsPathStr = settings.get(EsLintPlugin.SETTING_ES_RULE_CONFIGS);
        if (!sonarRuleDefinitionsPathStr.isPresent()) {
            LOGGER.warn(("Sonar custom rules is not defined"));
            return;
        }
        Path ruleDefPath = Paths.get(sonarRuleDefinitionsPathStr.get());
        if (!Files.exists(ruleDefPath)) {
            LOGGER.error("Path {} cannot be accessed, cannot load the Sonar ESLint custom rules", ruleDefPath);
            return ;
        }

        Properties properties = new Properties();

        String sonarRulePath = sonarRuleDefinitionsPathStr.get();
        try (BufferedInputStream inputStream = new BufferedInputStream(Files.newInputStream(ruleDefPath));) {
            properties.load(inputStream);
            EsRulesDefinition.loadingRuleDefinitionsFromProps(eslintRules, properties);
            log.info("Custom rules loaded {}", eslintRules.size());

        } catch (FileNotFoundException e) {
            LOGGER.error("Cannot find a Sonar ESLint custom rule description to the path {}", sonarRulePath, e);
        } catch (IOException e) {
            LOGGER.error("Cannot read the Sonar ESLint custom rule description (path {})", sonarRulePath, e);
        }
    }
}

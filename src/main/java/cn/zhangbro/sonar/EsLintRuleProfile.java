/*
 * Copyright (C) 2017 Sylvain Leroy - BYOSkill Company All Rights Reserved
 * You may use, distribute and modify this code under the
 * terms of the MIT license, which unfortunately won't be
 * written for another century.
 *
 * You should have received a copy of the MIT license with
 * this file. If not, please write to: sleroy at byoskill.com, or visit : www.byoskill.com
 *
 */
package cn.zhangbro.sonar;

import cn.zhangbro.sonar.model.EsLintRule;
import org.sonar.api.profiles.ProfileDefinition;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.rules.Rule;
import org.sonar.api.utils.ValidationMessages;

/**
 * 自定义规则配置
 *
 * @author zhangbro
 */
public class EsLintRuleProfile extends ProfileDefinition {
    //todo 自定义规则组名称

    /**
     * 自定义规则组名称
     */
    public static final String PROFILE_NAME = "winnerESLintDemo";

    private static void activateRule(final RulesProfile profile, final String ruleKey) {
        profile.activateRule(Rule.create(EsRulesDefinition.REPOSITORY_NAME, ruleKey), null);
    }

    @Override
    public RulesProfile createProfile(final ValidationMessages validation) {
        final RulesProfile profile = RulesProfile.create(PROFILE_NAME, EsLintLanguage.LANGUAGE_KEY);

        final EsRulesDefinition rules = new EsRulesDefinition();

//        EsLintRuleProfile.activateRule(profile, EsRulesDefinition.ESLINT_UNKNOWN_RULE.getKey());

        for (final EsLintRule coreRule : rules.getCoreRules()) {
            EsLintRuleProfile.activateRule(profile, coreRule.getKey());
        }

        return profile;
    }
}

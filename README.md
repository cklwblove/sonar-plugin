# winnerfed-sonar-eslint-plugin

## 配置文件示例

### 开关/规则id
```bash
no-await-in-loop=true
```
### 规则简介
```bash
no-await-in-loop.name=不允许await在循环体内使用
```
####严重性
- INFO   提示
- MINOR  次要
- MAJOR  主要
- CRITICAL   严重
- BLOCKER    阻断
```bash
no-await-in-loop.severity=MAJOR
```
###代码味道
- CODE_SMELL 异味
- BUG    BUG
- VULNERABILITY  漏洞
```bash
no-await-in-loop.debtType=BUG
```
###描述详情
```bash
no-await-in-loop.description=不允许await再循环体内使用，别墨迹
```
### DebtRemediationFunctions 共有三个值
- LINEAR
- LINEAR_OFFSET
- CONSTANT_ISSUE
```bash
no-await-in-loop.debtFunc=CONSTANT_ISSUE
```
###运行时间
```bash
no-await-in-loop.debtScalar=1min
```


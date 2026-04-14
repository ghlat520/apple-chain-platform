#!/usr/bin/env bash
#
# design-lint.sh — 零依赖的设计规范守护脚本
#
# 策略：存量冻结 / 增量合规
#
# 使用模式：
#   bash scripts/design-lint.sh              # 全量扫描（基线审计用）
#   bash scripts/design-lint.sh --staged     # 仅扫已 staged 的文件（pre-commit）
#   bash scripts/design-lint.sh --diff main  # 仅扫相对某分支变更的文件（PR review）
#   bash scripts/design-lint.sh file1 file2  # 扫指定文件
#
# 集成：
#   pre-commit: .husky/pre-commit → bash scripts/design-lint.sh --staged
#   CI PR 检查: bash scripts/design-lint.sh --diff origin/main
#   周期审计:   bash scripts/design-lint.sh（全量，产出基线报告）
#
# 核心规则：
#   1. 禁止硬编码颜色 hex（除 tokens.ts / tailwind.config）
#   2. 禁止硬编码 px 尺寸
#   3. 禁止硬编码 font-family
#   4. 禁止 inline style 带颜色/字号/间距
#   5. 禁止 !important
#   6. 禁止 transition: all
#   7. 禁止 outline: none 去除焦点环

set -uo pipefail

# ---------- 配置 ----------
# apple-chain-platform 三个前端同时扫描
#   - apple-web-ui   : 移动端 (Vant)
#   - frontend       : 演示前端 (Vant + Mock)
#   - apple-admin-ui : PC 管理后台 (Element Plus)
TARGET_DIRS=(
  "apple-web-ui/src"
  "frontend/src"
  "apple-admin-ui/src/views"
  "apple-admin-ui/src/components"
  "apple-admin-ui/src/layout"
)
TARGET_EXTS="ts tsx jsx vue css scss"
WHITELIST=(
  "design/tokens.js"
  "design/tokens.ts"
  "design/vant-theme.css"
  "design/element-theme.css"
  "design/global.css"
  "design/echarts-theme.js"
  "tailwind.config.js"
  "tailwind.config.ts"
  # Styleguide 本身是"展示规范"的元页面，职责就是硬编码演示字号/间距效果
  "views/Styleguide.vue"
  # apple-admin-ui 存量 SCSS 主题文件 (与 element-theme.css 同级职责)
  # 后续重构时改走 CSS 变量，目前作为存量冻结
  "apple-admin-ui/src/styles/variables.scss"
  "apple-admin-ui/src/styles/index.scss"
)

# 颜色输出
RED='\033[0;31m'
YELLOW='\033[0;33m'
GREEN='\033[0;32m'
CYAN='\033[0;36m'
NC='\033[0m'

errors=0
warnings=0

# ---------- 文件扫描范围（支持三种模式） ----------
SCAN_MODE="full"
SCAN_FILES_OVERRIDE=()

# 跨平台的 read-into-array（bash 3.2 兼容，macOS 默认）
read_lines_into_array() {
  # 用法：read_lines_into_array < <(cmd)
  SCAN_FILES_OVERRIDE=()
  local line
  while IFS= read -r line; do
    [[ -n "$line" ]] && SCAN_FILES_OVERRIDE+=("$line")
  done
}

case "${1:-}" in
  --staged)
    SCAN_MODE="staged"
    # 仅获取 staged 的前端文件
    read_lines_into_array < <(git diff --cached --name-only --diff-filter=ACM 2>/dev/null | grep -E '\.(ts|tsx|jsx|vue|css|scss)$' || true)
    ;;
  --diff)
    SCAN_MODE="diff"
    BASE_REF="${2:-origin/main}"
    read_lines_into_array < <(git diff --name-only --diff-filter=ACM "$BASE_REF"...HEAD 2>/dev/null | grep -E '\.(ts|tsx|jsx|vue|css|scss)$' || true)
    ;;
  --help|-h)
    sed -n '2,20p' "$0" | sed 's/^#//'
    exit 0
    ;;
  "")
    SCAN_MODE="full"
    ;;
  *)
    # 扫描指定文件
    SCAN_MODE="files"
    SCAN_FILES_OVERRIDE=("$@")
    ;;
esac

# 增量模式下如果没有任何文件 → 直接通过
if [[ "$SCAN_MODE" != "full" && ${#SCAN_FILES_OVERRIDE[@]} -eq 0 ]]; then
  echo -e "${GREEN}✓ design-lint [${SCAN_MODE}] 无文件需要扫描，跳过${NC}"
  exit 0
fi

# ---------- 工具函数 ----------
is_whitelisted() {
  local file="$1"
  for w in "${WHITELIST[@]}"; do
    [[ "$file" == *"$w" ]] && return 0
  done
  return 1
}

scan_files() {
  local pattern="$1"
  local message="$2"
  local severity="$3"  # error | warning
  local rule_id="$4"

  # 文件列表：增量模式用 override，全量模式用 find
  local files
  if [[ "$SCAN_MODE" != "full" ]]; then
    files=$(printf '%s\n' "${SCAN_FILES_OVERRIDE[@]}")
  else
    local find_args=()
    for dir in "${TARGET_DIRS[@]}"; do
      [[ -d "$dir" ]] && find_args+=("$dir")
    done
    [[ ${#find_args[@]} -eq 0 ]] && return
    files=$(find "${find_args[@]}" -type f \
      \( -name "*.ts" -o -name "*.tsx" -o -name "*.jsx" -o -name "*.vue" -o -name "*.css" -o -name "*.scss" \) \
      2>/dev/null)
  fi

  local found=0
  while IFS= read -r file; do
    [[ -z "$file" ]] && continue
    is_whitelisted "$file" && continue

    local matches
    matches=$(grep -nE "$pattern" "$file" 2>/dev/null || true)
    if [[ -n "$matches" ]]; then
      if [[ $found -eq 0 ]]; then
        if [[ "$severity" == "error" ]]; then
          echo -e "\n${RED}✗ [${rule_id}] ${message}${NC}"
        else
          echo -e "\n${YELLOW}⚠ [${rule_id}] ${message}${NC}"
        fi
        found=1
      fi
      while IFS= read -r match; do
        echo -e "  ${CYAN}${file}${NC}:${match}"
      done <<< "$matches"
    fi
  done <<< "$files"

  if [[ $found -eq 1 ]]; then
    if [[ "$severity" == "error" ]]; then
      errors=$((errors + 1))
    else
      warnings=$((warnings + 1))
    fi
  fi
}

# ---------- 规则 ----------

echo -e "${CYAN}▸ Running design-lint...${NC}"

# Rule D001: 硬编码 hex 颜色
scan_files \
  '#[0-9a-fA-F]{3,8}([^0-9a-fA-F]|$)' \
  '硬编码颜色 hex。必须用 tokens.color 或 tailwind class。' \
  'error' \
  'D001'

# Rule D002: 硬编码 rgb/rgba
scan_files \
  'rgba?\([0-9]' \
  '硬编码 rgb/rgba。必须用 tokens.color 或 tailwind class。' \
  'error' \
  'D002'

# Rule D003: 硬编码 px 尺寸（允许 0px / 1px / 0.5px 边框）
scan_files \
  '[^0-9"]([2-9]|[1-9][0-9]+)px[^;:]' \
  '硬编码 px 尺寸。必须用 tokens.spacing 或 tailwind class。' \
  'error' \
  'D003'

# Rule D004: 硬编码 font-family / fontFamily
scan_files \
  "(font-family|fontFamily)[[:space:]]*:[[:space:]]*['\"]" \
  '硬编码 font-family。必须用 tokens.typography.fontFamily。' \
  'error' \
  'D004'

# Rule D005: inline style 带颜色字段（多行友好——逐行扫关键词）
# 注意：D001/D002 已经抓了硬编码值，这条针对"即使用了 var 也不该 inline"的情况
scan_files \
  '^[[:space:]]*color:[[:space:]]*[\x27"]' \
  'JSX 里单独出现 color: 属性（inline style 多行）。改用 className。' \
  'error' \
  'D005'

# Rule D006: !important
scan_files \
  '!important' \
  '使用 !important。考虑重构 CSS 特异性。' \
  'warning' \
  'D006'

# Rule D007: transition: all（支持单行 CSS / 多行 JSX inline style / 字符串属性）
scan_files \
  "transition:?[[:space:]]*['\"]?all\b" \
  'transition: all 会触发意外动画。必须指定具体属性。' \
  'error' \
  'D007'

# Rule D008: outline: none / outline: 0
scan_files \
  "outline:?[[:space:]]*['\"]?(none|0)['\"]?" \
  '去除焦点环破坏无障碍。请用 :focus-visible 自定义焦点环。' \
  'error' \
  'D008'

# Rule D009: 非 tokens 的 font-size px
scan_files \
  'font-size[[:space:]]*:[[:space:]]*[0-9]+px' \
  '硬编码 font-size。必须用 tokens.typography.fontSize。' \
  'error' \
  'D009'

# Rule D010: 数字字段未用 tabular-nums（警告级）
# 这条默认关闭，开启方式：取消下面注释
# scan_files \
#   'className="[^"]*\b(price|amount|number|percent)\b' \
#   '数字展示建议加 nums-tabular class 保证对齐。' \
#   'warning' \
#   'D010'

# ---------- 结果汇总 ----------
echo ""
if [[ $errors -eq 0 && $warnings -eq 0 ]]; then
  echo -e "${GREEN}✓ design-lint 全部通过${NC}"
  exit 0
elif [[ $errors -eq 0 ]]; then
  echo -e "${YELLOW}⚠ design-lint 通过，但有 ${warnings} 条警告${NC}"
  exit 0
else
  echo -e "${RED}✗ design-lint 失败：${errors} 个错误 / ${warnings} 条警告${NC}"
  echo -e "${CYAN}  修复指南：参考项目根目录 DESIGN.md${NC}"
  exit 1
fi

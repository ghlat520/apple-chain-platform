#!/usr/bin/env bash
# Requires bash 4+ (associative arrays). On macOS: brew install bash, then run with /opt/homebrew/bin/bash
# field-audit.sh — 前端字段映射校验脚本
# 用法：
#   bash scripts/field-audit.sh                    # 校验所有模块
#   bash scripts/field-audit.sh planting            # 只校验 planting 模块
#   bash scripts/field-audit.sh planting Orchards   # 只校验某个组件
#
# 原理：扫描 .vue 文件中的 prop="xxx"，curl 对应 API，
#       报告 .vue 中存在但 API 响应中不存在的字段
#
# 需要环境变量：
#   API_BASE — 后端地址，默认 http://localhost:8080
#   AUTH_TOKEN — JWT token（从浏览器 localStorage 获取）

set -euo pipefail

API_BASE="${API_BASE:-http://localhost:5174/api}"
AUTH_TOKEN="${AUTH_TOKEN:-}"
MODULE="${1:-}"
COMPONENT="${2:-}"

# 颜色
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"
VIEWS_DIR="$PROJECT_DIR/apple-admin-ui/src/views"

# 模块→API 映射
# 格式: "视图目录|API路径|列表接口后缀"
declare -A MODULE_APIS
MODULE_APIS[planting]="planting"
MODULE_APIS[trace]="trace"
MODULE_APIS[trade]="trade"
MODULE_APIS[warehouse]="warehouse"
MODULE_APIS[coldchain]="coldchain"
MODULE_APIS[finance]="finance"
MODULE_APIS[input]="input"
MODULE_APIS[bigdata]="bigdata"

# 组件→具体 API 路径映射（覆盖默认的目录名推断）
declare -A COMPONENT_API_MAP
COMPONENT_API_MAP["Orchards"]="/planting/orchard/list"
COMPONENT_API_MAP["Farmers"]="/farm/farmers"
COMPONENT_API_MAP["HarvestBatch"]="/planting/harvest/list"
COMPONENT_API_MAP["Cultivation"]="/cultivation/batches"
COMPONENT_API_MAP["Maturity"]="/planting/maturity/standards"
COMPONENT_API_MAP["TaskPlan"]="/planting/task-plan/list"
COMPONENT_API_MAP["GrowthRecord"]="/planting/record/list"
COMPONENT_API_MAP["Analysis"]="SKIP"  # Analysis 页面是图表，不做表格字段校验

total_errors=0
total_warnings=0
total_checked=0

# 从 .vue 文件提取 prop="xxx" 字段名
extract_props() {
  local file="$1"
  grep -oP 'prop="(\K[^"]+)' "$file" 2>/dev/null | sort -u
}

# 提取模板中使用的字段（包括 {{ row.xxx }} 模式）
extract_template_fields() {
  local file="$1"
  grep -oP 'row\.(\w+)' "$file" 2>/dev/null | sed 's/row\.//' | sort -u
}

# curl API 获取字段列表
fetch_api_fields() {
  local api_path="$1"
  local url="${API_BASE}${api_path}?page=1&size=2"

  if [ -z "$AUTH_TOKEN" ]; then
    echo "NO_AUTH" >&2
    return 1
  fi

  local response
  response=$(curl -s -m 10 -H "Authorization: Bearer $AUTH_TOKEN" "$url" 2>/dev/null) || {
    echo "CURL_FAILED" >&2
    return 1
  }

  # 从 JSON 第一条记录提取字段名
  echo "$response" | python3 -c "
import sys, json
try:
    data = json.load(sys.stdin)
    records = data.get('data', {}).get('records', [])
    if not records and isinstance(data.get('data'), list):
        records = data['data']
    if records:
        fields = list(records[0].keys())
        print('\n'.join(fields))
except:
    print('PARSE_ERROR', file=sys.stderr)
    sys.exit(1)
" 2>/dev/null || {
    echo "PARSE_ERROR" >&2
    return 1
  }
}

audit_component() {
  local vue_file="$1"
  local component_name
  component_name=$(basename "$vue_file" .vue)

  # 检查是否有自定义 API 映射
  local api_path="${COMPONENT_API_MAP[$component_name]:-}"

  if [ "$api_path" = "SKIP" ]; then
    return 0
  fi

  # 如果没有自定义映射，跳过（无法自动推断）
  if [ -z "$api_path" ]; then
    echo -e "${YELLOW}  ⚠ $component_name — 无 API 映射，跳过${NC}"
    return 0
  fi

  total_checked=$((total_checked + 1))

  # 提取 prop 字段
  local props
  props=$(extract_props "$vue_file")

  if [ -z "$props" ]; then
    echo -e "${GREEN}  ✓ $component_name — 无 prop 绑定列（可能是图表/详情页）${NC}"
    return 0
  fi

  # 提取模板字段（row.xxx）
  local template_fields
  template_fields=$(extract_template_fields "$vue_file")

  # 合并去重
  local all_frontend_fields
  all_frontend_fields=$(echo -e "${props}\n${template_fields}" | sort -u | grep -v '^$')

  # curl API
  local api_fields
  api_fields=$(fetch_api_fields "$api_path") || {
    echo -e "${RED}  ✗ $component_name — API 调用失败: $api_path ($api_fields)${NC}"
    total_errors=$((total_errors + 1))
    return 0
  }

  # 逐字段检查
  local has_error=0
  local has_warning=0
  local missing_fields=""

  while IFS= read -r field; do
    [ -z "$field" ] && continue
    # 跳过状态等特殊字段（通过模板渲染，不是直接 prop）
    if echo "$field" | grep -qiE '^(status|desc|code|type|name)$'; then
      continue
    fi
    if ! echo "$api_fields" | grep -qx "$field"; then
      missing_fields="$missing_fields $field"
      has_warning=1
    fi
  done <<< "$all_frontend_fields"

  if [ $has_warning -eq 1 ]; then
    echo -e "${RED}  ✗ $component_name — 字段在 API 中不存在:$missing_fields${NC}"
    total_errors=$((total_errors + 1))
  else
    echo -e "${GREEN}  ✓ $component_name — 所有字段匹配 API${NC}"
  fi
}

echo "==========================================="
echo " 前端字段映射校验 (field-audit)"
echo " API: $API_BASE"
echo "==========================================="
echo ""

if [ -z "$AUTH_TOKEN" ]; then
  echo -e "${YELLOW}⚠ 未设置 AUTH_TOKEN，尝试从浏览器获取...${NC}"
  echo "  请在浏览器控制台执行: localStorage.getItem('token')"
  echo "  然后: export AUTH_TOKEN=<token>"
  echo ""
  echo -e "${RED}中止：需要 AUTH_TOKEN 才能调用 API${NC}"
  exit 1
fi

# 确定要检查的目录
if [ -n "$MODULE" ] && [ "$MODULE" != "all" ]; then
  target_dir="$VIEWS_DIR/$MODULE"
  if [ ! -d "$target_dir" ]; then
    echo -e "${RED}模块目录不存在: $target_dir${NC}"
    exit 1
  fi
else
  target_dir="$VIEWS_DIR"
fi

echo "检查目录: $target_dir"
echo ""

# 遍历 .vue 文件
found_files=0
shopt -s globstar nullglob
for vue_file in "$target_dir"/**/*.vue; do
  [ -f "$vue_file" ] || continue

  component_name=$(basename "$vue_file" .vue)

  # 如果指定了组件名，只检查该组件
  if [ -n "$COMPONENT" ] && [ "$component_name" != "$COMPONENT" ]; then
    continue
  fi

  found_files=$((found_files + 1))
  audit_component "$vue_file"
done

echo ""
echo "==========================================="
echo " 结果: 检查 $total_checked 个页面, $total_errors 个错误"
echo "==========================================="

[ $total_errors -gt 0 ] && exit 1 || exit 0

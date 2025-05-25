#!/bin/sh

# 設置嚴格模式
set -e

# 定義模板文件和輸出文件路徑
TEMPLATE_FILE="/etc/nginx/conf.d/default.conf.template"
CONFIG_FILE="/etc/nginx/conf.d/default.conf"

# 檢查 BACKEND_TARGET 環境變數是否設置
if [ -z "${BACKEND_URL}" ]; then
  echo "Error: BACKEND_TARGET environment variable is not set."
  exit 1
fi

# 使用 envsubst 替換模板中的變數並生成最終配置文件
# 只替換指定的變數，避免 Nginx 內建變數（如 $uri, $proxy_host）被錯誤替換
envsubst '$BACKEND_URL' < "${TEMPLATE_FILE}" > "${CONFIG_FILE}"

echo "Nginx configuration generated with BACKEND_TARGET=${BACKEND_URL}"

# 執行傳遞給 entrypoint 的 CMD (通常是 nginx -g 'daemon off;')
exec "$@"

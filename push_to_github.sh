#!/usr/bin/env bash

# StreamFlow GitHub Upload Script
# استخدام السكريبت لرفع المشروع إلى مستودع GitHub الخاص بك مباشرة

set -e

REPO_NAME="StreamFlow"
DESCRIPTION="StreamFlow: Modern Privacy-friendly YouTube Client for Android combining LibreTube and Flow with Arabic RTL support."

echo "🚀 التحضير لرفع مشروع StreamFlow إلى GitHub..."

if [ -z "$GITHUB_TOKEN" ]; then
    echo "⚠️ يرجى تعيين متغير البيئة GITHUB_TOKEN أولاً:"
    echo "export GITHUB_TOKEN=\"your_new_token_here\""
    echo "ثم قم بإعادة تشغيل السكريبت: bash push_to_github.sh"
    exit 1
fi

echo "🔍 جلب بيانات الحساب من GitHub API..."
USER_JSON=$(curl -s -H "Authorization: token $GITHUB_TOKEN" https://api.github.com/user)
GITHUB_USER=$(echo "$USER_JSON" | grep -o '"login": "[^"]*' | cut -d'"' -f4)

if [ -z "$GITHUB_USER" ]; then
    echo "❌ فشل تسجيل الدخول إلى GitHub! تأكد من صلاحية التوكن."
    exit 1
fi

echo "✅ تم التعرف على المستخدم: $GITHUB_USER"

echo "📦 إنشاء مستودع $REPO_NAME على GitHub..."
CREATE_REPO_RES=$(curl -s -X POST \
  -H "Authorization: token $GITHUB_TOKEN" \
  -H "Accept: application/vnd.github.v3+json" \
  https://api.github.com/user/repos \
  -d "{\"name\":\"$REPO_NAME\",\"description\":\"$DESCRIPTION\",\"private\":false}")

git init
git config user.name "$GITHUB_USER"
git config user.email "$GITHUB_USER@users.noreply.github.com"

git branch -M main
git add .
git commit -m "Initial commit: StreamFlow YouTube Client (LibreTube + Flow integration with Arabic support)"

REMOTE_URL="https://$GITHUB_USER:$GITHUB_TOKEN@github.com/$GITHUB_USER/$REPO_NAME.git"

git remote remove origin 2>/dev/null || true
git remote add origin "$REMOTE_URL"

echo "⬆️ جاري رفع الكود إلى GitHub..."
git push -u origin main --force

echo "🎉 تم رفع المشروع بنجاح إلى: https://github.com/$GITHUB_USER/$REPO_NAME"

#!/data/data/com.termux/files/usr/bin/bash
set -e
GH_USER="DarkXofiz"
REPO="AlphaDlc"
cd "$(dirname "$0")"
git init -b main 2>/dev/null || true
git config user.name "DarkXofiz"
git config user.email "ahmeetmuhammet78@gmail.com"
git add -A
git commit -m "Remap pipeline, gradle build and license" || true
git remote remove origin 2>/dev/null || true
git remote add origin "https://github.com/$GH_USER/$REPO.git"
git pull origin main --rebase --allow-unrelated-histories 2>/dev/null || true
git push -u origin main
echo "Tamam. Actions sekmesinde Decompile Jar, sonra Build akisini izle."

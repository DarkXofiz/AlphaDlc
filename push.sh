#!/data/data/com.termux/files/usr/bin/bash
set -e
GH_USER="DarkXofiz"
REPO="AlphaDlc"
cd "$(dirname "$0")"
git rebase --abort 2>/dev/null || true
git config user.name "DarkXofiz"
git config user.email "ahmeetmuhammet78@gmail.com"
git add -A
git commit -m "Rename duplicate variables in AICommand, SkyShaderRenderer, KillEffect" || true
git remote remove origin 2>/dev/null || true
git remote add origin "https://github.com/$GH_USER/$REPO.git"
git fetch origin main
git checkout main 2>/dev/null || true
git merge -X ours origin/main -m "Merge remote" || true
git push -u origin main
echo "Tamam. Decompile Jar, sonra Build calisir. Bitince: git pull origin main"

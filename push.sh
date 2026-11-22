#!/data/data/com.termux/files/usr/bin/bash
set -e
GH_USER="DarkXofiz"
REPO="AlphaDlc"
cd "$(dirname "$0")"
git config user.name "DarkXofiz"
git config user.email "ahmeetmuhammet78@gmail.com"
git add -A
git commit -m "Output jar name alphadlc-0_0_9.jar, drop sources jar" || true
git remote remove origin 2>/dev/null || true
git remote add origin "https://github.com/$GH_USER/$REPO.git"
git pull origin main --rebase || true
git push -u origin main
echo "Tamam. Build bitince Artifacts bolumunden alphadlc-0_0_9 indir."

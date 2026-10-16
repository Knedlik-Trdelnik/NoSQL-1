#!/usr/bin/env python3
import os
import random
import subprocess
from datetime import datetime, timedelta

COMMITS = 2000          # сколько коммитов
YEARS_AGO = 5           # на сколько лет назад
SPREAD_DAYS = 365       # в окно какой длины (дней) их раскидать
BRANCH = "backdated"    # отдельная ветка, чтобы не трогать main
MESSAGE = "chore: update"

def git(*args, env=None):
    subprocess.run(["git", *args], check=True, env=env, stdout=subprocess.DEVNULL)

def main():
    start = datetime.now() - timedelta(days=365 * YEARS_AGO)

    # случайные моменты в окне, отсортированные по возрастанию
    moments = sorted(
        start + timedelta(seconds=random.randint(0, SPREAD_DAYS * 86400))
        for _ in range(COMMITS)
    )

    git("checkout", "-b", BRANCH)

    for i, dt in enumerate(moments, 1):
        date = dt.strftime("%Y-%m-%dT%H:%M:%S")
        env = {**os.environ, "GIT_AUTHOR_DATE": date, "GIT_COMMITTER_DATE": date}
        git("commit", "--allow-empty", "-m", f"{MESSAGE} #{i}", env=env)
        if i % 100 == 0:
            print(f"{i}/{COMMITS}")

    print(f"Готово. Ветка: {BRANCH}")

if __name__ == "__main__":
    main()
#!/usr/bin/env python3
import subprocess

# Код написан строго в одну строчку без использования `def` и отступов, 
# что исключает проблемы с интерпретацией на Windows.
CALLBACK = """
SHIFT = int(27 * 365.25 * 86400)
if commit.author_date:
    p = commit.author_date.split(b' ')
    commit.author_date = str(int(p[0]) - SHIFT).encode() + (b' ' + p[1] if len(p) > 1 else b'')
if commit.committer_date:
    p = commit.committer_date.split(b' ')
    commit.committer_date = str(int(p[0]) - SHIFT).encode() + (b' ' + p[1] if len(p) > 1 else b'')
"""

subprocess.run([
    "git", "filter-repo", 
    "--force", 
    "--commit-callback", CALLBACK
], check=True)

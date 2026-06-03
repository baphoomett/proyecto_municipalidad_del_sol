#!/usr/bin/env python3
import re
import sys
from pathlib import Path

root = Path('.')
java_files = list(root.rglob('src/main/java/**/*.java'))

unused = []
for f in java_files:
    txt = f.read_text(encoding='utf-8')
    imports = re.findall(r'^[ \t]*import\s+([^;]+);', txt, flags=re.M)
    if not imports:
        continue
    for imp in imports:
        # skip wildcard imports
        simple = imp.split('.')[-1]
        if simple == '*' or simple.startswith('static'):
            continue
        # remove generic <> and array brackets? simple name is fine
        # check if simple name appears elsewhere in file (excluding import lines)
        other = re.sub(r'^[ \t]*import\s+[^;]+;', '', txt, flags=re.M)
        if re.search(r'\b' + re.escape(simple) + r'\b', other):
            continue
        unused.append((str(f), imp))

for f, imp in unused:
    print(f + ' :: ' + imp)

print('\nTotal candidates:', len(unused))

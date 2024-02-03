import lzma
import json
import sys

path = sys.argv[1]
with lzma.open(path, 'rt') as handle:
    data = json.load(handle)
print(json.dumps(data, indent=2))
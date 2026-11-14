#!/usr/bin/env python3
"""Write or update a metric fragment JSON from key=value arguments.

Values are parsed as bool, int or float when possible. Keys ending in a name listed in
STRING_SUFFIXES are always kept as strings (a commit hash can be all digits).
"""

from __future__ import annotations

import argparse
import json
from pathlib import Path

STRING_SUFFIXES = (".git_commit", ".git_branch", ".device", ".gradle_args", ".variant", ".machine", ".tool")


def parse_value(key: str, raw: str):
    if key.endswith(STRING_SUFFIXES):
        return raw
    lowered = raw.lower()
    if lowered == "true":
        return True
    if lowered == "false":
        return False
    try:
        return float(raw) if "." in raw else int(raw)
    except ValueError:
        return raw


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("output_json")
    parser.add_argument("pairs", nargs="+")
    parser.add_argument("--replace", action="store_true", help="start from an empty fragment instead of merging")
    args = parser.parse_args()

    output_json = Path(args.output_json).resolve()
    output_json.parent.mkdir(parents=True, exist_ok=True)

    current = {}
    if output_json.exists() and not args.replace:
        current = json.loads(output_json.read_text(encoding="utf-8"))
        if not isinstance(current, dict):
            raise ValueError(f"{output_json} is not a JSON object")

    for pair in args.pairs:
        if "=" not in pair:
            raise ValueError(f"Expected key=value format, got: {pair}")
        key, raw_value = pair.split("=", 1)
        key = key.strip()
        if not key:
            raise ValueError(f"Metric key cannot be blank: {pair}")
        current[key] = parse_value(key, raw_value.strip())

    output_json.write_text(json.dumps(current, indent=2, sort_keys=True) + "\n", encoding="utf-8")
    print(f"Wrote {output_json}")


if __name__ == "__main__":
    main()

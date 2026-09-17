#!/usr/bin/env python3
"""Write or update a metric fragment JSON from key=value arguments."""

from __future__ import annotations

import argparse
import json
from pathlib import Path


def parse_value(raw: str):
    lowered = raw.lower()
    if lowered == "true":
        return True
    if lowered == "false":
        return False
    try:
        if "." in raw:
            return float(raw)
        return int(raw)
    except ValueError:
        return raw


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("output_json")
    parser.add_argument("pairs", nargs="+")
    args = parser.parse_args()

    output_json = Path(args.output_json).resolve()
    output_json.parent.mkdir(parents=True, exist_ok=True)

    current = {}
    if output_json.exists():
        try:
            current = json.loads(output_json.read_text(encoding="utf-8"))
            if not isinstance(current, dict):
                current = {}
        except json.JSONDecodeError:
            current = {}

    for pair in args.pairs:
        if "=" not in pair:
            raise ValueError(f"Expected key=value format, got: {pair}")
        key, raw_value = pair.split("=", 1)
        key = key.strip()
        if not key:
            raise ValueError(f"Metric key cannot be blank: {pair}")
        current[key] = parse_value(raw_value.strip())

    output_json.write_text(json.dumps(current, indent=2, sort_keys=True) + "\n", encoding="utf-8")
    print(f"Wrote {output_json}")


if __name__ == "__main__":
    main()

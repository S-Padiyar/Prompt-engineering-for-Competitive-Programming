"""Add Codeforces ratings to a three-column manual verdict log."""

from __future__ import annotations

import argparse
from pathlib import Path
from typing import List, Optional

from cf_prompting.ratings import add_ratings_to_rows, build_problem_rating_map, write_csv_rows


def build_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("base_dir", help="AI results directory organized by rating and problem ID")
    parser.add_argument("input_file", help="Three- or four-column verdict CSV file")
    parser.add_argument("-o", "--output", default="Final_IDs.txt", help="rated output file")
    parser.add_argument(
        "--missing-log", default="missing_ids.txt", help="file that receives IDs without a rating"
    )
    return parser


def main(argv: Optional[List[str]] = None) -> int:
    args = build_parser().parse_args(argv)
    input_path = Path(args.input_file)
    if not input_path.is_file():
        print(f"Error: input file does not exist: '{input_path}'.")
        return 2

    try:
        ratings = build_problem_rating_map(args.base_dir)
        with input_path.open("r", encoding="utf-8", newline="") as handle:
            rows, missing_ids, malformed_count = add_ratings_to_rows(handle, ratings)
        write_csv_rows(args.output, rows)
        Path(args.missing_log).write_text(
            "".join(f"{problem_id}\n" for problem_id in missing_ids), encoding="utf-8"
        )
    except (OSError, ValueError) as exc:
        print(f"Error: {exc}")
        return 1

    print(f"Updated file written to {args.output}")
    print(f"Missing problem IDs written to {args.missing_log}: {len(missing_ids)}")
    if malformed_count:
        print(f"Warning: skipped {malformed_count} malformed row(s).")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

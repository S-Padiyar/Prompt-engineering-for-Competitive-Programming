"""Convert a verdict log into the experiment's Excel analysis workbook."""

from __future__ import annotations

import argparse
from typing import List, Optional

from cf_prompting.analysis import create_analysis_workbook, read_verdict_file


def convert_txt_to_excel(txt_file: str, excel_file: str) -> int:
    records, malformed_count = read_verdict_file(txt_file)
    if not records:
        raise ValueError("The verdict file contains no usable records.")
    create_analysis_workbook(records, excel_file)
    print(f"Created '{excel_file}' with accuracy, combinations, and NP comparisons.")
    if malformed_count:
        print(f"Warning: skipped {malformed_count} malformed row(s).")
    return malformed_count


def build_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("input_file", nargs="?", default="Data_With_Ratings.txt")
    parser.add_argument("output_file", nargs="?", default="data.xlsx")
    return parser


def main(argv: Optional[List[str]] = None) -> int:
    args = build_parser().parse_args(argv)
    try:
        convert_txt_to_excel(args.input_file, args.output_file)
    except (OSError, RuntimeError, ValueError) as exc:
        print(f"Error: {exc}")
        return 1
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

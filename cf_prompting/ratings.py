"""Pure helpers for adding rating fields to manual verdict logs."""

from __future__ import annotations

import csv
from collections.abc import Iterable, Sequence
from pathlib import Path
from typing import Dict, List, Tuple, Union


def build_problem_rating_map(base_dir: Union[str, Path]) -> Dict[str, str]:
    root = Path(base_dir)
    if not root.is_dir():
        raise ValueError(f"AI results directory does not exist: '{root}'.")
    ratings: Dict[str, str] = {}
    for rating_dir in sorted(path for path in root.iterdir() if path.is_dir()):
        if rating_dir.name == "unrated":
            continue
        for problem_dir in (path for path in rating_dir.iterdir() if path.is_dir()):
            previous = ratings.get(problem_dir.name)
            if previous is not None and previous != rating_dir.name:
                raise ValueError(
                    f"Problem '{problem_dir.name}' appears under ratings {previous} and {rating_dir.name}."
                )
            ratings[problem_dir.name] = rating_dir.name
    return ratings


def add_ratings_to_rows(
    lines: Iterable[str], ratings: Dict[str, str]
) -> Tuple[List[List[str]], List[str], int]:
    """Add missing fourth fields; return rows, missing IDs, and malformed-row count."""
    output_rows: List[List[str]] = []
    missing_ids: List[str] = []
    malformed = 0
    for row in csv.reader(lines, skipinitialspace=True):
        cleaned = [value.strip() for value in row]
        if not cleaned or not cleaned[0]:
            malformed += 1
            continue
        if len(cleaned) < 3 or len(cleaned) > 4:
            malformed += 1
            continue
        if len(cleaned) == 3:
            rating = ratings.get(cleaned[0])
            if rating is None:
                missing_ids.append(cleaned[0])
                cleaned.append("")
            else:
                cleaned.append(rating)
        output_rows.append(cleaned)
    return output_rows, missing_ids, malformed


def write_csv_rows(path: Union[str, Path], rows: Sequence[Sequence[str]]) -> None:
    with Path(path).open("w", encoding="utf-8", newline="") as handle:
        csv.writer(handle, lineterminator="\n").writerows(rows)

"""Reusable discovery and CSV-log helpers for manual submission review."""

from __future__ import annotations

import csv
from collections.abc import Iterable, Iterator
from pathlib import Path
from typing import Set, TextIO, Tuple, Union

from .artifacts import find_java_file

ALLOWED_PROMPTS = ("NP", "CoT", "CoT-ADV", "PC")


def load_processed_entries(path: Union[str, Path]) -> Set[Tuple[str, str]]:
    """Load problem/prompt keys from an existing review log."""
    log_path = Path(path)
    if not log_path.exists():
        return set()
    processed: Set[Tuple[str, str]] = set()
    with log_path.open("r", encoding="utf-8", newline="") as handle:
        for row in csv.reader(handle, skipinitialspace=True):
            if len(row) >= 2 and row[0].strip() and row[1].strip():
                processed.add((row[0].strip(), row[1].strip()))
    return processed


def iter_review_files(
    base_dir: Union[str, Path], prompt: str, processed: Iterable[Tuple[str, str]] = ()
) -> Iterator[Tuple[str, str, Path]]:
    """Yield unreviewed rating, problem ID, and Java path in deterministic order."""
    root = Path(base_dir)
    processed_set = set(processed)
    if not root.is_dir():
        return
    for rating_dir in sorted(path for path in root.iterdir() if path.is_dir()):
        for problem_dir in sorted(path for path in rating_dir.iterdir() if path.is_dir()):
            if (problem_dir.name, prompt) in processed_set:
                continue
            prompt_dir = problem_dir / prompt
            if not prompt_dir.is_dir():
                continue
            java_path = find_java_file(prompt_dir, prompt, problem_dir.name)
            if java_path is not None:
                yield rating_dir.name, problem_dir.name, java_path


def append_review_entry(handle: TextIO, problem_id: str, prompt: str, verdict: str) -> None:
    writer = csv.writer(handle, lineterminator="\n")
    writer.writerow([problem_id, prompt, verdict])
    handle.flush()

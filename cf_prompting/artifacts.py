"""Helpers for generated source files and resumable status data."""

from __future__ import annotations

import json
import os
import re
import tempfile
from collections.abc import Iterator, Mapping
from contextlib import suppress
from pathlib import Path
from typing import Any, Dict, List, Optional, Tuple, Union

StatusMap = Dict[Tuple[Union[int, str], str], Dict[str, Any]]

_JAVA_FENCE = re.compile(r"```\s*java\s*\n?(.*?)```", re.IGNORECASE | re.DOTALL)


def extract_java_code(response_text: Optional[str]) -> str:
    """Return all fenced Java blocks from a model response, in source order."""
    if not response_text:
        return ""
    return "\n\n".join(block.strip() for block in _JAVA_FENCE.findall(response_text)).strip()


def atomic_write_json(path: Union[str, Path], value: Any) -> None:
    """Write JSON through a sibling temporary file so interruptions cannot truncate state."""
    destination = Path(path)
    destination.parent.mkdir(parents=True, exist_ok=True)
    descriptor, temporary_name = tempfile.mkstemp(
        dir=str(destination.parent), prefix=f".{destination.name}.", suffix=".tmp"
    )
    try:
        with os.fdopen(descriptor, "w", encoding="utf-8") as handle:
            json.dump(value, handle, ensure_ascii=False, indent=4)
            handle.write("\n")
        os.replace(temporary_name, destination)
    except BaseException:
        with suppress(FileNotFoundError):
            os.unlink(temporary_name)
        raise


def load_status_map(path: Union[str, Path]) -> StatusMap:
    """Load status records, ignoring malformed entries while preserving valid ones."""
    status_path = Path(path)
    if not status_path.exists():
        return {}
    raw_data = json.loads(status_path.read_text(encoding="utf-8"))
    if not isinstance(raw_data, list):
        raise ValueError("Status file must contain a JSON array.")

    status_map: StatusMap = {}
    for entry in raw_data:
        if not isinstance(entry, Mapping):
            continue
        rating = entry.get("rating")
        problem_id = entry.get("problem_id")
        prompt_status = entry.get("prompts_status", {})
        if rating is None or not isinstance(problem_id, str) or not isinstance(prompt_status, dict):
            continue
        status_map[(rating, problem_id)] = prompt_status
    return status_map


def serialize_status_map(status_map: StatusMap) -> List[Dict[str, Any]]:
    """Convert the tuple-keyed map to the historical JSON list format."""
    return [
        {"rating": rating, "problem_id": problem_id, "prompts_status": prompt_status}
        for (rating, problem_id), prompt_status in sorted(
            status_map.items(), key=lambda item: (str(item[0][0]), item[0][1])
        )
    ]


def prompt_output_is_complete(output_dir: Union[str, Path], prompt: str, problem_id: str) -> bool:
    output_path = Path(output_dir)
    expected = (
        output_path / f"{prompt}-{problem_id}.java",
        output_path / f"{prompt}-response.txt",
        output_path / f"{prompt}-interaction.json",
    )
    return output_path.is_dir() and all(
        path.is_file() and path.stat().st_size > 0 for path in expected
    )


def find_java_file(prompt_dir: Union[str, Path], prompt: str, problem_id: str) -> Optional[Path]:
    """Find generated Java while supporting both historical hyphen and underscore names."""
    directory = Path(prompt_dir)
    candidates = (
        directory / f"{prompt}-{problem_id}.java",
        directory / f"{prompt}_{problem_id}.java",
    )
    for candidate in candidates:
        if candidate.is_file():
            return candidate
    java_files = sorted(directory.glob("*.java"))
    return java_files[0] if len(java_files) == 1 else None


def iter_prompt_directories(base_dir: Union[str, Path]) -> Iterator[Tuple[str, str, str, Path]]:
    """Yield rating, problem ID, prompt name, and path in deterministic order."""
    root = Path(base_dir)
    if not root.is_dir():
        return
    for rating_dir in sorted(path for path in root.iterdir() if path.is_dir()):
        for problem_dir in sorted(path for path in rating_dir.iterdir() if path.is_dir()):
            for prompt_dir in sorted(path for path in problem_dir.iterdir() if path.is_dir()):
                yield rating_dir.name, problem_dir.name, prompt_dir.name, prompt_dir


def find_unparseable_java(base_dir: Union[str, Path]) -> List[Dict[str, str]]:
    """Find non-empty responses containing Java when no non-empty Java artifact exists."""
    issues: List[Dict[str, str]] = []
    for rating, problem_id, prompt, prompt_dir in iter_prompt_directories(base_dir):
        response_path = prompt_dir / f"{prompt}-response.txt"
        java_path = find_java_file(prompt_dir, prompt, problem_id)
        has_java = java_path is not None and java_path.stat().st_size > 0
        if has_java or not response_path.is_file() or response_path.stat().st_size == 0:
            continue
        try:
            response = response_path.read_text(encoding="utf-8")
        except OSError:
            continue
        if "public class" in response or "class Main" in response:
            issues.append(
                {
                    "rating": rating,
                    "problem_id": problem_id,
                    "prompt_name": prompt,
                    "response_file": str(response_path),
                }
            )
    return issues

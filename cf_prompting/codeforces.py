"""Codeforces API, filtering, and problem-statement organization helpers."""

from __future__ import annotations

import json
import shutil
from collections.abc import Mapping
from datetime import datetime, timezone
from pathlib import Path
from typing import Any, Dict, List, Tuple, Union

CODEFORCES_PROBLEMS_API_URL = "https://codeforces.com/api/problemset.problems"
CODEFORCES_CONTESTS_API_URL = "https://codeforces.com/api/contest.list"
HTTP_TIMEOUT_SECONDS = 30


def _get_api_result(http_client: Any, url: str, description: str) -> Any:
    try:
        response = http_client.get(url, timeout=HTTP_TIMEOUT_SECONDS)
        response.raise_for_status()
        payload = response.json()
    except json.JSONDecodeError as exc:
        raise RuntimeError(f"Codeforces returned invalid JSON for {description}.") from exc
    except Exception as exc:
        raise RuntimeError(f"Could not fetch Codeforces {description}: {exc}") from exc

    if not isinstance(payload, Mapping) or payload.get("status") != "OK":
        comment = (
            payload.get("comment", "unknown API error")
            if isinstance(payload, Mapping)
            else "invalid payload"
        )
        raise RuntimeError(f"Codeforces rejected the {description} request: {comment}")
    return payload.get("result")


def fetch_codeforces_data(http_client: Any) -> Tuple[List[Dict[str, Any]], Dict[int, int]]:
    """Fetch problem metadata and a contest-ID-to-start-time map with bounded HTTP calls."""
    problem_result = _get_api_result(http_client, CODEFORCES_PROBLEMS_API_URL, "problem set")
    contest_result = _get_api_result(http_client, CODEFORCES_CONTESTS_API_URL, "contest list")

    if not isinstance(problem_result, Mapping) or not isinstance(
        problem_result.get("problems"), list
    ):
        raise RuntimeError("Codeforces problem-set response did not contain a problems array.")
    if not isinstance(contest_result, list):
        raise RuntimeError("Codeforces contest-list response was not an array.")

    contest_times: Dict[int, int] = {}
    for contest in contest_result:
        if not isinstance(contest, Mapping):
            continue
        contest_id = contest.get("id")
        start_time = contest.get("startTimeSeconds")
        if isinstance(contest_id, int) and isinstance(start_time, int):
            contest_times[contest_id] = start_time
    return problem_result["problems"], contest_times


def filter_problem_details(
    problems: List[Dict[str, Any]],
    contest_times: Mapping[int, int],
    min_rating: int,
    max_rating: int,
    earliest: datetime,
    latest: datetime,
) -> List[Dict[str, Any]]:
    """Filter ordinary rated problems by inclusive rating and UTC contest date."""
    if min_rating > max_rating:
        raise ValueError("Minimum rating cannot exceed maximum rating.")
    if earliest > latest:
        raise ValueError("Earliest date cannot be after latest date.")

    filtered: List[Dict[str, Any]] = []
    for problem in problems:
        if not isinstance(problem, Mapping):
            continue
        rating = problem.get("rating")
        contest_id = problem.get("contestId")
        index = problem.get("index")
        tags = problem.get("tags", [])
        if (
            not isinstance(rating, int)
            or not min_rating <= rating <= max_rating
            or not isinstance(contest_id, int)
            or not isinstance(index, str)
            or not isinstance(tags, list)
            or any(isinstance(tag, str) and tag.startswith("*special") for tag in tags)
        ):
            continue

        start_time = contest_times.get(contest_id)
        if not isinstance(start_time, int):
            continue
        try:
            # Codeforces timestamps are UTC; stripping tzinfo keeps comparison with CLI dates simple.
            contest_datetime = datetime.fromtimestamp(start_time, tz=timezone.utc).replace(
                tzinfo=None
            )
        except (OSError, OverflowError, ValueError):
            continue
        if not earliest <= contest_datetime <= latest:
            continue

        problem_id = f"{contest_id}{index}"
        filtered.append(
            {
                "id": problem_id,
                "name": problem.get("name", "N/A"),
                "link": f"https://codeforces.com/problemset/problem/{contest_id}/{index}",
                "rating": rating,
                "tags": tags,
                "contestId": contest_id,
                "index": index,
                "contestDate": contest_datetime.strftime("%Y-%m-%d %H:%M:%S"),
            }
        )
    return filtered


def organize_scraped_problems(
    scraped_dir: Union[str, Path], target_dir: Union[str, Path]
) -> List[Dict[str, Any]]:
    """Copy scraped statements into rating/problem directories and return their metadata."""
    source_root, target_root = Path(scraped_dir), Path(target_dir)
    if not source_root.is_dir():
        return []

    organized: List[Dict[str, Any]] = []
    for rating_dir in sorted(path for path in source_root.iterdir() if path.is_dir()):
        try:
            rating = int(rating_dir.name)
        except ValueError:
            continue
        for problem_dir in sorted(path for path in rating_dir.iterdir() if path.is_dir()):
            statement = next(
                (
                    path
                    for path in sorted(problem_dir.glob("*.txt"))
                    if path.name.startswith(problem_dir.name)
                ),
                None,
            )
            if statement is None:
                continue
            destination = target_root / rating_dir.name / problem_dir.name / statement.name
            destination.parent.mkdir(parents=True, exist_ok=True)
            shutil.copy2(statement, destination)
            organized.append(
                {"rating": rating, "problem_id": problem_dir.name, "file_path": str(destination)}
            )
    return organized

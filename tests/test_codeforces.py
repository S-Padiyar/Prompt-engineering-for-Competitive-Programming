from datetime import datetime, timezone

import pytest

from cf_prompting.codeforces import (
    CODEFORCES_CONTESTS_API_URL,
    CODEFORCES_PROBLEMS_API_URL,
    HTTP_TIMEOUT_SECONDS,
    fetch_codeforces_data,
    filter_problem_details,
    organize_scraped_problems,
)


class FakeResponse:
    def __init__(self, payload):
        self.payload = payload

    def raise_for_status(self):
        return None

    def json(self):
        return self.payload


class FakeHttpClient:
    def __init__(self):
        self.calls = []

    def get(self, url, timeout):
        self.calls.append((url, timeout))
        if url == CODEFORCES_PROBLEMS_API_URL:
            return FakeResponse({"status": "OK", "result": {"problems": [{"contestId": 1}]}})
        return FakeResponse(
            {"status": "OK", "result": [{"id": 1, "startTimeSeconds": 1_700_000_000}]}
        )


def test_fetch_uses_timeouts_and_validates_shape():
    client = FakeHttpClient()
    problems, contest_times = fetch_codeforces_data(client)
    assert problems == [{"contestId": 1}]
    assert contest_times == {1: 1_700_000_000}
    assert client.calls == [
        (CODEFORCES_PROBLEMS_API_URL, HTTP_TIMEOUT_SECONDS),
        (CODEFORCES_CONTESTS_API_URL, HTTP_TIMEOUT_SECONDS),
    ]


def test_filter_is_inclusive_utc_and_skips_special_or_malformed_problems():
    timestamp = int(datetime(2024, 1, 15, 12, tzinfo=timezone.utc).timestamp())
    problems = [
        {"contestId": 10, "index": "A", "rating": 1600, "name": "Valid", "tags": []},
        {
            "contestId": 10,
            "index": "B",
            "rating": 1600,
            "name": "Special",
            "tags": ["*special"],
        },
        {"contestId": 10, "index": "C", "rating": None, "tags": []},
    ]
    result = filter_problem_details(
        problems,
        {10: timestamp},
        1600,
        1700,
        datetime(2024, 1, 15),
        datetime(2024, 1, 15, 23, 59, 59),
    )
    assert [problem["id"] for problem in result] == ["10A"]
    assert result[0]["contestDate"] == "2024-01-15 12:00:00"

    with pytest.raises(ValueError, match="Minimum"):
        filter_problem_details([], {}, 1800, 1600, datetime.min, datetime.max)


def test_organize_skips_non_numeric_directories(tmp_path):
    source = tmp_path / "scraped"
    statement_dir = source / "1600" / "10A"
    statement_dir.mkdir(parents=True)
    (statement_dir / "10A.txt").write_text("statement", encoding="utf-8")
    (source / "misc").mkdir()

    organized = organize_scraped_problems(source, tmp_path / "organized")
    assert organized[0]["rating"] == 1600
    assert (tmp_path / "organized" / "1600" / "10A" / "10A.txt").read_text() == "statement"

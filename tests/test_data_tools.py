from io import StringIO

from cf_prompting.analysis import VerdictRecord, parse_verdict_rows
from cf_prompting.ratings import add_ratings_to_rows, build_problem_rating_map
from cf_prompting.review import iter_review_files, load_processed_entries


def test_parse_verdict_rows_reports_malformed_and_preserves_optional_rating():
    records, malformed = parse_verdict_rows(
        ["1A, NP, A, 1600\n", "2B, CoT, **\n", "bad\n", ", PC, WA\n"]
    )
    assert records == [
        VerdictRecord("1A", "NP", "A", "1600"),
        VerdictRecord("2B", "CoT", "**", ""),
    ]
    assert records[0].solved
    assert not records[1].is_valid
    assert malformed == 2


def test_rating_map_ignores_unrated_and_adds_missing_fields(tmp_path):
    (tmp_path / "1600" / "1A").mkdir(parents=True)
    (tmp_path / "unrated" / "1A").mkdir(parents=True)
    ratings = build_problem_rating_map(tmp_path)
    assert ratings == {"1A": "1600"}

    rows, missing, malformed = add_ratings_to_rows(
        StringIO("1A,NP,A\n2B,CoT,WA\ninvalid\n"), ratings
    )
    assert rows == [["1A", "NP", "A", "1600"], ["2B", "CoT", "WA", ""]]
    assert missing == ["2B"]
    assert malformed == 1


def test_review_discovery_supports_historical_names_and_skips_processed(tmp_path):
    prompt_dir = tmp_path / "1600" / "1A" / "PC"
    prompt_dir.mkdir(parents=True)
    java_path = prompt_dir / "PC_1A.java"
    java_path.write_text("class Main {}", encoding="utf-8")

    assert list(iter_review_files(tmp_path, "PC")) == [("1600", "1A", java_path)]
    assert list(iter_review_files(tmp_path, "PC", {("1A", "PC")})) == []

    log_path = tmp_path / "review.txt"
    log_path.write_text('1A,PC,"A, manually checked"\nmalformed\n', encoding="utf-8")
    assert load_processed_entries(log_path) == {("1A", "PC")}

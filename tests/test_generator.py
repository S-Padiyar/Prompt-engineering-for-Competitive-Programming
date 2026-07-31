from pathlib import Path
from types import SimpleNamespace

import generator


class FakeMessage:
    content = "```JAVA\nclass Main {}\n```"


class FakeChoice:
    message = FakeMessage()


class FakeResponse:
    choices = [FakeChoice()]

    def model_dump_json(self, indent=2):
        return '{"choices": []}'


class FakeCompletions:
    def create(self, **kwargs):
        assert kwargs["model"] == generator.AZURE_OPENAI_MODEL_NAME
        return FakeResponse()


class FakeClient:
    class Chat:
        completions = FakeCompletions()

    chat = Chat()


def test_api_success_and_java_extraction(tmp_path, monkeypatch):
    monkeypatch.setattr(generator, "AZURE_OPENAI_ENDPOINT", "https://example.test")
    monkeypatch.setattr(generator, "AZURE_OPENAI_API_KEY", "secret")

    content, interaction = generator.call_openai_api(
        FakeClient(), [{"role": "user", "content": "problem"}], "NP", log_dir=tmp_path
    )
    assert generator.extract_java_code(content) == "class Main {}"
    assert interaction["sent_messages"][0]["role"] == "user"
    assert len(list(tmp_path.glob("*.json"))) == 1


def test_cleanup_refuses_workspace_root(tmp_path, monkeypatch):
    monkeypatch.chdir(tmp_path)
    try:
        generator.clear_pipeline_directories([Path(".")])
    except ValueError as exc:
        assert "unsafe path" in str(exc)
    else:
        raise AssertionError("workspace root deletion should have been rejected")


def test_api_retry_classification():
    assert not generator.is_transient_api_error(SimpleNamespace(status_code=400))
    assert generator.is_transient_api_error(SimpleNamespace(status_code=429))
    assert generator.is_transient_api_error(SimpleNamespace(status_code=503))
    assert generator.is_transient_api_error(SimpleNamespace())

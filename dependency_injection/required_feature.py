from typing import Any, Callable

from dependency_injection.di_assertions import NoAssertion
from dependency_injection.feature_broker import features


class RequiredFeature(object):
    def __init__(self, feature: str, assertion: Callable[[Any], bool] = NoAssertion) -> None:
        self.feature = feature
        self.assertion = assertion

    def __get__(self, obj: Any) -> Any:
        return self.result  # <-- will request the feature upon first call

    def __getattr__(self, name: str) -> Any:
        assert name == 'result', "Unexpected attribute request other then 'result'"
        self.result = self.Request()
        return self.result

    def request(self, none_if_missing: bool = False) -> Any:
        if none_if_missing:
            obj = features.get(self.feature)
        else:
            obj = features[self.feature]

        assert self.assertion(obj), f"The value {obj:r} of {self.feature:r} does not match the specified criteria"
        return obj

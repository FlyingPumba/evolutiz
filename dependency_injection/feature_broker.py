# From http://code.activestate.com/recipes/413268/
from typing import Any, Optional, Dict, Callable


class FeatureBroker:
    def __init__(self, allowReplace: bool = False) -> None:
        self.providers: Dict[str, Callable[[], Any]] = {}
        self.allowReplace = allowReplace

    def provide(self, feature: str, provider: Any, *args: Any, **kwargs: Any) -> None:
        if not self.allowReplace:
            assert feature not in self.providers, f"Duplicate feature: {feature:r}"
        if callable(provider):
            def call() -> Any:
                return provider(*args, **kwargs)
        else:
            def call() -> Any:
                return provider
        self.providers[feature] = call

    def get(self, feature: str, default: Any = None) -> Optional[Any]:
        try:
            provider = self.providers[feature]
        except KeyError:
            return default
        return provider()

    def __getitem__(self, feature: str) -> Any:
        try:
            provider = self.providers[feature]
        except KeyError:
            raise KeyError(f"Unknown feature named {feature!r}")
        return provider()


features = FeatureBroker(allowReplace=True)

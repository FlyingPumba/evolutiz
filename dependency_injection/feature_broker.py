# From http://code.activestate.com/recipes/413268/
from typing import Any, Optional, Dict, Callable


class FeatureBroker:
    def __init__(self, allowReplace: bool = False) -> None:
        self.providers: Dict[str, Callable] = {}
        self.allowReplace = allowReplace

    def provide(self, feature: str, provider: Any, *args, **kwargs) -> None:
        if not self.allowReplace:
            assert feature not in self.providers, "Duplicate feature: %r" % feature
        if callable(provider):
            def call():
                return provider(*args, **kwargs)
        else:
            def call():
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
            raise KeyError("Unknown feature named %r" % feature)
        return provider()


features = FeatureBroker(allowReplace=True)

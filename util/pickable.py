def pickable_function(instance, name, args=(), kwargs=None):
    "indirect caller for instance methods and multiprocessing"
    if kwargs is None:
        kwargs = {}
    return getattr(instance, name)(*args, **kwargs)
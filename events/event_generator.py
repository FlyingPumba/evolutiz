import random

from events.all_events import event_classes

def generate_new_event():
    event_class = random.choice(event_classes)
    event = event_class.generate()
    return event